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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /** A file sent along with an e-mail; {@code name} is the file name shown to the recipient. */
    public record Attachment(Path path,String name){}

    /**
     * Sends an e-mail with one or more file attachments. Best effort: a delivery failure is logged and
     * reported through the return value, never thrown. Attachments are read from temporary files
     * and are never persisted by this service. Duplicate names get a numeric prefix so none is lost.
     */
    public boolean sendWithAttachments(String recipient,String subject,String body,List<Attachment> attachments){
        try {
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message,true);
            if(!from.isBlank())helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body);
            Set<String> used=new HashSet<>();
            for(Attachment attachment:attachments){
                String name=attachment.name();
                for(int copy=2;!used.add(name);copy++)name=copy+"-"+attachment.name();
                helper.addAttachment(name,new FileSystemResource(attachment.path()));
            }
            mailSender.send(message);
            return true;
        } catch (Exception ex){log.warn("Email delivery with attachment failed");return false;}
    }
}
