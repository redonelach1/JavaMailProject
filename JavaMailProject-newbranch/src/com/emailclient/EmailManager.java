package com.emailclient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.models.EmailMessage;
public class EmailManager {
    private static EmailManager instance;
    private List<EmailMessage> emailMessages;
    private List<String> folders;

    private EmailManager() {
        this.emailMessages = new ArrayList<>();
        this.folders = new ArrayList<>();
        // Initialize default folders
        folders.add("INBOX");
        folders.add("SENT");
        folders.add("DRAFT");
        folders.add("TRASH");
    }

    public static EmailManager getInstance() {
        if (instance == null) {
            instance = new EmailManager();
        }
        return instance;
    }

    // Email Operations
    public void markAsRead(String messageId) {
        findEmailById(messageId).ifPresent(EmailMessage::markAsRead);
    }

    public void markAsUnread(String messageId) {
        findEmailById(messageId).ifPresent(EmailMessage::markAsUnread);
    }

    public void moveToFolder(String messageId, String folder) {
        if (!folders.contains(folder)) {
            folders.add(folder);
        }
        findEmailById(messageId).ifPresent(email -> email.moveToFolder(folder));
    }

    public void deleteEmail(String messageId) {
        findEmailById(messageId).ifPresent(email -> {
            email.delete();
            email.moveToFolder("TRASH");
        });
    }

    public void restoreEmail(String messageId) {
        findEmailById(messageId).ifPresent(email -> {
            email.restore();
            email.moveToFolder("INBOX");
        });
    }

    // Folder Operations
    public List<String> getFolders() {
        return new ArrayList<>(folders);
    }

    public void createFolder(String folderName) {
        if (!folders.contains(folderName)) {
            folders.add(folderName);
        }
    }

    public void deleteFolder(String folderName) {
        if (!isDefaultFolder(folderName)) {
            folders.remove(folderName);
            // Move emails from deleted folder to INBOX
            emailMessages.stream()
                .filter(email -> email.getFolder().equals(folderName))
                .forEach(email -> email.moveToFolder("INBOX"));
        }
    }

    // Email Retrieval
    public List<EmailMessage> getEmailsInFolder(String folder) {
        return emailMessages.stream()
            .filter(email -> email.getFolder().equals(folder))
            .collect(Collectors.toList());
    }

    public List<EmailMessage> getUnreadEmails() {
        return emailMessages.stream()
            .filter(email -> !email.isRead())
            .collect(Collectors.toList());
    }

    public List<EmailMessage> getDeletedEmails() {
        return emailMessages.stream()
            .filter(EmailMessage::isDeleted)
            .collect(Collectors.toList());
    }

    public List<EmailMessage> searchEmails(String query) {
        String lowerQuery = query.toLowerCase();
        return emailMessages.stream()
            .filter(email -> 
                (email.getSubject() != null && email.getSubject().toLowerCase().contains(lowerQuery)) ||
                (email.getSender() != null && email.getSender().toLowerCase().contains(lowerQuery)) ||
                (email.getBody() != null && email.getBody().toLowerCase().contains(lowerQuery)))
            .collect(Collectors.toList());
    }

    public List<EmailMessage> searchEmailsInFolder(String folder, String query) {
        String lowerQuery = query.toLowerCase();
        return emailMessages.stream()
            .filter(email -> email.getFolder().equals(folder))
            .filter(email -> 
                (email.getSubject() != null && email.getSubject().toLowerCase().contains(lowerQuery)) ||
                (email.getSender() != null && email.getSender().toLowerCase().contains(lowerQuery)) ||
                (email.getBody() != null && email.getBody().toLowerCase().contains(lowerQuery)))
            .collect(Collectors.toList());
    }

    // Helper Methods
    public java.util.Optional<EmailMessage> findEmailById(String messageId) {
        return emailMessages.stream()
            .filter(email -> email.getMessageId().equals(messageId))
            .findFirst();
    }

    private boolean isDefaultFolder(String folderName) {
        return folderName.equals("INBOX") || 
               folderName.equals("SENT") || 
               folderName.equals("DRAFT") || 
               folderName.equals("TRASH");
    }

    // Message Conversion
    public void addMessage(Message message) throws MessagingException {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setMessageId(message.getHeader("Message-ID")[0]);
        emailMessage.setSubject(message.getSubject());
        emailMessage.setSender(message.getFrom()[0].toString());
        emailMessage.setReceivedDate(message.getReceivedDate());
        // Add more message properties as needed
        emailMessages.add(emailMessage);
    }
} 