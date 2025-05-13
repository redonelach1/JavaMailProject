package com.emailclient;

import javax.mail.Message;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public class EmailSessionManager {
  private Session emailSession;
  private Store store;
  private Folder emailFolder;
  private static EmailSessionManager instance;

  private static String currentUsername = "";
  private static String currentPassword = "";

  private EmailSessionManager(String username, String password) throws MessagingException {
      Properties properties = new Properties();
      properties.put("mail.store.protocol", "imaps");
      properties.put("mail.imaps.host", "imap.gmail.com");
      properties.put("mail.imaps.port", "993");
      properties.put("mail.imaps.ssl.enable", "true");
      this.emailSession = Session.getInstance(properties, null);
      this.store = emailSession.getStore("imaps");
      this.store.connect(username, password);

      currentUsername = username;
      currentPassword = password;
  }

  public static EmailSessionManager getInstance(String username, String password) throws MessagingException {
      if (instance == null) {
          instance = new EmailSessionManager(username, password);
      }
      return instance;
  }

  public static EmailSessionManager getInstance() throws IllegalStateException {
      if (instance == null) {
          throw new IllegalStateException("EmailSessionManager is not initialized. Please login first.");
      }
      return instance;
  }

  public static String getUsername() {
      return currentUsername;
  }

  public static String getPassword() {
      return currentPassword;
  }

  public Message[] receiveEmail() throws MessagingException {
      if (emailFolder == null || !emailFolder.isOpen()) {
          emailFolder = store.getFolder("INBOX");
          emailFolder.open(Folder.READ_ONLY);
      }
      return emailFolder.getMessages();
  }

  public Message[] getMessagesFromFolder(String folderName) throws MessagingException {
      Folder folder = store.getFolder(folderName);
      if (!folder.exists()) {
          throw new MessagingException("Folder " + folderName + " does not exist");
      }
      folder.open(Folder.READ_ONLY);
      Message[] messages = folder.getMessages();
      folder.close(false);
      return messages;
  }

  public void close() throws MessagingException {
      if (emailFolder != null) {
          emailFolder.close(false);
          emailFolder = null;
      }
      if (store != null) {
          store.close();
          store = null;
      }
      instance = null;
      currentUsername = "";
      currentPassword = "";
  }
}