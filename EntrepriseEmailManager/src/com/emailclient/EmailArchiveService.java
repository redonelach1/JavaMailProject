package com.emailclient;

import com.models.SentEmail;
import com.models.EmailMessage;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class EmailArchiveService {
    private static final String SENT_ARCHIVE_DIR = "sent_emails";
    private static final String GENERAL_ARCHIVE_DIR = "archived_emails";
    
    public static void saveSentEmail(String subject, String body, List<String> recipients, 
                                   List<String> attachmentPaths) {
        try {
            // Create archive directory if it doesn't exist
            Path archivePath = Paths.get(SENT_ARCHIVE_DIR);
            if (!Files.exists(archivePath)) {
                Files.createDirectories(archivePath);
            }

            // Create SentEmail object
            SentEmail sentEmail = new SentEmail(
                subject,
                body,
                recipients,
                new Date(),
                attachmentPaths
            );

            // Generate unique filename based on timestamp
            String filename = String.format("sent_email_%d.xml", System.currentTimeMillis());
            File outputFile = new File(SENT_ARCHIVE_DIR, filename);

            // Create JAXB context and marshaller
            JAXBContext context = JAXBContext.newInstance(SentEmail.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshal the object to XML file
            marshaller.marshal(sentEmail, outputFile);
            
            System.out.println("Sent email archived successfully: " + outputFile.getAbsolutePath());
        } catch (JAXBException | IOException e) {
            System.err.println("Error archiving sent email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void archiveEmailMessage(EmailMessage email) {
        try {
            // Create archive directory if it doesn't exist
            Path archivePath = Paths.get(GENERAL_ARCHIVE_DIR);
            if (!Files.exists(archivePath)) {
                Files.createDirectories(archivePath);
            }

            // Generate unique filename based on message ID or timestamp
            String filename = String.format("email_%s.xml", email.getMessageId() != null ? email.getMessageId().replaceAll("[^a-zA-Z0-9]", "_") : System.currentTimeMillis());
            File outputFile = new File(GENERAL_ARCHIVE_DIR, filename);

            // Create JAXB context and marshaller for EmailMessage
            JAXBContext context = JAXBContext.newInstance(EmailMessage.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshal the object to XML file
            marshaller.marshal(email, outputFile);

            System.out.println("Email archived successfully: " + outputFile.getAbsolutePath());
        } catch (JAXBException | IOException e) {
            System.err.println("Error archiving email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<EmailMessage> loadArchivedEmails() {
        List<EmailMessage> archivedEmails = new ArrayList<>();
        File archiveDir = new File(GENERAL_ARCHIVE_DIR);
        
        if (!archiveDir.exists() || !archiveDir.isDirectory()) {
            return archivedEmails;
        }

        File[] archiveFiles = archiveDir.listFiles((dir, name) -> name.endsWith(".xml"));
        if (archiveFiles == null) {
            return archivedEmails;
        }

        try {
            JAXBContext context = JAXBContext.newInstance(EmailMessage.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            for (File file : archiveFiles) {
                EmailMessage emailMessage = (EmailMessage) unmarshaller.unmarshal(file);
                // Ensure archived flag is set when loading from archive
                emailMessage.setArchived(true);
                emailMessage.setFolder("ARCHIVED"); // Set folder to ARCHIVED when loaded
                archivedEmails.add(emailMessage);
            }
        } catch (JAXBException e) {
            System.err.println("Error loading archived emails: " + e.getMessage());
            e.printStackTrace();
        }

        return archivedEmails;
    }

    public static List<EmailMessage> loadSentEmails(String userEmail) {
        List<EmailMessage> sentEmails = new ArrayList<>();
        File archiveDir = new File(SENT_ARCHIVE_DIR);

        if (!archiveDir.exists() || !archiveDir.isDirectory()) {
            return sentEmails;
        }

        File[] archiveFiles = archiveDir.listFiles((dir, name) -> name.endsWith(".xml"));
        if (archiveFiles == null) {
            return sentEmails;
        }

        try {
            JAXBContext context = JAXBContext.newInstance(SentEmail.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            for (File file : archiveFiles) {
                try {
                    SentEmail sentEmail = (SentEmail) unmarshaller.unmarshal(file);
                    // Load all sent emails and set the sender to the current user's email
                    EmailMessage emailMessage = new EmailMessage();
                    // Populate EmailMessage from SentEmail
                    emailMessage.setSubject(sentEmail.getSubject());
                    emailMessage.setBody(sentEmail.getBody());
                    emailMessage.setSender(userEmail); // Set sender to the current user's email
                    emailMessage.setRecipients(sentEmail.getRecipients());
                    emailMessage.setReceivedDate(sentEmail.getSentDate()); // Using sent date as received date for sent emails
                    emailMessage.setAttachmentPaths(sentEmail.getAttachmentPaths());
                    emailMessage.setFolder("SENT"); // Set folder to SENT
                    sentEmails.add(emailMessage);

                } catch (JAXBException e) {
                    System.err.println("Error unmarshalling sent email file: " + file.getName() + ": " + e.getMessage());
                    // Continue loading other files
                }
            }
        } catch (JAXBException e) {
            System.err.println("Error loading sent emails from archive: " + e.getMessage());
            e.printStackTrace();
        }

        return sentEmails;
    }

    public static void cleanupTemporaryArchive() throws IOException {
        Path archivePath = Paths.get(GENERAL_ARCHIVE_DIR);
        if (Files.exists(archivePath)) {
            Files.walk(archivePath)
                 .sorted((a, b) -> b.compareTo(a)) // Process directories last
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (IOException e) {
                         System.err.println("Error deleting temporary archive file/directory: " + path + ": " + e.getMessage());
                         e.printStackTrace();
                     }
                 });
            System.out.println("Temporary archive directory cleaned up.");
        }
    }
} 