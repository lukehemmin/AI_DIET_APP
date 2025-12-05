package com.lukehemmin.dodietapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender emailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Async
    public void sendVerificationCode(String to, String code) {
        try {
            log.info("==========================================");
            log.info("SENDING EMAIL to: {}", to);
            log.info("Verification Code: {}", code);
            log.info("==========================================");

            if (emailSender == null || fromEmail == null || fromEmail.isEmpty()) {
                log.info("==========================================");
                log.info("MOCK EMAIL SEND to: {}", to);
                log.info("Verification Code: {}", code);
                log.info("==========================================");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("AI Diet App - 이메일 인증 코드");
            message.setText("인증 코드: " + code + "\n\n5분 내에 입력해주세요.");
            emailSender.send(message);
            log.info("Verification email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email", e);
            // Fallback to log for development convenience
            log.info("==========================================");
            log.info("FALLBACK: Verification Code for {}: {}", to, code);
            log.info("==========================================");
        }
    }
}
