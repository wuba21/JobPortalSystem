package com.jobportal.controller;

import com.jobportal.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import java.awt.Desktop;
import java.net.URI;

public class ContactController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField subjectField;
    @FXML private TextArea messageArea;

    private final com.jobportal.dao.ContactMessageDAO messageDAO = new com.jobportal.dao.ContactMessageDAO();

    @FXML
    private void handleSendMessage(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String subject = subjectField != null ? subjectField.getText().trim() : "No Subject";
        String message = messageArea.getText().trim();

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            AlertUtil.showError("Validation Error", "Name, Email, and Message fields are required.");
            return;
        }

        if (!com.jobportal.util.ValidationUtil.isValidEmail(email)) {
            AlertUtil.showError("Validation Error", "Please provide a valid email address.");
            return;
        }

        Button sendButton = (Button) event.getSource();
        sendButton.setDisable(true);
        sendButton.setText("Sending...");

        Task<Boolean> sendTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                com.jobportal.model.ContactMessage msg = new com.jobportal.model.ContactMessage();
                msg.setName(name);
                msg.setEmail(email);
                msg.setSubject(subject);
                msg.setMessage(message);
                
                boolean saved = messageDAO.create(msg);
                if (!saved) {
                    throw new Exception("Database error");
                }
                
                // Keep the email simulation or sending code if needed
                Thread.sleep(800);
                return true;
            }
        };

        sendTask.setOnSucceeded(e -> {
            sendButton.setDisable(false);
            sendButton.setText("Send Message");
            AlertUtil.showInfo("Successful", "Thank you, " + name + "! Your message has been sent.");
            nameField.clear();
            emailField.clear();
            if (subjectField != null) subjectField.clear();
            messageArea.clear();
        });

        sendTask.setOnFailed(e -> {
            sendButton.setDisable(false);
            sendButton.setText("Send Message");
            AlertUtil.showError("Error", "Failed to send message. Please try again.");
            e.getSource().getException().printStackTrace();
        });

        Thread bgThread = new Thread(sendTask);
        bgThread.setDaemon(true);
        bgThread.start();
    }

    @FXML
    private void handleEmailClick(javafx.scene.input.MouseEvent event) {
        openLink("mailto:wubantetil@gmail.com");
    }

    @FXML
    private void handlePhoneClick(javafx.scene.input.MouseEvent event) {
        openLink("tel:+251939304457");
    }

    @FXML
    private void handleContactHover(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Label lbl = (javafx.scene.control.Label) event.getSource();
        lbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #0ea5e9; -fx-cursor: hand; -fx-underline: true;");
    }

    @FXML
    private void handleContactExit(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Label lbl = (javafx.scene.control.Label) event.getSource();
        lbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #475569; -fx-cursor: hand; -fx-underline: false;");
    }

    private void openLink(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            }
        } catch (Exception e) {
            System.err.println("Could not open link: " + e.getMessage());
        }
    }
}
