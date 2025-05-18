package com.emailclient;

import javax.mail.Message;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public class EmailSessionManager {
    private Session emailSession;
    private Store   store;
    private Folder  emailFolder;

    private static EmailSessionManager instance;
    private static String currentUsername = "";
    private static String currentPassword = "";

    private EmailSessionManager(String username, String password) throws MessagingException {
        connect(username, password);
    }


    public static synchronized EmailSessionManager getInstance(String username,
                                                               String password) throws MessagingException {

        // 1) Premier appel : on crée l’instance normalement
        if (instance == null) {
            instance = new EmailSessionManager(username, password);
            return instance;
        }

        // 2) Appel suivant avec *les mêmes* identifiants → on réutilise
        if (username.equals(currentUsername) && password.equals(currentPassword)) {
            return instance;
        }

        // 3) Identifiants différents → on change de compte !
        instance.switchAccount(username, password);
        return instance;
    }

    public static EmailSessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("EmailSessionManager not initialized. Please login first.");
        }
        return instance;
    }


    public static String getUsername() { return currentUsername; }
    public static String getPassword() { return currentPassword; }


    public Message[] receiveEmail() throws MessagingException {
        if (emailFolder == null || !emailFolder.isOpen()) {
            emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);
        }
        return emailFolder.getMessages();
    }

    public Message[] getMessagesFromFolder(String folderName) throws MessagingException {
        Folder folder = store.getFolder(folderName);
        if (!folder.exists()) throw new MessagingException("Folder " + folderName + " does not exist");
        folder.open(Folder.READ_ONLY);
        Message[] messages = folder.getMessages();
        folder.close(false);
        return messages;
    }


    public void close() throws MessagingException {
        if (emailFolder != null) { emailFolder.close(false); emailFolder = null; }
        if (store != null)       { store.close();           store        = null; }
        currentUsername = "";
        currentPassword = "";
    }


    // (Ré)ouvre le Store avec les identifiants donnés 
    private void connect(String username, String password) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host",     "imap.gmail.com");
        properties.put("mail.imaps.port",     "993");
        properties.put("mail.imaps.ssl.enable","true");

        emailSession = Session.getInstance(properties, null);
        store        = emailSession.getStore("imaps");
        store.connect(username, password);

        currentUsername = username;
        currentPassword = password;
    }

    // Ferme l’ancien compte puis appelle connect() avec le nouveau
    private void switchAccount(String newUser, String newPass) throws MessagingException {
        close();            
        connect(newUser, newPass); 
        System.out.println("Switched to account: " + newUser);
    }
}