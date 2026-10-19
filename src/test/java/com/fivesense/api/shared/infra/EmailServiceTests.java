package com.fivesense.api.shared.infra;

import com.fivesense.api.TestTemplates;
import com.fivesense.api.shared.config.AsyncConfiguration;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.TemplateEngine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTests {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};

    private static EmailService.HtmlMessage message(Map<String, String> details, List<EmailService.InlineImage> images) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("title", "Nova ocorrência: Vassoura");
        model.put("message", "Uma ocorrência foi registrada.");
        model.put("details", details);
        return new EmailService.HtmlMessage("user@example.com", "Assunto", "texto simples", "email/occurrence", model, images);
    }

    private static MimeMessage mime(JavaMailSender sender) {
        MimeMessage mime = new JavaMailSenderImpl().createMimeMessage();
        when(sender.createMimeMessage()).thenReturn(mime);
        return mime;
    }

    @Test void sendsConfiguredPlainMessage() {
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailService service = new EmailService(sender, TestTemplates.engine(), "noreply@example.com");

        service.send("user@example.com", "subject", "body");

        var message = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(message.capture());
        assertThat(message.getValue().getFrom()).isEqualTo("noreply@example.com");
        assertThat(message.getValue().getTo()).containsExactly("user@example.com");
        assertThat(message.getValue().getSubject()).isEqualTo("subject");
        assertThat(message.getValue().getText()).isEqualTo("body");
    }

    @Test void plainMessageFailureIsSwallowed() {
        JavaMailSender sender = mock(JavaMailSender.class);
        doThrow(new IllegalStateException("smtp unavailable")).when(sender).send(any(SimpleMailMessage.class));
        EmailService service = new EmailService(sender, TestTemplates.engine(), "");

        assertThatCode(() -> service.send("user@example.com", "subject", "body")).doesNotThrowAnyException();
        verify(sender).send(any(SimpleMailMessage.class));
    }

    @Test void rendersThymeleafTemplateWithInlineImagesAndPlainTextFallback() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mime = mime(sender);
        EmailService service = new EmailService(sender, TestTemplates.engine(), "noreply@example.com");
        var details = new LinkedHashMap<String, String>();
        details.put("Problema", "Vassoura");
        details.put("Quantidade afetada", "3");

        service.sendHtml(message(details, List.of(
                new EmailService.InlineImage("image1", "foto.png", "image/png", PNG),
                new EmailService.InlineImage("image2", "outra.png", "image/png", PNG))));

        verify(sender).send(mime);
        mime.saveChanges();
        assertThat(mime.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
        assertThat(mime.getSubject()).isEqualTo("Assunto");
        assertThat(mime.getFrom()[0].toString()).isEqualTo("noreply@example.com");
        List<BodyPart> parts = leafParts(mime);
        String html = parts.stream().filter(p -> isType(p, "text/html")).map(EmailServiceTests::text).findFirst().orElseThrow();
        assertThat(html).contains("Nova ocorrência: Vassoura").contains("Uma ocorrência foi registrada.")
                .contains("Quantidade afetada").contains(">3<").contains("src=\"cid:image1\"").contains("src=\"cid:image2\"")
                .contains("Imagens enviadas com a ocorrência");
        assertThat(parts.stream().filter(p -> isType(p, "text/plain")).map(EmailServiceTests::text).findFirst().orElseThrow()).isEqualTo("texto simples");
        var inline = parts.stream().filter(p -> isType(p, "image/png")).toList();
        assertThat(inline).hasSize(2);
        assertThat(inline.get(0).getHeader("Content-ID")[0]).isEqualTo("<image1>");
        assertThat(inline.get(0).getDisposition()).isEqualTo("inline");
        assertThat(inline.get(0).getInputStream().readAllBytes()).isEqualTo(PNG);
    }

    @Test void messageWithoutImagesHasNoImageTagAndEscapesHtmlInValues() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mime = mime(sender);
        EmailService service = new EmailService(sender, TestTemplates.engine(), "");

        service.sendHtml(message(Map.of("Problema", "<script>alert(1)</script>"), List.of()));

        mime.saveChanges();
        String html = leafParts(mime).stream().filter(p -> isType(p, "text/html")).map(EmailServiceTests::text).findFirst().orElseThrow();
        assertThat(html).doesNotContain("<img").doesNotContain("<script>").contains("&lt;script&gt;");
    }

    @Test void htmlMessageFailureIsSwallowed() {
        JavaMailSender sender = mock(JavaMailSender.class);
        mime(sender);
        doThrow(new IllegalStateException("smtp unavailable")).when(sender).send(any(MimeMessage.class));
        EmailService service = new EmailService(sender, TestTemplates.engine(), "");

        assertThatCode(() -> service.sendHtml(message(Map.of(), List.of()))).doesNotThrowAnyException();
        verify(sender).send(any(MimeMessage.class));
    }

    @Test void unknownTemplateFailureIsSwallowedAndNothingIsSent() {
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailService service = new EmailService(sender, TestTemplates.engine(), "");
        var broken = new EmailService.HtmlMessage("user@example.com", "s", "t", "email/does-not-exist", Map.of(), List.of());

        assertThatCode(() -> service.sendHtml(broken)).doesNotThrowAnyException();
        verify(sender, never()).send(any(MimeMessage.class));
    }

    @Test void sendMethodsAreAsyncOnTheDedicatedMailExecutor() throws Exception {
        for (var method : List.of(EmailService.class.getMethod("send", String.class, String.class, String.class),
                EmailService.class.getMethod("sendHtml", EmailService.HtmlMessage.class))) {
            assertThat(method.getAnnotation(Async.class)).as(method.getName()).isNotNull();
            assertThat(method.getAnnotation(Async.class).value()).isEqualTo(AsyncConfiguration.MAIL_EXECUTOR);
        }
    }

    @Test void callerIsNotBlockedWhenTheMailServerIsSlowOrUnreachable() throws Exception {
        CountDownLatch smtpStarted = new CountDownLatch(1);
        CountDownLatch releaseSmtp = new CountDownLatch(1);
        AtomicReference<String> smtpThread = new AtomicReference<>();
        JavaMailSender sender = mock(JavaMailSender.class);
        doAnswer(inv -> {
            smtpThread.set(Thread.currentThread().getName());
            smtpStarted.countDown();
            releaseSmtp.await(10, TimeUnit.SECONDS);
            return null;
        }).when(sender).send(any(SimpleMailMessage.class));

        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(AsyncConfiguration.class, EmailService.class);
            context.registerBean(JavaMailSender.class, () -> sender);
            context.registerBean(TemplateEngine.class, TestTemplates::engine);
            context.refresh();

            long begin = System.nanoTime();
            context.getBean(EmailService.class).send("user@example.com", "subject", "body");
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin);

            assertThat(smtpStarted.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(elapsedMillis).as("send() returns while the SMTP call is still blocked").isLessThan(2000);
            assertThat(smtpThread.get()).startsWith("mail-").isNotEqualTo(Thread.currentThread().getName());
            releaseSmtp.countDown();
        } finally {
            releaseSmtp.countDown();
        }
    }

    private static boolean isType(BodyPart part, String type) {
        try { return part.isMimeType(type); } catch (Exception ex) { throw new IllegalStateException(ex); }
    }

    private static String text(BodyPart part) {
        try { return (String) part.getContent(); } catch (Exception ex) { throw new IllegalStateException(ex); }
    }

    /** Every non-multipart body part of the message, searching nested multiparts. */
    private static List<BodyPart> leafParts(MimeMessage mime) throws Exception {
        List<BodyPart> parts = new ArrayList<>();
        collect((Multipart) mime.getContent(), parts);
        return parts;
    }

    private static void collect(Multipart multipart, List<BodyPart> parts) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (part.getContent() instanceof Multipart nested) collect(nested, parts);
            else parts.add(part);
        }
    }
}
