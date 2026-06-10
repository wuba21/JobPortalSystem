package com.jobportal.controller;

import com.jobportal.model.Notification;
import com.jobportal.service.NotificationService;
import com.jobportal.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationsController {

    @FXML private VBox notificationsContainer;
    @FXML private Label emptyLabel;
    @FXML private Button markAllReadBtn;

    private final NotificationService notificationService = new NotificationService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @FXML
    public void initialize() {
        loadNotifications();
    }

    private void loadNotifications() {
        notificationsContainer.getChildren().clear();
        
        if (SessionManager.getCurrentUser() == null) return;
        
        List<Notification> notifications = notificationService.getNotificationsByUser(SessionManager.getCurrentUser().getId());
        
        if (notifications.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            markAllReadBtn.setDisable(true);
            return;
        }
        
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);
        markAllReadBtn.setDisable(false);

        for (Notification notif : notifications) {
            notificationsContainer.getChildren().add(createNotificationCard(notif));
        }
    }

    private HBox createNotificationCard(Notification notif) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Style changes if unread
        String bgColor = notif.isRead() ? "white" : "#f0f9ff";
        String borderInfo = notif.isRead() ? "" : "-fx-border-width: 0 0 0 4; -fx-border-color: #3b82f6; -fx-border-radius: 8;";
        
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 15 20; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2); " + borderInfo);

        VBox contentBox = new VBox(5);
        Label msgLbl = new Label(notif.getMessage());
        msgLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b; " + (notif.isRead() ? "" : "-fx-font-weight: bold;"));
        msgLbl.setWrapText(true);
        
        Label dateLbl = new Label(notif.getCreatedAt() != null ? notif.getCreatedAt().toLocalDateTime().format(FORMATTER) : "");
        dateLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        
        contentBox.getChildren().addAll(msgLbl, dateLbl);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        card.getChildren().add(contentBox);

        if (!notif.isRead()) {
            Button readBtn = new Button("Mark Read");
            readBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3b82f6; -fx-cursor: hand; -fx-font-size: 13px;");
            readBtn.setOnAction(e -> {
                notificationService.markAsRead(notif.getId());
                loadNotifications();
            });
            card.getChildren().add(readBtn);
        }

        return card;
    }

    @FXML
    private void handleMarkAllRead() {
        if (SessionManager.getCurrentUser() != null) {
            notificationService.clearAllNotifications(SessionManager.getCurrentUser().getId());
            loadNotifications();
        }
    }
}
