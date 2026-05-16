package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.model.Job;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Separator;

import java.util.List;

public class CompanyDetailsController {

    @FXML
    private Label lblCompanyInitial;
    @FXML
    private Label lblCompanyName;
    @FXML
    private Label lblJobCount;
    @FXML
    private Label lblDescription;

    @FXML
    private ProgressIndicator loadingSpinner;
    @FXML
    private FlowPane jobsContainer;

    private int companyId;
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();

    @FXML
    public void initialize() {
    }

    public void setCompanyData(int id, String name, String description, int jobCount) {
        this.companyId = id;

        lblCompanyName.setText(name != null ? name : "Company Unknown");
        if (name != null && !name.trim().isEmpty()) {
            lblCompanyInitial.setText(name.substring(0, 1).toUpperCase());
        } else {
            lblCompanyInitial.setText("C");
        }

        lblJobCount.setText(jobCount + " Open Jobs");
        lblDescription.setText(description != null && !description.isEmpty() ? description
                : "No detailed description available for this company.");

        loadJobs();
    }

    public void setCompanyId(int id) {
        this.companyId = id;
        // Fetch company details from DB
        String sql = "SELECT company_name, company_description, (SELECT COUNT(id) FROM jobs WHERE employer_id = ? AND is_active = TRUE) as job_count FROM employers WHERE id = ?";
        try (java.sql.Connection conn = com.jobportal.config.DBConnection.getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, id);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("company_name");
                String desc = rs.getString("company_description");
                int count = rs.getInt("job_count");

                lblCompanyName.setText(name != null ? name : "Company Unknown");
                if (name != null && !name.trim().isEmpty()) {
                    lblCompanyInitial.setText(name.substring(0, 1).toUpperCase());
                } else {
                    lblCompanyInitial.setText("C");
                }
                lblJobCount.setText(count + " Open Jobs");
                lblDescription.setText(
                        desc != null && !desc.isEmpty() ? desc : "No detailed description available for this company.");
            }
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            lblCompanyName.setText("Company Details");
            lblDescription.setText("Could not load company details.");
        }
        loadJobs();
    }

    private void loadJobs() {
        if (loadingSpinner != null)
            loadingSpinner.setVisible(true);
        if (jobsContainer != null)
            jobsContainer.getChildren().clear();

        Task<List<Job>> loadTask = new Task<List<Job>>() {
            @Override
            protected List<Job> call() throws Exception {
                return jobService.findByEmployerId(companyId);
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
            AlertUtil.showError("Error", "Failed to load company jobs.");
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
            Label noJobsLabel = new Label("No active jobs available for this company.");
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

        card.setOnMouseEntered(
                e -> card.setStyle("-fx-background-color: #e0f2fe; -fx-border-radius: 10; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 8);"));
        card.setOnMouseExited(
                e -> card.setStyle("-fx-background-color: #f0fdfa; -fx-border-radius: 10; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView logoView = new ImageView();
        logoView.setFitHeight(40);
        logoView.setFitWidth(40);
        logoView.setStyle("-fx-background-color: #cbd5e1; -fx-background-radius: 5;");

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

        titleBox.getChildren().add(titleLabel);
        header.getChildren().addAll(logoView, titleBox);

        VBox detailsBox = new VBox(8);
        detailsBox.getChildren().add(createIconLabel("Location: " + job.getLocation()));
        detailsBox.getChildren().add(createIconLabel("Type: " + job.getJobType()));

        String salary = "Negotiable";
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            salary = "$" + job.getSalaryMin() + " - $" + job.getSalaryMax();
        } else if (job.getSalaryMin() != null) {
            salary = "From $" + job.getSalaryMin();
        }
        detailsBox.getChildren().add(createIconLabel("Salary: " + salary));

        Label daysLeft = new Label();
        daysLeft.setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold; -fx-font-size: 12px;");
        if (job.getDeadline() != null) {
            long diff = job.getDeadline().getTime() - System.currentTimeMillis();
            long days = diff / (1000 * 60 * 60 * 24);
            daysLeft.setText(days > 0 ? days + " days left" : "Expired");
        } else {
            daysLeft.setText("No deadline");
        }

        HBox footerInfo = new HBox(10, new Region(), daysLeft);
        HBox.setHgrow(footerInfo.getChildren().get(0), Priority.ALWAYS);
        footerInfo.setAlignment(Pos.CENTER_LEFT);

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

        if (SessionManager.isEmployer()) {
            actionBox.getChildren().addAll(detailsBtn);
        } else {
            actionBox.getChildren().addAll(applyBtn, detailsBtn);
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

    @FXML
    private void handleBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/companies.fxml"));
            javafx.scene.Parent root = loader.load();
            MainApp.changeCenterScene(root, "Companies");
        } catch (Exception e) {
            e.printStackTrace();
            MainApp.changeScene("Home.fxml", "Home");
        }
    }
}
