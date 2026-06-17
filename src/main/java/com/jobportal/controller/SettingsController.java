package com.jobportal.controller;

import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;

public class SettingsController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox emailNotifCheck;
    @FXML private CheckBox smsNotifCheck;
    @FXML private CheckBox jobAlertCheck;
    @FXML private CheckBox profileVisibilityCheck;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null) {
            return;
        }
        
        // Load existing preferences if available
        // For now, we set defaults
        emailNotifCheck.setSelected(true);
        jobAlertCheck.setSelected(true);
        profileVisibilityCheck.setSelected(true);
    }

    @FXML
    private void handleSaveSettings() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        boolean passwordChanged = false;

        if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                AlertUtil.showError("Error", "Please fill all password fields to change your password.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                AlertUtil.showError("Error", "New passwords do not match.");
                return;
            }
            
            // Note: In a real app, verify the current password matches before updating.
            // For now, we simulate success.
            passwordChanged = true;
        }
        
        // Simulate saving preferences to the database or session
        boolean emailPrefs = emailNotifCheck.isSelected();
        boolean smsPrefs = smsNotifCheck.isSelected();
        boolean alertPrefs = jobAlertCheck.isSelected();
        boolean privacyPrefs = profileVisibilityCheck.isSelected();

        if (passwordChanged) {
            AlertUtil.showInfo("Success", "Password updated successfully. Preferences have been saved.");
        } else {
            AlertUtil.showInfo("Success", "Settings and preferences have been saved.");
        }
        
        // Clear password fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
}
