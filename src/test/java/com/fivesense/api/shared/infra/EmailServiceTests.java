package com.fivesense.api.shared.infra;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.file.Files;
import java.nio.file.Path;

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

    @Test void sendsAttachmentAndReturnsSuccess() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mime = new JavaMailSenderImpl().createMimeMessage();
        when(sender.createMimeMessage()).thenReturn(mime);
        EmailService service = new EmailService(sender, "noreply@example.com");
        Path file = Files.createTempFile("fivesense-attach", ".png");
        try {
            Files.writeString(file, "image-bytes");
            assertThat(service.sendWithAttachment("user@example.com", "subject", "body", file, "foto.png")).isTrue();
            verify(sender).send(mime);
            assertThat(mime.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
            assertThat(mime.getSubject()).isEqualTo("subject");
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
            assertThat(service.sendWithAttachment("user@example.com", "subject", "body", file, "foto.png")).isFalse();
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
