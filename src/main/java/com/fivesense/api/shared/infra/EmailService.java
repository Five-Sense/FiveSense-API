package com.fivesense.api.shared.infra;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class EmailService {
    private static final Logger log=LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final String from;
    public EmailService(JavaMailSender mailSender,@Value("${spring.mail.username:}") String from){this.mailSender=mailSender;this.from=from;}

    public boolean send(String recipient,String subject,String body){
        try { var message=new SimpleMailMessage(); if(!from.isBlank())message.setFrom(from); message.setTo(recipient); message.setSubject(subject); message.setText(body); mailSender.send(message); return true; }
        catch (RuntimeException ex){log.warn("Email delivery failed");return false;}
    }

    /**
     * Sends an e-mail with a single file attachment. Best effort: a delivery failure is logged and
     * reported through the return value, never thrown. The attachment is read from a temporary file
     * and is never persisted by this service.
     */
    public boolean sendWithAttachment(String recipient,String subject,String body,Path attachment,String attachmentName){
        try {
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message,true);
            if(!from.isBlank())helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(attachmentName,new FileSystemResource(attachment));
            mailSender.send(message);
            return true;
        } catch (Exception ex){log.warn("Email delivery with attachment failed");return false;}
    }
}
