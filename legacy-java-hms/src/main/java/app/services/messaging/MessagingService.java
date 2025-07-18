package app.services.messaging;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.models.Subject;
import app.models.patient.Patient;
import app.services.system.SystemService;
import app.services.templating.TemplateService;
import app.util.ElementBuilder;
import app.util.LocaleUtil;
import app.util.UrlUtils;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

@ServiceDescriptor
public final class MessagingService extends ServiceImpl {
    private final Mailer mailer;
    private final Session session;
    private final String fromName, fromAddress;

    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public MessagingService(Configuration configuration) {
        super(configuration);

        mailer = MailerBuilder
                .withDebugLogging(configuration.DebugMode)
                .withSMTPServer(configuration.SmtpHost, configuration.SmtpPort, configuration.SmtpUser, configuration.SmtpPassword)
                .withTransportStrategy(configuration.SmtpTransport)
                .async() // Do not wait for messages,
                .buildMailer();
        fromName = configuration.EmailSender;
        fromAddress = configuration.SmtpUser;

        Properties properties = new Properties();
        properties.put("mail.smtp.host", configuration.SmtpHost);
        properties.put("mail.smtp.port", configuration.SmtpPort);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.debug", configuration.DebugMode);

        session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromAddress, configuration.SmtpPassword);
            }
        });
    }

    public void sendEmail(String recipientEmail, String recipientName, String subject, String template, Map<String, Object> model) {
        sendEmail(
                recipientEmail,
                recipientName,
                subject,
                getService(TemplateService.class).render("emails/" + template, model),
                true
        );
    }

    public void sendEmail(String recipientEmail, String recipientName, String subject, String message, boolean isHtml) {
        if (!LocaleUtil.isNullOrEmpty(recipientEmail)) {
            try {
                Message mimeMessage = new MimeMessage(session);
                mimeMessage.setFrom(new InternetAddress(fromAddress, fromName));

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail, recipientName));

                mimeMessage.setSubject(subject);
                mimeMessage.setContent(message, String.join("; ", String.join("/", "text", isHtml ? "html" : "plain"), "charset=utf-8"));

                Transport.send(mimeMessage);
            } catch (Exception ex) {
                getLogger().error("Mailer: Could not end email to '{}'<{}>: ", recipientName, recipientEmail, ex);
                final Email email;
                final EmailPopulatingBuilder builder = EmailBuilder.startingBlank()
                        .to(recipientName, recipientEmail)
                        .from(fromName, fromAddress)
                        .withSubject(subject);

                if (isHtml) {
                    builder.withHTMLText(message);
                } else {
                    builder.withPlainText(message);
                }
                email = builder.buildEmail();
                try {
                    // send email in async mode
                    mailer.sendMail(email);
                } catch (Exception e) {
                    getLogger().error("Error sending email to '{}'<{}>: ", recipientName, recipientEmail, e);
                }
            }
        } else {
            getLogger().warn("Empty email address supplied. Email not sent");
        }
    }

    public void sendPatientAccountSetupInstructions(Patient account) {
        String message;
        String subject;
        String setupLink;

        setupLink = UrlUtils.make(getService(SystemService.class).getWebsiteBaseUrl(), "Auth/ResetPassword");

        subject = "Hospital Information Management System: Account Created";

        message = new ElementBuilder("div")
                .addChild(
                        new ElementBuilder("p")
                                .text("Dear " + account.getFullName() + ",")
                ).addChild(
                        new ElementBuilder("p")
                                .text("This email address was used to create an account on our " +
                                        "Hospital Information Management System. Ignore this email " +
                                        "if you cannot identify it, otherwise ")
                                .addChild(new ElementBuilder
                                        .Anchor()
                                        .href(setupLink)
                                        .text("click here")
                                ).addChild(
                                new ElementBuilder("span")
                                        .text(" to setup your account's password and login.")
                        )
                )
                .addChild(
                        new ElementBuilder("p")
                                .text("Your Medical Records Number (MRN) or patient ID is ")
                                .addChild(
                                        new ElementBuilder("strong")
                                                .text(account.getMrn())
                                )
                )
                .build();

        sendEmail(
                account.getEmail(),
                account.getFullName(),
                subject,
                message,
                true
        );
    }

    public void sendAccountSetupInstructions(Subject account) {
        String message;
        String subject;
        String setupLink;

        setupLink = UrlUtils.make(getService(SystemService.class).getWebsiteBaseUrl(), "Auth/ResetPassword");

        subject = "Hospital Information Management System: Account Created";

        message = new ElementBuilder("div")
                .addChild(
                        new ElementBuilder("p")
                                .text("Dear " + account.getFullName() + ",")
                ).addChild(
                        new ElementBuilder("p")
                                .text("This email address was used to create an account on our " +
                                        "Hospital Information Management System. Ignore this email " +
                                        "if you cannot identify it, otherwise ")
                                .addChild(new ElementBuilder
                                        .Anchor()
                                        .href(setupLink)
                                        .text("click here")
                                ).addChild(
                                new ElementBuilder("span")
                                        .text(" to setup your account's password and login.")
                        )
                ).build();

        sendEmail(
                account.getEmail(),
                account.getFullName(),
                subject,
                message,
                true
        );
    }
}
