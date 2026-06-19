package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.model.Message;
import com.jobportal.model.User;
import com.jobportal.service.MessageService;
import com.jobportal.service.UserService;
import com.jobportal.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.stream.Collectors;

public class MessagesController {

    @FXML private ListView<User> contactsListView;
    @FXML private VBox chatArea;
    @FXML private VBox emptyState;
    @FXML private Label chatUserNameLabel;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField messageInputField;

    private final MessageService messageService = new MessageService();
    private final UserService userService = new UserService();
    private User currentContact = null;
    private int currentUserId;
    private String currentUserType;
    private List<User> allMessageableUsers;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null) {
            MainApp.restoreMainLayout("login.fxml", "Login");
            return;
        }
        currentUserId = SessionManager.getCurrentUser().getId();
        currentUserType = SessionManager.getCurrentUser().getUserType();

        // Load all messageable users for New Message dialog
        allMessageableUsers = userService.getMessageableUsers(currentUserId, currentUserType);

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

                    // Avatar circle label
                    Label avatar = new Label(user.getFullName().substring(0, 1).toUpperCase());
                    avatar.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; "
                            + "-fx-font-weight: bold; -fx-font-size: 14px; "
                            + "-fx-min-width: 36; -fx-min-height: 36; "
                            + "-fx-background-radius: 18; -fx-alignment: center;");

                    Label nameLbl = new Label(user.getFullName());
                    nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 13px;");

                    String typeDisplay = formatUserType(user.getUserType());
                    Label typeLbl = new Label(typeDisplay);
                    typeLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

                    VBox vBox = new VBox(2, nameLbl, typeLbl);
                    vBox.setAlignment(Pos.CENTER_LEFT);
                    box.getChildren().addAll(avatar, vBox);
                    HBox.setHgrow(vBox, Priority.ALWAYS);

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

        // If a contact was set via session (e.g., from Application details), add them
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

    @FXML
    private void handleNewMessage() {
        // Build a search dialog
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("New Message");
        dialog.setHeaderText("Select a user to message");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Search by name...");
        searchField.setStyle("-fx-padding: 8; -fx-background-radius: 8; "
                + "-fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-font-size: 13px;");

        ListView<User> userListView = new ListView<>();
        userListView.setPrefHeight(300);
        userListView.getItems().setAll(allMessageableUsers);

        userListView.setCellFactory(p -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox row = new HBox(12);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(8));

                    Label av = new Label(user.getFullName().substring(0, 1).toUpperCase());
                    av.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; "
                            + "-fx-font-weight: bold; -fx-min-width: 36; -fx-min-height: 36; "
                            + "-fx-background-radius: 18; -fx-alignment: center;");

                    Label name = new Label(user.getFullName());
                    name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
                    Label type = new Label(formatUserType(user.getUserType()));
                    type.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                    VBox info = new VBox(2, name, type);
                    row.getChildren().addAll(av, info);
                    setGraphic(row);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        // Live search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal.trim().toLowerCase();
            if (query.isEmpty()) {
                userListView.getItems().setAll(allMessageableUsers);
            } else {
                List<User> filtered = allMessageableUsers.stream()
                        .filter(u -> u.getFullName().toLowerCase().contains(query)
                                || u.getUserType().toLowerCase().contains(query))
                        .collect(Collectors.toList());
                userListView.getItems().setAll(filtered);
            }
        });

        // On double-click select user
        userListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                User selected = userListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    dialog.setResult(selected);
                    dialog.close();
                }
            }
        });

        // Also single click + OK-like button
        Button startChatBtn = new Button("Start Chat →");
        startChatBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
        startChatBtn.setOnAction(e -> {
            User selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                dialog.setResult(selected);
                dialog.close();
            }
        });

        VBox content = new VBox(12, searchField, userListView, startChatBtn);
        content.setPadding(new Insets(15));
        content.setPrefWidth(380);
        dialogPane.setContent(content);

        dialog.setResultConverter(bt -> null);
        dialog.showAndWait().ifPresent(this::startNewChat);
    }

    private void startNewChat(User contact) {
        if (contact == null) return;

        // Add to contacts list if not already there
        boolean exists = contactsListView.getItems().stream()
                .anyMatch(u -> u.getId() == contact.getId());
        if (!exists) {
            contactsListView.getItems().add(0, contact);
        }

        // Select and open chat
        contactsListView.getSelectionModel().select(contact);
        openChat(contact);
    }

    private void openChat(User contact) {
        currentContact = contact;
        chatUserNameLabel.setText(contact.getFullName() + "  •  " + formatUserType(contact.getUserType()));
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        chatArea.setVisible(true);
        chatArea.setManaged(true);

        messageService.markConversationAsRead(contact.getId(), currentUserId);
        loadMessages();

        Platform.runLater(() -> messageInputField.requestFocus());
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        if (currentContact == null) return;

        List<Message> msgs = messageService.getConversation(currentUserId, currentContact.getId());

        if (msgs.isEmpty()) {
            Label empty = new Label("No messages yet. Say hello! 👋");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            HBox emptyBox = new HBox(empty);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30));
            messagesContainer.getChildren().add(emptyBox);
            return;
        }

        for (Message m : msgs) {
            boolean isMe = (m.getSenderId() == currentUserId);

            HBox msgBox = new HBox();
            msgBox.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            msgBox.setPadding(new Insets(2, 8, 2, 8));

            Label msgLabel = new Label(m.getContent());
            msgLabel.setWrapText(true);
            msgLabel.setMaxWidth(400);
            msgLabel.setPadding(new Insets(10, 15, 10, 15));

            if (isMe) {
                msgLabel.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; "
                        + "-fx-background-radius: 18 18 0 18; -fx-font-size: 13px;");
            } else {
                msgLabel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; "
                        + "-fx-background-radius: 18 18 18 0; -fx-font-size: 13px;");
            }

            msgBox.getChildren().add(msgLabel);
            messagesContainer.getChildren().add(msgBox);
        }

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

            // Add to contacts if new
            boolean exists = contactsListView.getItems().stream()
                    .anyMatch(u -> u.getId() == currentContact.getId());
            if (!exists) {
                contactsListView.getItems().add(0, currentContact);
            }
        } else {
            com.jobportal.util.ToastUtil.showError(messageInputField.getScene().getWindow(), "Failed to send message.");
        }
    }

    private String formatUserType(String type) {
        if (type == null) return "";
        switch (type) {
            case "ADMIN":      return "🛡 Admin";
            case "EMPLOYER":   return "🏢 Employer";
            case "JOB_SEEKER": return "👤 Job Seeker";
            default:           return type;
        }
    }
}
