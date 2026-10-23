package com.fivesense.api.shared.infra;

import com.fivesense.api.shared.config.AsyncConfiguration;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sends e-mail. Every public method runs on the {@code mailExecutor} thread pool, never on the caller's
 * thread, and is best effort: a delivery failure is logged (without addresses or content) and never thrown.
 * HTML messages are rendered with Thymeleaf from {@code templates/email/*.html}; images are embedded in the
 * message body (inline, referenced as {@code cid:<contentId>}) and are never persisted by this service.
 */
@Service
public class EmailService {
    private static final Logger log=LoggerFactory.getLogger(EmailService.class);
    private static final Locale LOCALE=Locale.forLanguageTag("pt-BR");
    private final JavaMailSender mailSender;
    private final TemplateEngine templates;
    private final String from;
    public EmailService(JavaMailSender mailSender,TemplateEngine templates,@Value("${spring.mail.username:}") String from){this.mailSender=mailSender;this.templates=templates;this.from=from;}

    /** An image embedded in an HTML message; {@code contentId} is what the template uses in {@code cid:}. */
    public record InlineImage(String contentId,String name,String contentType,byte[] content){}

    /** An HTML message: {@code template} is rendered with {@code model} plus the {@code images} list; {@code plainText} is the fallback part. */
    public record HtmlMessage(String recipient,String subject,String plainText,String template,Map<String,Object> model,List<InlineImage> images){}

    @Async(AsyncConfiguration.MAIL_EXECUTOR)
    public void send(String recipient,String subject,String body){
        try { var message=new SimpleMailMessage(); if(!from.isBlank())message.setFrom(from); message.setTo(recipient); message.setSubject(subject); message.setText(body); mailSender.send(message); }
        catch (RuntimeException ex){log.warn("Email delivery failed: {}",rootCause(ex));}
    }

    @Async(AsyncConfiguration.MAIL_EXECUTOR)
    public void sendHtml(HtmlMessage request){
        log.info("Preparing occurrence e-mail to {} (subject: {})",maskRecipient(request.recipient()),request.subject());
        try {
            Map<String,Object> model=new HashMap<>(request.model());
            model.put("images",request.images());
            Context context=new Context(LOCALE,model);
            String html=templates.process(request.template(),context);
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message,true,StandardCharsets.UTF_8.name());
            if(!from.isBlank())helper.setFrom(from);
            helper.setTo(request.recipient());
            helper.setSubject(request.subject());
            helper.setText(request.plainText(),html);
            for(InlineImage image:request.images())helper.addInline(image.contentId(),new ByteArrayResource(image.content()),image.contentType());
            mailSender.send(message);
            log.info("Occurrence e-mail sent to {}",maskRecipient(request.recipient()));
        } catch (Exception ex){log.warn("Email delivery failed to {}: {}",maskRecipient(request.recipient()),rootCause(ex),ex);}
    }

    /** Logs only the recipient domain, never the full address, to avoid leaking PII. */
    private static String maskRecipient(String recipient){
        if(recipient==null||recipient.isBlank())return "<null>";
        int at=recipient.indexOf('@');
        return at>0?"***"+recipient.substring(at):"***";
    }

    /** Only the exception type is logged: messages can carry addresses or message content. */
    private static String rootCause(Throwable ex){
        Throwable cause=ex;
        while(cause.getCause()!=null&&cause.getCause()!=cause)cause=cause.getCause();
        return cause.getClass().getSimpleName();
    }
}
