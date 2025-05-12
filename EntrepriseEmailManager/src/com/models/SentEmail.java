package com.models;

import java.util.Date;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "sentEmail")
public class SentEmail {
    private String subject;
    private String body;
    private List<String> recipients;
    private Date sentDate;
    private List<String> attachmentPaths;

    public SentEmail() {}

    public SentEmail(String subject, String body, List<String> recipients, Date sentDate, List<String> attachmentPaths) {
        this.subject = subject;
        this.body = body;
        this.recipients = recipients;
        this.sentDate = sentDate;
        this.attachmentPaths = attachmentPaths;
    }

    @XmlElement
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @XmlElement
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @XmlElement
    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    @XmlElement
    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    @XmlElement
    public List<String> getAttachmentPaths() {
        return attachmentPaths;
    }

    public void setAttachmentPaths(List<String> attachmentPaths) {
        this.attachmentPaths = attachmentPaths;
    }
} 