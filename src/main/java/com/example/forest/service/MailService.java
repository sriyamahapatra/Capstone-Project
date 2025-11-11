package com.example.forest.service;

import com.example.forest.Exceptions.CustomException;
import com.example.forest.model.NotificationEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final MailContentBuilder mailContentBuilder;

    @Value("${spring.mail.username:no-reply@forest.com}")
    private String senderEmail;

    @Async
    public void sendMail(NotificationEmail notificationEmail) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(notificationEmail.getRecipient());
            helper.setSubject(notificationEmail.getSubject());

            String html = mailContentBuilder.build(
                    notificationEmail.getBody(),
                    notificationEmail.getUsername(),
                    notificationEmail.getRecipient()
            );
            helper.setText(html, true);
        };

        try {
            mailSender.send(messagePreparator);
            log.info("✅ Email sent to {}", notificationEmail.getRecipient());
        } catch (MailException e) {
            log.error("❌ Failed to send email to {}", notificationEmail.getRecipient(), e);
            throw new CustomException("Failed to send email to " + notificationEmail.getRecipient(), e);
        }
    }
}
