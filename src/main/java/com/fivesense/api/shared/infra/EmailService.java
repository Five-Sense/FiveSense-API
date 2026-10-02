package com.fivesense.api.shared.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}
