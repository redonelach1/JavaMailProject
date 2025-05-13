package com.emailclient;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailSender {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private static Session emailSession;

    private static synchronized Session getSession() {
        if (emailSession == null) {
            String username = EmailSessionManager.getUsername();
            String password = EmailSessionManager.getPassword();

            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.connectiontimeout", "5000");
            prop.put("mail.smtp.timeout", "5000");

            emailSession = Session.getInstance(prop, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }
        return emailSession;
    }

    public static void sendEmailWithAttachment(String to, String subject, String body, File[] attachments) {
        try {
            Session session = getSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EmailSessionManager.getUsername()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);
            multipart.addBodyPart(textPart);

            List<String> attachmentPaths = new ArrayList<>();
            for (File file : attachments) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);
                multipart.addBodyPart(attachmentPart);
                attachmentPaths.add(file.getAbsolutePath());
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Email sent successfully with attachments.");

            // Save a copy of the sent email asynchronously
            List<String> recipients = Arrays.asList(to.split(","));
            executorService.submit(() -> {
                try {
                    EmailArchiveService.saveSentEmail(subject, body, recipients, attachmentPaths);
                } catch (Exception e) {
                    System.err.println("Error archiving email: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public static void shutdown() {
        executorService.shutdown();
    }
}