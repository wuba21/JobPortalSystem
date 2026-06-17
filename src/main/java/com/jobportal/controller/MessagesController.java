package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.model.Message;
import com.jobportal.model.User;
import com.jobportal.service.MessageService;
import com.jobportal.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class MessagesController {

    @FXML private ListView<User> contactsListView;
    @FXML private VBox chatArea;
    @FXML private VBox emptyState;
    @FXML private Label chatUserNameLabel;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField messageInputField;

    private final MessageService messageService = new MessageService();
    private User currentContact = null;
    private int currentUserId;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null) {
            MainApp.restoreMainLayout("login.fxml", "Login");
            return;
        }
        currentUserId = SessionManager.getCurrentUser().getId();

        setupContactsList();
        loadContacts();
    }

    private void setupContactsList() {
        contactsListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(10);
                    box.setPadding(new Insets(10));
                    Label nameLbl = new Label(user.getFullName());
                    nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
                    Label typeLbl = new Label(user.getUserType());
                    typeLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
                    VBox vBox = new VBox(nameLbl, typeLbl);
                    box.getChildren().add(vBox);
                    
                    setGraphic(box);
                    
                    if (isSelected()) {
                        setStyle("-fx-background-color: #e0f2fe; -fx-cursor: hand;");
                    } else {
                        setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                    }
                    
                    setOnMouseEntered(e -> {
                        if (!isSelected()) setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand;");
                    });
                    setOnMouseExited(e -> {
                        if (!isSelected()) setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                    });
                }
            }
        });

        contactsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                openChat(newVal);
            }
        });
    }

    private void loadContacts() {
        List<User> contacts = messageService.getChatContacts(currentUserId);
        
        // If an employer wants to message an applicant not in contacts, they should initiate via Application details
        // For now, if "ContactUser" is set in session, add them to contacts
        User targetUser = SessionManager.getContactUser();
        if (targetUser != null && targetUser.getId() != currentUserId) {
            boolean exists = contacts.stream().anyMatch(u -> u.getId() == targetUser.getId());
            if (!exists) {
                contacts.add(0, targetUser);
            }
            SessionManager.setContactUser(null);
        }
        
        contactsListView.getItems().setAll(contacts);
        
        if (targetUser != null) {
            contactsListView.getSelectionModel().select(targetUser);
        }
    }

    private void openChat(User contact) {
        currentContact = contact;
        chatUserNameLabel.setText(contact.getFullName() + " (" + contact.getUserType() + ")");
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        chatArea.setVisible(true);
        chatArea.setManaged(true);
        
        // Mark read
        messageService.markConversationAsRead(contact.getId(), currentUserId);
        
        loadMessages();
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        if (currentContact == null) return;
        
        List<Message> msgs = messageService.getConversation(currentUserId, currentContact.getId());
        
        for (Message m : msgs) {
            boolean isMe = (m.getSenderId() == currentUserId);
            
            HBox msgBox = new HBox();
            msgBox.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            
            Label msgLabel = new Label(m.getContent());
            msgLabel.setWrapText(true);
            msgLabel.setMaxWidth(400);
            msgLabel.setPadding(new Insets(10, 15, 10, 15));
            
            if (isMe) {
                msgLabel.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-background-radius: 15 15 0 15;");
            } else {
                msgLabel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-background-radius: 15 15 15 0;");
            }
            
            msgBox.getChildren().add(msgLabel);
            messagesContainer.getChildren().add(msgBox);
        }
        
        // Scroll to bottom
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    @FXML
    private void handleSendMessage() {
        String content = messageInputField.getText().trim();
        if (content.isEmpty() || currentContact == null) return;
        
        boolean success = messageService.sendMessage(currentUserId, currentContact.getId(), content);
        if (success) {
            messageInputField.clear();
            loadMessages();
        } else {
            com.jobportal.util.ToastUtil.showError(messageInputField.getScene().getWindow(), "Failed to send message.");
        }
    }
}
