package com.emailclient;

import javax.mail.Message;
import javax.mail.MessagingException;
import com.models.EmailMessage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

public class EmailReceiver {
    public static List<EmailMessage> receiveEmail() throws MessagingException {
        EmailSessionManager manager = EmailSessionManager.getInstance();
        Message[] messages = manager.receiveEmail();
        
        // Convert messages to EmailMessage objects and add to EmailManager
        EmailManager emailManager = EmailManager.getInstance();
        for (Message message : messages) {
            emailManager.addMessage(message);
        }
        
        // Return all messages from INBOX
        return emailManager.getEmailsInFolder("INBOX");
    }

    public static List<EmailMessage> getUnreadEmails() {
        return EmailManager.getInstance().getUnreadEmails();
    }

    public static List<EmailMessage> getEmailsInFolder(String folder) {
        return EmailManager.getInstance().getEmailsInFolder(folder);
    }

    public static void markAsRead(String messageId) {
        EmailManager.getInstance().markAsRead(messageId);
    }

    public static void markAsUnread(String messageId) {
        EmailManager.getInstance().markAsUnread(messageId);
    }

    public static void moveToFolder(String messageId, String folder) {
        EmailManager.getInstance().moveToFolder(messageId, folder);
    }

    public static void deleteEmail(String messageId) {
        EmailManager.getInstance().deleteEmail(messageId);
    }

    public static void restoreEmail(String messageId) {
        EmailManager.getInstance().restoreEmail(messageId);
    }
}