package registration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

@ApplicationScoped
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final String SMTP_USER = "your-email@example.com";
    private final String SMTP_PASSWORD = System.getenv("SMTP_PASSWORD");

    public void sendEmail(String recipient, String subject, String body) {
        Properties properties = new Properties();
        String SMTP_HOST = "smtp.example.com";
        properties.put("mail.smtp.host", SMTP_HOST);
        String SMTP_PORT = "587";
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            logger.info("Email sent successfully to: {}", recipient);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", recipient, e.getMessage(), e);
        }
    }
}
