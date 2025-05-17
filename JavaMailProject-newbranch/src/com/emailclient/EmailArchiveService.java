package com.emailclient;

import com.models.SentEmail;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

public class EmailArchiveService {
    private static final String ARCHIVE_DIR = "sent_emails";
    
    public static void saveSentEmail(String subject, String body, List<String> recipients, 
                                   List<String> attachmentPaths) {
        try {
            // Create archive directory if it doesn't exist
            Path archivePath = Paths.get(ARCHIVE_DIR);
            if (!Files.exists(archivePath)) {
                Files.createDirectories(archivePath);
            }

            // Create SentEmail object
            SentEmail sentEmail = new SentEmail(
                subject,
                body,
                recipients,
                new Date(),
                attachmentPaths
            );

            // Generate unique filename based on timestamp
            String filename = String.format("email_%d.xml", System.currentTimeMillis());
            File outputFile = new File(ARCHIVE_DIR, filename);

            // Create JAXB context and marshaller
            JAXBContext context = JAXBContext.newInstance(SentEmail.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshal the object to XML file
            marshaller.marshal(sentEmail, outputFile);
            
            System.out.println("Email archived successfully: " + outputFile.getAbsolutePath());
        } catch (JAXBException | IOException e) {
            System.err.println("Error archiving email: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 