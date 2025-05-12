package com.emailclient;

import javax.mail.Message;
import javax.mail.MessagingException;

public class EmailReceiver {

  public static Message[] receiveEmail() throws MessagingException {
      EmailSessionManager manager = EmailSessionManager.getInstance();
      Message[] messages = manager.receiveEmail();
      return messages;
  }
}