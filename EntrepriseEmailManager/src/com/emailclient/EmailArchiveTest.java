package com.emailclient;

import java.io.File;

public class EmailArchiveTest {
    public static void main(String[] args) {
        try {
            // Initialize email session (replace with your Gmail credentials)
            String username = "redone1lachgar@gmail.com";
            String password = "gcbw qobd kdja rhwa"; // Use App Password for Gmail
            EmailSessionManager.getInstance(username, password);

            // Test data
            String to = "rednahdawi@gmail.com";
            String subject = "Test Email with Archive";
            String body = "This is a test email to verify the archiving functionality.\n" +
                         "The email should be saved as XML in the sent_emails directory.";
            
            // Create a test attachment
            File testFile = new File("test_attachment.txt");
            if (!testFile.exists()) {
                testFile.createNewFile();
                java.nio.file.Files.write(testFile.toPath(), 
                    "This is a test attachment".getBytes());
            }

            // Send email with attachment
            File[] attachments = new File[]{testFile};
            EmailSender.sendEmailWithAttachment(to, subject, body, attachments);

            // Verify archive directory
            File archiveDir = new File("sent_emails");
            if (archiveDir.exists() && archiveDir.isDirectory()) {
                File[] archiveFiles = archiveDir.listFiles((dir, name) -> name.endsWith(".xml"));
                if (archiveFiles != null && archiveFiles.length > 0) {
                    System.out.println("\nArchive verification:");
                    System.out.println("Found " + archiveFiles.length + " archived email(s)");
                    System.out.println("Latest archive: " + archiveFiles[archiveFiles.length - 1].getName());
                }
            }

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                // Clean up session
                EmailSessionManager.getInstance().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
} 