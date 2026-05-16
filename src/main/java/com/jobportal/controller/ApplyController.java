package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.exception.ValidationException;
import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;
import com.jobportal.service.UserService;
import javafx.stage.FileChooser;

import java.io.File;

public class ApplyController {

    @FXML private Label jobTitleLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private Label selectedFileLabel;
    @FXML private TextArea coverLetterArea;
    @FXML private Button submitButton;
    @FXML private ProgressIndicator submitProgress;

    @FXML private VBox contactInfoBox;
    @FXML private Label contactEmailLabel;
    @FXML private Label telegramLabel;
    @FXML private Label websiteLabel;
    @FXML private Label linkedinLabel;

    private final ApplicationService applicationService = new ApplicationService();
    private final UserService userService = new UserService();
    private String resumeFilePath = "";

    @FXML
    public void initialize() {
        Job currentJob = SessionManager.getCurrentJob();
        User currentUser = SessionManager.getCurrentUser();

        if (currentJob == null || currentUser == null) {
            MainApp.changeScene("job_dashboard.fxml", "Jobs");
            return;
        }

        jobTitleLabel.setText("Applying for: " + currentJob.getTitle());
        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        addressField.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");

        boolean hasContactInfo = false;
        if (currentJob.getContactEmail() != null && !currentJob.getContactEmail().isEmpty()) {
            contactEmailLabel.setText("Email: " + currentJob.getContactEmail());
            hasContactInfo = true;
        }
        if (currentJob.getTelegramLink() != null && !currentJob.getTelegramLink().isEmpty()) {
            telegramLabel.setText("Telegram: " + currentJob.getTelegramLink());
            hasContactInfo = true;
        }
        if (currentJob.getWebsiteLink() != null && !currentJob.getWebsiteLink().isEmpty()) {
            websiteLabel.setText("Website: " + currentJob.getWebsiteLink());
            hasContactInfo = true;
        }
        if (currentJob.getLinkedinLink() != null && !currentJob.getLinkedinLink().isEmpty()) {
            linkedinLabel.setText("LinkedIn: " + currentJob.getLinkedinLink());
            hasContactInfo = true;
        }
        
        contactInfoBox.setVisible(hasContactInfo);
        contactInfoBox.setManaged(hasContactInfo);
    }

    @FXML
    private void handleUploadResume() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CV/Resume");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx")
        );

        File selectedFile = fileChooser.showOpenDialog(MainApp.getPrimaryStage());
        if (selectedFile != null) {
            resumeFilePath = selectedFile.getAbsolutePath();
            selectedFileLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    private void handleSubmitApplication() {
        Job currentJob = SessionManager.getCurrentJob();
        User currentUser = SessionManager.getCurrentUser();

        if (fullNameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Missing Input", "Please provide your Full Name and Email Address.");
            return;
        }
        
        if (!com.jobportal.util.ValidationUtil.isValidEmail(emailField.getText().trim())) {
            AlertUtil.showWarning("Invalid Input", "Please provide a valid email format.");
            return;
        }

        if (resumeFilePath.isEmpty()) {
            AlertUtil.showWarning("Missing Input", "Please upload a CV/Resume.");
            return;
        }

        submitButton.setDisable(true);
        submitProgress.setVisible(true);

        Task<Boolean> submitTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Update user profile properties dynamically
                currentUser.setFullName(fullNameField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setPhone(phoneField.getText().trim());
                currentUser.setAddress(addressField.getText().trim());
                userService.update(currentUser); // Save to database

                // Handle real file upload (Copy to centralized folder)
                String finalResumePath = resumeFilePath;
                if (resumeFilePath != null && !resumeFilePath.isEmpty()) {
                    java.io.File source = new java.io.File(resumeFilePath);
                    if (source.exists()) {
                        java.io.File uploadDir = new java.io.File("uploads/resumes");
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs();
                        }
                        String ext = "";
                        if (source.getName().lastIndexOf(".") > 0) {
                            ext = source.getName().substring(source.getName().lastIndexOf("."));
                        }
                        String newFileName = "resume_" + currentUser.getId() + "_" + System.currentTimeMillis() + ext;
                        java.io.File dest = new java.io.File(uploadDir, newFileName);
                        java.nio.file.Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        finalResumePath = dest.getAbsolutePath();
                    }
                }

                Application application = new Application(currentJob.getId(), currentUser.getId(), coverLetterArea.getText());
                application.setResumePath(finalResumePath);

                boolean applied = applicationService.apply(application);
                if (applied) {
                    com.jobportal.util.EmailUtil.sendNotificationEmail(
                        currentUser.getEmail(), 
                        "Application Submitted Successfully", 
                        "Dear " + currentUser.getFullName() + ",\n\nYour application for the position of '" + currentJob.getTitle() + "' has been successfully submitted.\n\nGood luck!\nJob Portal Team"
                    );
                    if (currentJob.getContactEmail() != null && !currentJob.getContactEmail().isEmpty()) {
                        com.jobportal.util.EmailUtil.sendNotificationEmail(
                            currentJob.getContactEmail(),
                            "New Application Received",
                            "You have received a new application for the position of '" + currentJob.getTitle() + "' from " + currentUser.getFullName() + ".\n\nPlease log in to the dashboard to review it."
                        );
                    }
                }
                return applied;
            }
        };

        submitTask.setOnSucceeded(event -> {
            submitButton.setDisable(false);
            submitProgress.setVisible(false);
            if (submitTask.getValue()) {
                AlertUtil.showInfo("Success", "Application submitted successfully (Email sent to employer and applicant).");
                MainApp.changeScene("dashboard.fxml", "Dashboard");
            } else {
                AlertUtil.showError("Error", "Failed to submit your application. Please try again.");
            }
        });

        submitTask.setOnFailed(event -> {
            submitButton.setDisable(false);
            submitProgress.setVisible(false);
            Throwable ex = submitTask.getException();
            if (ex instanceof ValidationException) {
                AlertUtil.showError("Error", ex.getMessage());
            } else {
                AlertUtil.showError("Error", "An unexpected error occurred while applying.");
                ex.printStackTrace();
            }
        });

        Thread bgThread = new Thread(submitTask);
        bgThread.setDaemon(true);
        bgThread.start();
    }

    @FXML
    private void handleCancel() {
        MainApp.changeScene("job_dashboard.fxml", "Jobs");
    }
}
