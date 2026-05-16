package com.jobportal.model;

import java.sql.Timestamp;

public class ContactMessage {
    private int id;
    private String name;
    private String email;
    private String subject;
    private String message;
    private String status;
    private Timestamp sentDate;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getSentDate() { return sentDate; }
    public void setSentDate(Timestamp sentDate) { this.sentDate = sentDate; }
}
