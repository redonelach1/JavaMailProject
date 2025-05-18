package com.emailclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.MessagingException;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.models.EmailMessage;
public class EmailManager {
    private static EmailManager instance;
    private List<EmailMessage> emailMessages;
    private List<String> folders;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private final Map<String,List<EmailMessage>> senderBuckets = new HashMap<>();

    private EmailManager() {
        this.emailMessages = new ArrayList<>();
        this.folders = new ArrayList<>();
        // Initialize default folders
        folders.add("INBOX");
        folders.add("SENT");
        folders.add("DRAFT");
        folders.add("TRASH");
        folders.add("ARCHIVED"); // Added ARCHIVED folder
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
        findEmailById(messageId).ifPresent(email -> {
            email.moveToFolder(folder);
            // If moving to ARCHIVED, save to local archive
            if (folder.equals("ARCHIVED")) {
                EmailArchiveService.archiveEmailMessage(email);
                // Optionally remove from in-memory list after archiving and moving
                // emailMessages.remove(email);
            } else if (email.isArchived() && !folder.equals("TRASH")) {
                // If moving from ARCHIVED to another folder (not TRASH), remove from local archive
                // This part would require implementing a deleteFromArchive method in EmailArchiveService
            }
        });
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
        String q = query.toLowerCase();
        return emailMessages.stream()
           .filter(email -> {
               boolean matchesText =  (email.getSubject() != null && email.getSubject().toLowerCase().contains(q))
                                   || (email.getSender()  != null && email.getSender().toLowerCase().contains(q))
                                   || (email.getBody()    != null && email.getBody().toLowerCase().contains(q));
               // ← new: match date
               String dateStr = DATE_FMT.format(email.getReceivedDate());
               boolean matchesDate = dateStr.contains(q);
               return matchesText || matchesDate;
           })
           .collect(Collectors.toList());
    }

    // do the same in searchEmailsInFolder:
    public List<EmailMessage> searchEmailsInFolder(String folder, String query) {
        String q = query.toLowerCase();
        return emailMessages.stream()
           .filter(email -> email.getFolder().equals(folder))
           .filter(email -> {
               boolean matchesText =  (email.getSubject() != null && email.getSubject().toLowerCase().contains(q))
                                   || (email.getSender()  != null && email.getSender().toLowerCase().contains(q))
                                   || (email.getBody()    != null && email.getBody().toLowerCase().contains(q));
               String dateStr = DATE_FMT.format(email.getReceivedDate());
               boolean matchesDate = dateStr.contains(q);
               return matchesText || matchesDate;
           })
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
  /*  public void addMessage(Message message) throws MessagingException {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setMessageId(message.getHeader("Message-ID")[0]);
        emailMessage.setSubject(message.getSubject());
        emailMessage.setSender(message.getFrom()[0].toString());
        emailMessage.setReceivedDate(message.getReceivedDate());
        // Add more message properties as needed
        emailMessages.add(emailMessage);
    }
    */
    public void addMessage(Message message) throws MessagingException {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setMessageId(message.getHeader("Message-ID")[0]);
        emailMessage.setSubject(message.getSubject());
        emailMessage.setSender(message.getFrom()[0].toString());
        emailMessage.setReceivedDate(message.getReceivedDate());

        // ← NEW: extract the body (handles multipart)
        try {
            String text = MailUtils.extractText(message);
            emailMessage.setBody(text);
        } catch (IOException io) {
            emailMessage.setBody("");           // fallback to empty on error
        }
        // plus de propriétés si besoin
        emailMessages.add(emailMessage);
        
        // 1) on l'ajoute à la liste principale (INBOX)

        // 2) on calcule la clé bucket (partie avant le @, minuscules)
        String rawSender = emailMessage.getSender();
        String key;

        if (rawSender == null || rawSender.trim().isEmpty()) {
            key = "Unknown Sender"; // Use a clearer name for unknown senders
        } else {
            // Attempt to extract the email address from the sender string
            int angleBracketStart = rawSender.lastIndexOf('<');
            int angleBracketEnd = rawSender.lastIndexOf('>');

            if (angleBracketStart != -1 && angleBracketEnd != -1 && angleBracketEnd > angleBracketStart) {
                // Sender format: "Display Name <email@domain.com>"
                key = rawSender.substring(angleBracketStart + 1, angleBracketEnd).trim().toLowerCase();
            } else if (rawSender.contains("@")) {
                // Sender format: "email@domain.com"
                key = rawSender.trim().toLowerCase();
            } else {
                // Fallback for unparseable or unusual formats
                key = "Other Senders"; // Group unparseable senders
            }
        }

        // 3) on crée le bucket si besoin
        senderBuckets.computeIfAbsent(key, k -> {
            // ajoute aussi le dossier virtuel à la liste
            // Only add to folders list if it's a new, non-standard folder
            if (!folders.contains(k) && !isDefaultFolder(k)) {
                folders.add(k);
            }
            return new ArrayList<>();
        }).add(emailMessage);
    }
    public void addEmailMessage(EmailMessage email) {
        emailMessages.add(email);
    }
    public List<EmailMessage> getEmailsBySenderBucket(String senderKey) {
        return senderBuckets.getOrDefault(senderKey, List.of());
    }

    // New method to get emails from the local archive
    public List<EmailMessage> getArchivedEmails() {
        return EmailArchiveService.loadArchivedEmails();
    }
} 