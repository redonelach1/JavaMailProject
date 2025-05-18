package com.emailclient;

import com.models.EmailMessage;
import java.util.List;

public class EmailManagementTest {
    public static void main(String[] args) {
        try {
            // Initialize email session
            String username = "redone1lachgar@gmail.com";
            String password = "gcbw qobd kdja rhwa";
            EmailSessionManager.getInstance(username, password);

            // Test receiving emails
            System.out.println("Receiving emails...");
            List<EmailMessage> emails = EmailReceiver.receiveEmail();
            System.out.println("Received " + emails.size() + " emails");

            if (!emails.isEmpty()) {
                EmailMessage firstEmail = emails.get(0);
                String messageId = firstEmail.getMessageId();

                // Test marking as read/unread
                System.out.println("\nTesting read/unread status:");
                EmailReceiver.markAsRead(messageId);
                System.out.println("Email marked as read");
                EmailReceiver.markAsUnread(messageId);
                System.out.println("Email marked as unread");

                // Test folder operations
                System.out.println("\nTesting folder operations:");
                EmailReceiver.moveToFolder(messageId, "IMPORTANT");
                System.out.println("Email moved to IMPORTANT folder");
                
                List<EmailMessage> importantEmails = EmailReceiver.getEmailsInFolder("IMPORTANT");
                System.out.println("Emails in IMPORTANT folder: " + importantEmails.size());

                // Test deletion and restoration
                System.out.println("\nTesting deletion and restoration:");
                EmailReceiver.deleteEmail(messageId);
                System.out.println("Email deleted (moved to TRASH)");
                
                List<EmailMessage> deletedEmails = EmailReceiver.getEmailsInFolder("TRASH");
                System.out.println("Emails in TRASH: " + deletedEmails.size());

                EmailReceiver.restoreEmail(messageId);
                System.out.println("Email restored to INBOX");
                
                List<EmailMessage> inboxEmails = EmailReceiver.getEmailsInFolder("INBOX");
                System.out.println("Emails in INBOX: " + inboxEmails.size());
            }

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                EmailSessionManager.getInstance().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
} 