package com.emailclient;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;

public class SMSSender {
    public static final String API_KEY = "";
    public static final String API_SECRET = "";

    public SMSSender() {
    }

    public void sendEmailAsSMS(String nomExpediteur, String numDest, String content) {
        VonageClient client = VonageClient.builder()
                .apiKey(API_KEY)
                .apiSecret(API_SECRET)
                .build();

         TextMessage message = new TextMessage(
                    nomExpediteur,
                    numDest,
                    content
                );

         try {
                SmsSubmissionResponse response = client.getSmsClient().submitMessage(message);

                if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
                    System.out.println("SMS envoyé avec succès !");
                } else {
                    System.out.println("Échec de l'envoi : " + response.getMessages().get(0).getErrorText());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


    }
}