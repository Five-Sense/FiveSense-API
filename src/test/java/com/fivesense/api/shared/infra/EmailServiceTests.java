package com.fivesense.api.shared.infra;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

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
}
