package com.emailclient;

import java.io.IOException;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

public class MailUtils {
    
     // Extracts the first text/plain (or text/html) part it finds.
    public static String extractText(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        }
        if (part.isMimeType("text/html")) {
            return (String) part.getContent();
        }
        if (part.isMimeType("multipart/*")) {
            MimeMultipart mp = (MimeMultipart) part.getContent();
            // 1) try plain
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    return (String) bp.getContent();
                }
            }
            // 2) try html
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/html")) {
                    return (String) bp.getContent();
                }
            }
            // 3) recurse into sub-multiparts
            for (int i = 0; i < mp.getCount(); i++) {
                String txt = extractText(mp.getBodyPart(i));
                if (!txt.isBlank()) return txt;
            }
        }
        return "";
    }

}
