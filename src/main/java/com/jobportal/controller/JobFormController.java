package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.exception.ValidationException;
import com.jobportal.model.Job;
import com.jobportal.service.JobService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.Date;

public class JobFormController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextArea requirementsField;
    @FXML private TextField locationField;
    @FXML private TextField salaryMinField;
    @FXML private TextField salaryMaxField;
    @FXML private ComboBox<String> jobTypeCombo;
    @FXML private TextField categoryField;
    @FXML private ComboBox<String> experienceCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker deadlinePicker;

    @FXML private TextField contactEmailField;
    @FXML private TextField telegramLinkField;
    @FXML private TextField websiteLinkField;
    @FXML private TextField linkedinLinkField;

    @FXML private TextField departmentField;
    @FXML private TextField reportsToField;
    @FXML private TextField requiredNumberField;
    @FXML private TextArea educationQualificationField;

    private final JobService jobService = new JobService();

    private Job currentEditJob;

    @FXML
    public void initialize() {
        jobTypeCombo.getItems().addAll("FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP", "REMOTE");
        jobTypeCombo.setValue("FULL_TIME");

        experienceCombo.getItems().addAll("ENTRY", "MID", "SENIOR", "EXECUTIVE");
        experienceCombo.setValue("ENTRY");

        // Check if we are editing an existing job
        currentEditJob = SessionManager.getCurrentJob();
        if (currentEditJob != null) {
            titleField.setText(currentEditJob.getTitle());
            descriptionField.setText(currentEditJob.getDescription());
            requirementsField.setText(currentEditJob.getRequirements());
            locationField.setText(currentEditJob.getLocation());
            if (currentEditJob.getJobType() != null) jobTypeCombo.setValue(currentEditJob.getJobType());
            categoryField.setText(currentEditJob.getCategory());
            if (currentEditJob.getExperienceLevel() != null) experienceCombo.setValue(currentEditJob.getExperienceLevel());
            
            
            contactEmailField.setText(currentEditJob.getContactEmail());
            telegramLinkField.setText(currentEditJob.getTelegramLink());
            websiteLinkField.setText(currentEditJob.getWebsiteLink());
            linkedinLinkField.setText(currentEditJob.getLinkedinLink());
            
            departmentField.setText(currentEditJob.getDepartment());
            reportsToField.setText(currentEditJob.getReportsTo());
            requiredNumberField.setText(String.valueOf(currentEditJob.getRequiredNumber()));
            educationQualificationField.setText(currentEditJob.getEducationQualification());
            
            if (currentEditJob.getSalaryMin() != null) salaryMinField.setText(currentEditJob.getSalaryMin().toString());
            if (currentEditJob.getSalaryMax() != null) salaryMaxField.setText(currentEditJob.getSalaryMax().toString());
            if (currentEditJob.getStartDate() != null) startDatePicker.setValue(currentEditJob.getStartDate().toLocalDate());
            if (currentEditJob.getDeadline() != null) deadlinePicker.setValue(currentEditJob.getDeadline().toLocalDate());
        }
    }

    @FXML
    private void handlePostJob() {
        Job job = currentEditJob != null ? currentEditJob : new Job();
        
        int empId = SessionManager.getEmployerId();
        if (empId <= 0 && SessionManager.isAdmin()) {
            empId = getOrCreateAdminEmployer();
            if (empId > 0) SessionManager.setEmployerId(empId);
        }
        
        if (empId <= 0 && currentEditJob == null) {
            AlertUtil.showError("Error", "You must be associated with an employer to post jobs.");
            return;
        }
        
        if (currentEditJob == null) job.setEmployerId(empId);
        
        job.setTitle(titleField.getText().trim());
        job.setDescription(descriptionField.getText().trim());
        job.setRequirements(requirementsField.getText().trim());
        job.setLocation(locationField.getText().trim());
        job.setJobType(jobTypeCombo.getValue());
        job.setCategory(categoryField.getText().trim());
        job.setExperienceLevel(experienceCombo.getValue());
        
        if (contactEmailField.getText() != null && !contactEmailField.getText().trim().isEmpty()) {
            if (!com.jobportal.util.ValidationUtil.isValidEmail(contactEmailField.getText().trim())) {
                AlertUtil.showError("Validation Error", "Please provide a valid contact email address.");
                return;
            }
            job.setContactEmail(contactEmailField.getText().trim());
        } else {
            job.setContactEmail(null);
        }
        
        job.setTelegramLink(telegramLinkField.getText() != null ? telegramLinkField.getText().trim() : null);
        job.setWebsiteLink(websiteLinkField.getText() != null ? websiteLinkField.getText().trim() : null);
        job.setLinkedinLink(linkedinLinkField.getText() != null ? linkedinLinkField.getText().trim() : null);
        
        job.setDepartment(departmentField.getText() != null ? departmentField.getText().trim() : null);
        job.setReportsTo(reportsToField.getText() != null ? reportsToField.getText().trim() : null);
        job.setEducationQualification(educationQualificationField.getText() != null ? educationQualificationField.getText().trim() : null);
        
        try {
            if (requiredNumberField.getText() != null && !requiredNumberField.getText().trim().isEmpty()) {
                job.setRequiredNumber(Integer.parseInt(requiredNumberField.getText().trim()));
            } else {
                job.setRequiredNumber(1);
            }
        } catch (NumberFormatException e) {
            job.setRequiredNumber(1);
        }

        try {
            if (!salaryMinField.getText().trim().isEmpty()) {
                job.setSalaryMin(new BigDecimal(salaryMinField.getText().trim()));
            }
            if (!salaryMaxField.getText().trim().isEmpty()) {
                job.setSalaryMax(new BigDecimal(salaryMaxField.getText().trim()));
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Error", "Please enter valid salary amounts.");
            return;
        }

        if (startDatePicker.getValue() != null) {
            job.setStartDate(Date.valueOf(startDatePicker.getValue()));
        }

        if (deadlinePicker.getValue() != null) {
            job.setDeadline(Date.valueOf(deadlinePicker.getValue()));
        }

        try {
            boolean success;
            if (currentEditJob != null) {
                success = jobService.update(job);
            } else {
                success = jobService.createJob(job);
            }
            
            if (success) {
                boolean wasUpdate = (currentEditJob != null);
                AlertUtil.showInfo("Success", wasUpdate ? "Job updated successfully!" : "Job posted successfully!");
                SessionManager.setCurrentJob(null);
                if (SessionManager.isAdmin()) {
                    if (wasUpdate) {
                        MainApp.changeScene("admin_dashboard.fxml", "Admin Dashboard");
                    } else {
                        MainApp.changeScene("dashboard.fxml", "Dashboard");
                    }
                } else if (SessionManager.isEmployer()) {
                    MainApp.changeScene("employer_dashboard.fxml", "Employer Dashboard");
                } else {
                    MainApp.changeScene("dashboard.fxml", "Dashboard");
                }
            } else {
                AlertUtil.showError("Error", "Failed to save job.");
            }
        } catch (ValidationException e) {
            AlertUtil.showError("Validation Error", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        if (SessionManager.isAdmin()) {
            if (currentEditJob == null) {
                MainApp.changeScene("dashboard.fxml", "Dashboard");
            } else {
                MainApp.changeScene("admin_dashboard.fxml", "Admin Dashboard");
            }
        } else if (SessionManager.isEmployer()) {
            MainApp.changeScene("employer_dashboard.fxml", "Employer Dashboard");
        } else {
            MainApp.changeScene("dashboard.fxml", "Dashboard");
        }
    }

    private int getOrCreateAdminEmployer() {
        int userId = SessionManager.getCurrentUser().getId();
        int empId = -1;
        String selectSql = "SELECT id FROM employers WHERE user_id = ?";
        try (java.sql.Connection conn = com.jobportal.config.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, userId);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                empId = rs.getInt("id");
            } else {
                String insertSql = "INSERT INTO employers (user_id, company_name, company_description, industry) VALUES (?, ?, ?, ?)";
                try (java.sql.PreparedStatement insertStmt = conn.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, "Job Portal Admin");
                    insertStmt.setString(3, "System Administrator");
                    insertStmt.setString(4, "Administration");
                    int rows = insertStmt.executeUpdate();
                    if (rows > 0) {
                        java.sql.ResultSet keys = insertStmt.getGeneratedKeys();
                        if (keys.next()) empId = keys.getInt(1);
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return empId;
    }
}
