package com.customs.network.fdapn.service;

import com.converter.exceptions.MaxRetriesReachedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MailService {
    private final JavaMailSender javaMailSender;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_SECONDS = 5;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmailWithAttachment(byte[] data, List<String> recipients) {
        int retryCount = 0;
        boolean success = false;

        while (!success && retryCount < MAX_RETRIES) {
            try {
                long start = System.currentTimeMillis();
                MimeMessage message = createEmailMessage(data, recipients);
                javaMailSender.send(message);
                long end = System.currentTimeMillis();
                log.info("Sent mail successfully to recipients {} (took {} milliseconds)",recipients,end-start);
                success = true;
            } catch (MessagingException | MailSendException e) {
                retryCount++;
                handleRetry(retryCount, e);
            }
        }
    }

    private MimeMessage createEmailMessage(byte[] data, List<String> recipients) throws MessagingException {
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

        return message;
    }

    private void handleRetry(int retryCount, Exception e) {
        if (retryCount < MAX_RETRIES) {
            long remainingRetries = MAX_RETRIES - (long) retryCount;
            log.error("Failed to send mail. Retrying in {} seconds... ({} {} remaining)",
                    RETRY_DELAY_SECONDS, remainingRetries,
                    remainingRetries == 1 ? "attempt" : "attempts");
            try {
                TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("Retry delay interrupted", ie);
            }
        } else {
            log.error("Maximum retries reached. Failed to send mail after {} attempts.", MAX_RETRIES);
            throw new MaxRetriesReachedException("Failed to send email after maximum retries: " + e.getMessage());
        }
    }
}

