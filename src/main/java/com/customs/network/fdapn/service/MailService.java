package com.customs.network.fdapn.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MailService {
    private final JavaMailSender javaMailSender;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmailWithAttachment(byte[] data, List<String> recipients) {
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;

        while (!success) {
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                String[] recipientArray = recipients.toArray(new String[0]);
                log.info("Sending mails to {} ", recipients);
                helper.setTo(recipientArray);
                helper.setSubject("Failures Report");

                String emailContent = "<html><body>"
                        + "<p>Dear Customer,</p>"
                        + "<p>Please find attached the failures report for your recent submission.</p>"
                        + "<p>We kindly ask you to review the errors and take necessary actions to correct them.</p>"
                        + "<p>If you have any questions or need further assistance, please do not hesitate to contact us at:</p>"
                        + "<p>Email: info@seabed2crest.com</p>"
                        + "<p>Phone: +91 7349368311</p>"
                        + "<br><p>Thank you for your attention to this matter.</p>"
                        + "<img src='cid:logo' style='width:130px;height:100px;'><br><br>"
                        + "<p>Best regards,<br>Customs Network</p>"
                        + "</body></html>";
                helper.setText(emailContent, true);

                ClassPathResource logoResource = new ClassPathResource("assets/seabed2crest-logo.png");
                helper.addInline("logo", logoResource, "assets/seabed2crest-logo.png");
                ByteArrayResource file = new ByteArrayResource(data);
                helper.addAttachment("ValidationFailuresReport.xlsx", file);

                javaMailSender.send(message);
                log.info("Sent mail successfully");
                success = true;
            } catch (MessagingException e) {
                log.error("Failed to send mail, retrying...");
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.error("Maximum retries reached, aborting.");
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

