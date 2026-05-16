package com.jobportal.controller;

import com.jobportal.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

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
}
