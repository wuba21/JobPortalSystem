package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.model.Job;
import com.jobportal.service.JobService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    @FXML private VBox jobsView;
    @FXML private TableView<Job> jobsTable;
    @FXML private TableColumn<Job, String> colJobTitle;
    @FXML private TableColumn<Job, String> colJobCompany;
    @FXML private TableColumn<Job, String> colJobLocation;
    @FXML private TableColumn<Job, Void> colJobActions;
    
    @FXML private Label notificationBadge;
    @FXML private Label notificationText;

    private final JobService jobService = new JobService();
    private final com.jobportal.dao.ContactMessageDAO messageDAO = new com.jobportal.dao.ContactMessageDAO();

    @FXML
    public void initialize() {
        setupJobsTable();
        showManageJobs();
        startNotificationService();
    }

    private void startNotificationService() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10), event -> loadUnreadMessageCount())
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
        loadUnreadMessageCount(); 
    }

    private void loadUnreadMessageCount() {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() {
                return messageDAO.countUnread();
            }
        };
        task.setOnSucceeded(e -> {
            int count = task.getValue();
            if (count > 0) {
                notificationBadge.setText(String.valueOf(count));
                notificationBadge.setVisible(true);
                notificationText.setText("New Messages (" + count + ")");
                notificationText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            } else {
                notificationBadge.setVisible(false);
                notificationText.setText("No New Messages");
                notificationText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #475569;");
            }
        });
        new Thread(task).start();
    }

    @FXML
    private void showMessages() {
        MainApp.changeScene("admin_messages.fxml", "Admin Messages");
    }

    @FXML
    private void showManageJobs() {
        jobsView.setVisible(true);
        jobsView.setManaged(true);
        loadJobs();
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("dashboard.fxml", "Dashboard");
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        MainApp.changeScene("login.fxml", "Login");
    }

    private void loadJobs() {
        Task<List<Job>> task = new Task<List<Job>>() {
            @Override
            protected List<Job> call() {
                return jobService.findAll();
            }
        };
        task.setOnSucceeded(e -> {
            ObservableList<Job> data = FXCollections.observableArrayList(task.getValue());
            jobsTable.setItems(data);
        });
        task.setOnFailed(e -> AlertUtil.showError("Error", "Failed to load jobs."));
        new Thread(task).start();
    }



    private void setupJobsTable() {
        colJobTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colJobCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colJobLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        Callback<TableColumn<Job, Void>, TableCell<Job, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Job, Void> call(final TableColumn<Job, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Edit");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox pane = new HBox(10, btnEdit, btnDelete);

                    {
                        btnEdit.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");

                        btnEdit.setOnAction(event -> {
                            Job job = getTableView().getItems().get(getIndex());
                            SessionManager.setCurrentJob(job);
                            MainApp.changeScene("job-form.fxml", "Edit Job");
                        });

                        btnDelete.setOnAction(event -> {
                            Job job = getTableView().getItems().get(getIndex());
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Confirm Deletion");
                            alert.setHeaderText("Delete Job");
                            alert.setContentText("Are you sure you want to delete this job?");
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                if (jobService.delete(job.getId())) {
                                    loadJobs();
                                } else {
                                    AlertUtil.showError("Error", "Could not delete job.");
                                }
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        colJobActions.setCellFactory(cellFactory);
    }


}
