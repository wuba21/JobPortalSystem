package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.dao.ContactMessageDAO;
import com.jobportal.model.ContactMessage;
import com.jobportal.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class AdminMessagesController {

    @FXML private TableView<ContactMessage> messagesTable;
    @FXML private TableColumn<ContactMessage, String> colName;
    @FXML private TableColumn<ContactMessage, String> colEmail;
    @FXML private TableColumn<ContactMessage, String> colSubject;
    @FXML private TableColumn<ContactMessage, String> colDate;
    @FXML private TableColumn<ContactMessage, String> colStatus;
    @FXML private TableColumn<ContactMessage, Void> colActions;

    private final ContactMessageDAO messageDAO = new ContactMessageDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        setupTable();
        loadMessages();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        
        colDate.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getSentDate();
            return new SimpleStringProperty(ts != null ? sdf.format(ts) : "");
        });
        
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<ContactMessage, Void> call(final TableColumn<ContactMessage, Void> param) {
                return new TableCell<>() {
                    private final Button btnRead = new Button("Mark Read");
                    private final Button btnView = new Button("View");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox pane = new HBox(10, btnView, btnRead, btnDelete);

                    {
                        btnView.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand;");
                        btnRead.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");

                        btnView.setOnAction(event -> {
                            ContactMessage msg = getTableView().getItems().get(getIndex());
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("View Message");
                            alert.setHeaderText("From: " + msg.getName() + " (" + msg.getEmail() + ")\nSubject: " + msg.getSubject());
                            alert.setContentText(msg.getMessage());
                            alert.showAndWait();
                            
                            if ("Unread".equals(msg.getStatus())) {
                                markAsRead(msg);
                            }
                        });

                        btnRead.setOnAction(event -> {
                            ContactMessage msg = getTableView().getItems().get(getIndex());
                            markAsRead(msg);
                        });

                        btnDelete.setOnAction(event -> {
                            ContactMessage msg = getTableView().getItems().get(getIndex());
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Confirm Deletion");
                            alert.setHeaderText("Delete Message");
                            alert.setContentText("Are you sure you want to delete this message?");
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                deleteMessage(msg);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            ContactMessage msg = getTableView().getItems().get(getIndex());
                            btnRead.setVisible("Unread".equals(msg.getStatus()));
                            btnRead.setManaged("Unread".equals(msg.getStatus()));
                            setGraphic(pane);
                        }
                    }
                };
            }
        });
    }

    private void loadMessages() {
        Task<List<ContactMessage>> task = new Task<>() {
            @Override
            protected List<ContactMessage> call() {
                return messageDAO.findAll();
            }
        };
        task.setOnSucceeded(e -> {
            ObservableList<ContactMessage> data = FXCollections.observableArrayList(task.getValue());
            messagesTable.setItems(data);
        });
        task.setOnFailed(e -> AlertUtil.showError("Error", "Failed to load messages."));
        new Thread(task).start();
    }

    private void markAsRead(ContactMessage msg) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return messageDAO.markAsRead(msg.getId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                loadMessages();
            }
        });
        new Thread(task).start();
    }

    private void deleteMessage(ContactMessage msg) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return messageDAO.delete(msg.getId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                loadMessages();
            } else {
                AlertUtil.showError("Error", "Could not delete message.");
            }
        });
        new Thread(task).start();
    }

    @FXML
    private void handleManageJobs() {
        MainApp.changeScene("admin_dashboard.fxml", "Admin Dashboard");
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("admin_dashboard.fxml", "Admin Dashboard");
    }
}
