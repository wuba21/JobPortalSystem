package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.model.Job;
import com.jobportal.service.ApplicationService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.text.SimpleDateFormat;

public class JobDetailsController {

    @FXML private Label lblJobTitle;
    @FXML private Label lblCompanyName;
    @FXML private Label lblDatePosted;
    @FXML private Label lblSalaryHeader; 
    @FXML private Label lblDescription;
    
    @FXML private Label lblDepartment;
    @FXML private Label lblReportsTo;
    @FXML private Label lblRequiredNumber;
    @FXML private Label lblEducation;
    
    @FXML private Label lblLocation;
    @FXML private Label lblEmploymentType;
    @FXML private Label lblCareerLevel;
    @FXML private Label lblShift;
    
    @FXML private Button btnApply;

    private Job currentJob;
    private final ApplicationService applicationService = new ApplicationService();

    @FXML
    public void initialize() {
        // Auto-load job from SessionManager (when navigated from recommendations or job list)
        com.jobportal.model.Job jobFromSession = SessionManager.getCurrentJob();
        if (jobFromSession != null) {
            setJob(jobFromSession);
        }

        // Add a nice fade-in animation
        try {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), lblJobTitle.getParent().getParent());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } catch (Exception ignored) {}
    }

    public void setJob(Job job) {
        this.currentJob = job;
        if (job == null) return;

        lblJobTitle.setText(job.getTitle());
        
        lblCompanyName.setText(job.getCompanyName() != null ? job.getCompanyName() : "Company Unknown");
        if (job.getEmployerId() <= 0) {
            lblCompanyName.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b;");
        } else {
            lblCompanyName.setStyle("-fx-font-size: 18px; -fx-text-fill: #3b82f6; -fx-cursor: hand;");
        }
        
        if (job.getPostedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            lblDatePosted.setText("Date Posted: " + sdf.format(job.getPostedAt()));
        } else {
            lblDatePosted.setText("Date Posted: Not Available");
        }

        String salary = "Negotiable";
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            salary = "$" + job.getSalaryMin() + " - $" + job.getSalaryMax();
        } else if (job.getSalaryMin() != null) {
            salary = "From $" + job.getSalaryMin();
        }
        if (lblSalaryHeader != null) lblSalaryHeader.setText(" Salary: " + salary);

        lblDescription.setText(job.getDescription() != null ? job.getDescription() : "No description provided.");
        
        lblDepartment.setText(job.getDepartment() != null && !job.getDepartment().isEmpty() ? job.getDepartment() : "General");
        lblReportsTo.setText(job.getReportsTo() != null && !job.getReportsTo().isEmpty() ? job.getReportsTo() : "Manager");
        lblRequiredNumber.setText(String.valueOf(job.getRequiredNumber() > 0 ? job.getRequiredNumber() : 1));
        lblEducation.setText(job.getEducationQualification() != null && !job.getEducationQualification().isEmpty() ? job.getEducationQualification() : "Not Specified");
        
        lblLocation.setText(job.getLocation() != null ? job.getLocation() : "Remote / Unknown");
        lblEmploymentType.setText(job.getJobType() != null ? job.getJobType() : "Full-Time");
        lblShift.setText(job.getShift() != null && !job.getShift().isEmpty() ? job.getShift() : "Day Shift");
        lblCareerLevel.setText(job.getExperienceLevel() != null ? job.getExperienceLevel() : "Mid-Level");

        // Update Apply button status
        if (SessionManager.getCurrentUser() == null) {
            btnApply.setText("Login to Apply");
            btnApply.setOnAction(e -> MainApp.changeScene("login.fxml", "Login"));
        } else if (SessionManager.isEmployer()) {
            btnApply.setVisible(false);
            btnApply.setManaged(false);
        } else {
            boolean applied = applicationService.hasApplied(SessionManager.getCurrentUser().getId(), job.getId());
            if (applied) {
                btnApply.setText("Already Applied");
                btnApply.setDisable(true);
            }
        }
    }

    @FXML
    private void handleApply() {
        if (currentJob != null) {
            SessionManager.setCurrentJob(currentJob);
            MainApp.changeScene("apply-form.fxml", "Apply for Job");
        }
    }

    @FXML
    private void handleBack() {
        // If job seeker came from the dashboard (recommendations), go back to dashboard
        // Otherwise go to job browse page
        if (SessionManager.isJobSeeker()) {
            MainApp.changeScene("dashboard.fxml", "Dashboard");
        } else {
            MainApp.changeScene("job_dashboard.fxml", "Browse Jobs");
        }
    }

    @FXML
    private void handleCompanyClick() {
        if (currentJob != null && currentJob.getEmployerId() > 0) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/company_details.fxml"));
                javafx.scene.Parent root = loader.load();
                CompanyDetailsController controller = loader.getController();
                controller.setCompanyId(currentJob.getEmployerId());
                MainApp.changeCenterScene(root, "Company Details");
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtil.showError("Error", "Could not load company details.");
            }
        }
    }

    @FXML
    private void handleCompanyEnter() {
        if (currentJob != null && currentJob.getEmployerId() > 0) {
            lblCompanyName.setStyle("-fx-font-size: 18px; -fx-text-fill: #2563eb; -fx-underline: true; -fx-cursor: hand;");
        }
    }

    @FXML
    private void handleCompanyExit() {
        if (currentJob != null && currentJob.getEmployerId() > 0) {
            lblCompanyName.setStyle("-fx-font-size: 18px; -fx-text-fill: #3b82f6; -fx-underline: false; -fx-cursor: hand;");
        }
    }

    @FXML
    private void handleFacebook() {
        String url = (currentJob != null && currentJob.getWebsiteLink() != null && !currentJob.getWebsiteLink().trim().isEmpty()) ? 
            currentJob.getWebsiteLink() : "https://web.facebook.com/profile.php?id=100077897349623";
        openWebpage(url);
    }

    @FXML
    private void handleLinkedIn() {
        String url = (currentJob != null && currentJob.getLinkedinLink() != null && !currentJob.getLinkedinLink().trim().isEmpty()) ? 
            currentJob.getLinkedinLink() : "https://www.linkedin.com/in/wubante-tilahun-776ab7377/";
        openWebpage(url);
    }

    @FXML
    private void handleTelegram() {
        openWebpage(currentJob != null && currentJob.getTelegramLink() != null ? currentJob.getTelegramLink() : "https://telegram.org");
    }

    private void openWebpage(String urlString) {
        try {
            if (urlString != null && !urlString.trim().isEmpty()) {
                if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                    urlString = "https://" + urlString;
                }
                java.awt.Desktop.getDesktop().browse(new java.net.URI(urlString));
            }
        } catch (Exception e) {
            System.err.println("Failed to open link: " + e.getMessage());
        }
    }
}
