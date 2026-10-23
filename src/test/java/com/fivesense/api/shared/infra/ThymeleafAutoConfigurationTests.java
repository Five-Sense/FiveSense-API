package com.fivesense.api.shared.infra;

import org.junit.jupiter.api.Test;
import com.fivesense.api.shared.config.AsyncConfiguration;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Guards the production wiring: the engine Spring Boot auto-configures must find and render the e-mail template. */
class ThymeleafAutoConfigurationTests {
    /** Production-like wiring (Boot mail + Thymeleaf + @Async) against an SMTP host that never answers: the caller must not wait. */
    @Test void emailServiceIsAsyncAndDoesNotBlockTheCallerWhenSmtpIsUnreachable() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ThymeleafAutoConfiguration.class, MailSenderAutoConfiguration.class))
                .withUserConfiguration(AsyncConfiguration.class, EmailService.class)
                .withPropertyValues("spring.mail.host=10.255.255.1", "spring.mail.port=25",
                        "spring.mail.properties.mail.smtp.connectiontimeout=500", "spring.mail.properties.mail.smtp.timeout=500")
                .run(context -> {
                    var service = context.getBean(EmailService.class);
                    assertThat(AopUtils.isAopProxy(service)).as("@Async proxy").isTrue();
                    long begin = System.nanoTime();
                    service.sendHtml(new EmailService.HtmlMessage("user@example.com", "s", "t", "email/occurrence",
                            Map.of("title", "T", "message", "M", "details", Map.of()), List.of()));
                    service.send("user@example.com", "s", "b");
                    assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin)).isLessThan(400);
                });
    }

    @Test void bootConfiguredEngineRendersTheOccurrenceEmailTemplate() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ThymeleafAutoConfiguration.class)).run(context -> {
            assertThat(context).hasSingleBean(TemplateEngine.class);
            var image = new EmailService.InlineImage("image1", "foto.png", "image/png", new byte[]{1});
            String html = context.getBean(TemplateEngine.class).process("email/occurrence",
                    new Context(Locale.forLanguageTag("pt-BR"), Map.of("title", "Título", "message", "Mensagem", "details", Map.of("Campo", "Valor"), "images", List.of(image))));
            assertThat(html).contains("Título").contains("Mensagem").contains("Campo").contains("Valor").contains("src=\"cid:image1\"");
        });
    }
}
