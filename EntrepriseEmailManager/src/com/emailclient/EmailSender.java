package com.emailclient;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmailSender {
  public static void sendEmailWithAttachment(String to, String subject, String body, File[] attachments) {
      try {
          String username = EmailSessionManager.getUsername();
          String password = EmailSessionManager.getPassword();

          Properties prop = new Properties();
          prop.put("mail.smtp.host", "smtp.gmail.com");
          prop.put("mail.smtp.port", "587");
          prop.put("mail.smtp.auth", "true");
          prop.put("mail.smtp.starttls.enable", "true");

          Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(username, password);
              }
          });

          try {
              Message message = new MimeMessage(session);
              message.setFrom(new InternetAddress(username));
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

              // Save a copy of the sent email
              List<String> recipients = Arrays.asList(to.split(","));
              EmailArchiveService.saveSentEmail(subject, body, recipients, attachmentPaths);

          } catch (Exception e) {
              e.printStackTrace();
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}