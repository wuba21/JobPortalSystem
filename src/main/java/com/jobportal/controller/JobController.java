package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.model.Job;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class JobController {

    @FXML
    private FlowPane jobsContainer;
    @FXML
    private ProgressIndicator loadingSpinner;
    @FXML
    private ToggleButton recentJobsBtn;
    @FXML
    private ToggleButton longTermJobsBtn;
    @FXML
    private ToggleGroup filterGroup;
    @FXML
    private Label pageTitleLabel;
    @FXML
    private HBox filterBox;

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final com.jobportal.service.SavedJobService savedJobService = new com.jobportal.service.SavedJobService();

    @FXML
    private ToggleButton advancedSearchBtn;
    @FXML
    private HBox advancedSearchBox;
    @FXML
    private TextField keywordField;
    @FXML
    private TextField locationField;
    @FXML
    private ComboBox<String> jobTypeCombo;
    @FXML
    private TextField minSalaryField;
    @FXML
    private TextField maxSalaryField;

    @FXML
    public void initialize() {
        if (jobTypeCombo != null) {
            jobTypeCombo.getItems().addAll("Any", "FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP", "REMOTE",
                    "HYBRID");
            jobTypeCombo.setValue("Any");
        }

        if (filterGroup != null) {
            filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    oldVal.setSelected(true); // Prevent unselecting both
                } else {
                    if (advancedSearchBtn != null && advancedSearchBtn.isSelected()) {
                        if (advancedSearchBox != null) {
                            advancedSearchBox.setVisible(true);
                            advancedSearchBox.setManaged(true);
                        }
                    } else {
                        if (advancedSearchBox != null) {
                            advancedSearchBox.setVisible(false);
                            advancedSearchBox.setManaged(false);
                        }
                        loadJobs();
                    }
                }
            });
        }

        if (SessionManager.getViewingCompanyId() != null) {
            if (pageTitleLabel != null) {
                pageTitleLabel.setText("Jobs at " + SessionManager.getViewingCompanyName());
            }
            if (filterBox != null) {
                filterBox.setVisible(false);
                filterBox.setManaged(false);
            }
        }

        loadJobs();
    }

    @FXML
    private void handleAdvancedSearch() {
        if (loadingSpinner != null)
            loadingSpinner.setVisible(true);
        if (jobsContainer != null)
            jobsContainer.getChildren().clear();

        String keyword = keywordField != null ? keywordField.getText() : null;
        String location = locationField != null ? locationField.getText() : null;
        String jobType = jobTypeCombo != null ? jobTypeCombo.getValue() : "Any";
        java.math.BigDecimal minSal = null;
        java.math.BigDecimal maxSal = null;

        try {
            if (minSalaryField != null && !minSalaryField.getText().trim().isEmpty()) {
                minSal = new java.math.BigDecimal(minSalaryField.getText().trim());
            }
            if (maxSalaryField != null && !maxSalaryField.getText().trim().isEmpty()) {
                maxSal = new java.math.BigDecimal(maxSalaryField.getText().trim());
            }
        } catch (NumberFormatException e) {
            com.jobportal.util.ToastUtil.showError(jobsContainer.getScene().getWindow(), "Invalid salary format.");
            if (loadingSpinner != null)
                loadingSpinner.setVisible(false);
            return;
        }

        final java.math.BigDecimal finalMinSal = minSal;
        final java.math.BigDecimal finalMaxSal = maxSal;

        Task<List<Job>> searchTask = new Task<List<Job>>() {
            @Override
            protected List<Job> call() throws Exception {
                return jobService.advancedSearch(keyword, location, jobType, finalMinSal, finalMaxSal);
            }
        };

        searchTask.setOnSucceeded(e -> {
            List<Job> jobs = searchTask.getValue();
            populateJobCards(jobs);
            if (loadingSpinner != null)
                loadingSpinner.setVisible(false);
        });

        searchTask.setOnFailed(e -> {
            if (loadingSpinner != null)
                loadingSpinner.setVisible(false);
            com.jobportal.util.ToastUtil.showError(jobsContainer.getScene().getWindow(), "Failed to search jobs");
        });

        Thread bgThread = new Thread(searchTask);
        bgThread.setDaemon(true);
        bgThread.start();
    }

    private void loadJobs() {
        if (loadingSpinner != null)
            loadingSpinner.setVisible(true);
        if (jobsContainer != null)
            jobsContainer.getChildren().clear();

        boolean showRecent = recentJobsBtn != null && recentJobsBtn.isSelected();

        Task<List<Job>> loadTask = new Task<List<Job>>() {
            @Override
            protected List<Job> call() throws Exception {
                // Background loading
                if (SessionManager.getViewingCompanyId() != null) {
                    return jobService.findByEmployerId(SessionManager.getViewingCompanyId());
                } else if (showRecent) {
                    return jobService.findRecentJobs();
                } else {
                    return jobService.findLongTermJobs();
                }
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<Job> jobs = loadTask.getValue();
            populateJobCards(jobs);
            if (loadingSpinner != null)
                loadingSpinner.setVisible(false);
        });

        loadTask.setOnFailed(e -> {
            if (loadingSpinner != null)
                loadingSpinner.setVisible(false);
            com.jobportal.util.ToastUtil.showError(jobsContainer.getScene().getWindow(), "Failed to load jobs");
        });

        Thread bgThread = new Thread(loadTask);
        bgThread.setDaemon(true);
        bgThread.start();
    }

    private void populateJobCards(List<Job> jobs) {
        if (jobsContainer == null)
            return;

        for (Job job : jobs) {
            VBox card = createJobCard(job);
            jobsContainer.getChildren().add(card);
        }

        if (jobs.isEmpty()) {
            Label noJobsLabel = new Label("No jobs available in this category.");
            noJobsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b;");
            jobsContainer.getChildren().add(noJobsLabel);
        }
    }

    private VBox createJobCard(Job job) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: #f0fdfa; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(300);
        card.setMaxWidth(300);

        // Hover effect
        card.setOnMouseEntered(
                e -> card.setStyle("-fx-background-color: #e0f2fe; -fx-border-radius: 10; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 8);"));
        card.setOnMouseExited(
                e -> card.setStyle("-fx-background-color: #f0fdfa; -fx-border-radius: 10; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"));

        // Header: Logo and Title/Company
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(Double.MAX_VALUE);

        ImageView logoView = new ImageView();
        logoView.setFitHeight(40);
        logoView.setFitWidth(40);
        // Using a placeholder logo for now
        logoView.setStyle("-fx-background-color: #cbd5e1; -fx-background-radius: 5;");
        try {
            // You can load an actual image here if available: new Image("path/to/img.png")
        } catch (Exception ignored) {
        }

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(job.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-text-fill: #1e293b; -fx-cursor: hand;");
        titleLabel.setOnMouseClicked(e -> showJobDetails(job));
        titleLabel.setOnMouseEntered(
                e -> titleLabel.setStyle("-fx-text-fill: #3b82f6; -fx-underline: true; -fx-cursor: hand;"));
        titleLabel.setOnMouseExited(
                e -> titleLabel.setStyle("-fx-text-fill: #1e293b; -fx-underline: false; -fx-cursor: hand;"));

        Label companyLabel = new Label(job.getCompanyName() != null ? job.getCompanyName() : "Company Unknown");
        companyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        companyLabel.setOnMouseEntered(e -> companyLabel
                .setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 14px; -fx-underline: true; -fx-cursor: hand;"));
        companyLabel.setOnMouseExited(e -> companyLabel
                .setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-underline: false; -fx-cursor: hand;"));
        companyLabel.setOnMouseClicked(e -> {
            if (job.getEmployerId() > 0) {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource("/fxml/company_details.fxml"));
                    javafx.scene.Parent root = loader.load();
                    CompanyDetailsController controller = loader.getController();
                    controller.setCompanyId(job.getEmployerId());
                    MainApp.changeCenterScene(root, "Company Details");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    AlertUtil.showError("Error", "Could not load company details.");
                }
            }
        });
        titleBox.getChildren().addAll(titleLabel, companyLabel);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button saveBtn = new Button();
        saveBtn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 18px; -fx-padding: 0 5 0 0;");

        if (SessionManager.getCurrentUser() != null
                && "JOB_SEEKER".equals(SessionManager.getCurrentUser().getUserType())) {
            int userId = SessionManager.getCurrentUser().getId();
            boolean isSaved = savedJobService.isJobSaved(userId, job.getId());
            saveBtn.setText(isSaved ? "⭐" : "☆");

            saveBtn.setOnAction(e -> {
                if (savedJobService.isJobSaved(userId, job.getId())) {
                    if (savedJobService.removeSavedJob(userId, job.getId())) {
                        saveBtn.setText("☆");
                    }
                } else {
                    if (savedJobService.saveJob(userId, job.getId())) {
                        saveBtn.setText("⭐");
                    }
                }
            });
        } else {
            saveBtn.setText("☆");
            saveBtn.setOnAction(e -> {
                if (SessionManager.getCurrentUser() == null) {
                    AlertUtil.showInfo("Login Required", "Please login as a Job Seeker to save jobs.");
                } else {
                    AlertUtil.showInfo("Feature Restricted", "Only Job Seekers can save jobs.");
                }
            });
        }

        header.getChildren().addAll(logoView, titleBox, headerSpacer, saveBtn);

        // Details: Type, Location, Salary
        VBox detailsBox = new VBox(8);
        detailsBox.getChildren().add(createIconLabel("Address :" + job.getLocation()));
        detailsBox.getChildren().add(createIconLabel(" " + job.getJobType()));

        String salary = "Salary: Negotiable";
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            salary = "Salary: $" + job.getSalaryMin() + " - $" + job.getSalaryMax();
        } else if (job.getSalaryMin() != null) {
            salary = "Salary: From $" + job.getSalaryMin();
        }
        detailsBox.getChildren().add(createIconLabel(salary));

        // Requirements / Skills Tag
        Label skillsLabel = new Label();
        skillsLabel.setStyle(
                "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-size: 12px;");
        if (job.getRequirements() != null && !job.getRequirements().isEmpty()) {
            skillsLabel.setText(job.getRequirements().length() > 20 ? job.getRequirements().substring(0, 20) + "..."
                    : job.getRequirements());
        } else {
            skillsLabel.setText("No skills listed");
        }

        // Days left
        Label daysLeft = new Label();
        daysLeft.setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold; -fx-font-size: 12px;");
        if (job.getDeadline() != null) {
            long diff = job.getDeadline().getTime() - System.currentTimeMillis();
            long days = diff / (1000 * 60 * 60 * 24);
            daysLeft.setText(days > 0 ? days + " days left" : "Expired");
        } else {
            daysLeft.setText("No deadline");
        }

        HBox footerInfo = new HBox(10, skillsLabel, new Region(), daysLeft);
        HBox.setHgrow(footerInfo.getChildren().get(1), Priority.ALWAYS);
        footerInfo.setAlignment(Pos.CENTER_LEFT);

        // Buttons and Socials
        VBox actionBox = new VBox(10);
        actionBox.setAlignment(Pos.CENTER);

        Button applyBtn = new Button();
        applyBtn.setMaxWidth(Double.MAX_VALUE);

        Button detailsBtn = new Button("View Details");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #0ea5e9; -fx-text-fill: #0ea5e9; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10; -fx-border-radius: 5;");
        detailsBtn.setOnAction(e -> showJobDetails(job));

        if (SessionManager.getCurrentUser() == null) {
            applyBtn.setText("Login to Apply");
            applyBtn.setStyle(
                    "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10;");
            applyBtn.setOnAction(e -> MainApp.changeScene("login.fxml", "Login"));
        } else if (SessionManager.isEmployer()) {
            applyBtn.setVisible(false);
            applyBtn.setManaged(false);
        } else {
            boolean applied = applicationService.hasApplied(SessionManager.getCurrentUser().getId(), job.getId());
            if (applied) {
                applyBtn.setText("Already Applied");
                applyBtn.setDisable(true);
                applyBtn.setStyle(
                        "-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
            } else {
                applyBtn.setText("Apply Now");
                applyBtn.setStyle(
                        "-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10;");
                applyBtn.setOnAction(e -> {
                    SessionManager.setCurrentJob(job);
                    MainApp.changeScene("apply-form.fxml", "Apply for Job");
                });
            }
        }

        // Social Media Icons
        HBox socialsBox = new HBox(15);
        socialsBox.setAlignment(Pos.CENTER);

        Button tgBtn = createSocialButton("Telegram", job.getTelegramLink(), "#0088cc");
        Button inBtn = createSocialButton("LinkedIn", job.getLinkedinLink(), "#0077b5");
        Button fbBtn = createSocialButton("Facebook", job.getWebsiteLink(), "#1877f2");

        socialsBox.getChildren().addAll(tgBtn, inBtn, fbBtn);

        if (SessionManager.isEmployer()) {
            actionBox.getChildren().addAll(detailsBtn, socialsBox);
        } else {
            actionBox.getChildren().addAll(applyBtn, detailsBtn, socialsBox);
        }

        card.getChildren().addAll(header, new Separator(), detailsBox, footerInfo, actionBox);
        return card;
    }

    private void showJobDetails(Job job) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/job_details.fxml"));
            javafx.scene.Parent root = loader.load();

            JobDetailsController controller = loader.getController();
            controller.setJob(job);

            MainApp.changeCenterScene(root, "Job Details");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to load job details.");
        }
    }

    private Label createIconLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
        return lbl;
    }

    private Button createSocialButton(String name, String url, String color) {
        Button btn = new Button(name);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color
                + "; -fx-underline: true; -fx-cursor: hand; -fx-font-size: 11px;");
        if (url == null || url.trim().isEmpty()) {
            btn.setVisible(false);
            btn.setManaged(false);
        } else {
            btn.setOnAction(e -> openWebpage(url));
        }
        return btn;
    }

    private void openWebpage(String urlString) {
        try {
            if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                urlString = "https://" + urlString;
            }
            Desktop.getDesktop().browse(new URI(urlString));
        } catch (Exception e) {
            System.err.println("Failed to open link: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        SessionManager.setViewingCompany(null, null);
        if (SessionManager.getCurrentUser() != null) {
            MainApp.goBackToDashboard();
        } else {
            MainApp.changeScene("Home.fxml", "Home");
        }
    }

    @FXML
    private void handleResetFilters() {
        // Clear all filter fields
        if (keywordField != null)
            keywordField.clear();
        if (locationField != null)
            locationField.clear();
        if (minSalaryField != null)
            minSalaryField.clear();
        if (maxSalaryField != null)
            maxSalaryField.clear();
        if (jobTypeCombo != null)
            jobTypeCombo.setValue("Any");

        // Hide advanced search box
        if (advancedSearchBox != null) {
            advancedSearchBox.setVisible(false);
            advancedSearchBox.setManaged(false);
        }
        if (advancedSearchBtn != null) {
            advancedSearchBtn.setSelected(false);
        }

        // Reload default jobs
        loadJobs();

        // Show toast feedback
        if (jobsContainer != null) {
            com.jobportal.util.ToastUtil.showInfo(jobsContainer.getScene().getWindow(),
                    "Filters reset. Showing all jobs.");
        }
    }
}
