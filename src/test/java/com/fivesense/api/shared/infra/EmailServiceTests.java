package com.fivesense.api.shared.infra;

import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTests {
    @Test void sendsConfiguredMessageAndReturnsSuccess() {
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailService service = new EmailService(sender, "noreply@example.com");

        assertThat(service.send("user@example.com", "subject", "body")).isTrue();

        var message = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(message.capture());
        assertThat(message.getValue().getFrom()).isEqualTo("noreply@example.com");
        assertThat(message.getValue().getTo()).containsExactly("user@example.com");
        assertThat(message.getValue().getSubject()).isEqualTo("subject");
        assertThat(message.getValue().getText()).isEqualTo("body");
    }

    @Test void reportsDeliveryFailureWithoutPropagatingMailException() {
        JavaMailSender sender = mock(JavaMailSender.class);
        doThrow(new IllegalStateException("smtp unavailable")).when(sender).send(any(SimpleMailMessage.class));
        EmailService service = new EmailService(sender, "");

        assertThat(service.send("user@example.com", "subject", "body")).isFalse();
    }

    @Test void sendsEveryAttachmentAndReturnsSuccess() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mime = new JavaMailSenderImpl().createMimeMessage();
        when(sender.createMimeMessage()).thenReturn(mime);
        EmailService service = new EmailService(sender, "noreply@example.com");
        Path first = Files.createTempFile("fivesense-attach", ".png");
        Path second = Files.createTempFile("fivesense-attach", ".jpg");
        try {
            Files.writeString(first, "image-1");
            Files.writeString(second, "image-2");
            var attachments = List.of(new EmailService.Attachment(first, "foto.png"), new EmailService.Attachment(second, "foto.jpg"));

            assertThat(service.sendWithAttachments("user@example.com", "subject", "body", attachments)).isTrue();

            verify(sender).send(mime);
            mime.saveChanges();
            assertThat(mime.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
            assertThat(mime.getSubject()).isEqualTo("subject");
            assertThat(attachmentFileNames(mime)).containsExactly("foto.png", "foto.jpg");
        } finally {
            Files.deleteIfExists(first);
            Files.deleteIfExists(second);
        }
    }

    @Test void keepsAttachmentsThatShareTheSameName() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mime = new JavaMailSenderImpl().createMimeMessage();
        when(sender.createMimeMessage()).thenReturn(mime);
        EmailService service = new EmailService(sender, "");
        Path file = Files.createTempFile("fivesense-attach", ".png");
        try {
            Files.writeString(file, "image");
            var attachments = List.of(new EmailService.Attachment(file, "imagem.png"), new EmailService.Attachment(file, "imagem.png"));

            assertThat(service.sendWithAttachments("user@example.com", "subject", "body", attachments)).isTrue();

            mime.saveChanges();
            assertThat(attachmentFileNames(mime)).containsExactly("imagem.png", "2-imagem.png");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test void reportsAttachmentDeliveryFailureWithoutPropagating() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
        doThrow(new IllegalStateException("smtp unavailable")).when(sender).send(any(MimeMessage.class));
        EmailService service = new EmailService(sender, "");
        Path file = Files.createTempFile("fivesense-attach", ".png");
        try {
            Files.writeString(file, "image-bytes");
            assertThat(service.sendWithAttachments("user@example.com", "subject", "body", List.of(new EmailService.Attachment(file, "foto.png")))).isFalse();
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /** Collects the file names of every attachment part, searching nested multiparts. */
    private static List<String> attachmentFileNames(MimeMessage mime) throws Exception {
        List<String> names = new ArrayList<>();
        collect((Multipart) mime.getContent(), names);
        return names;
    }

    private static void collect(Multipart multipart, List<String> names) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            var part = multipart.getBodyPart(i);
            if (part.getContent() instanceof Multipart nested) collect(nested, names);
            else if (part.getFileName() != null) names.add(part.getFileName());
        }
    }
}
