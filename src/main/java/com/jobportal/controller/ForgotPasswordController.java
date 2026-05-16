package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.exception.ValidationException;
import com.jobportal.service.UserService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.EmailUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

public class ForgotPasswordController {

    @FXML private VBox step1Box;
    @FXML private VBox step2Box;

    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button sendOtpButton;
    @FXML private ProgressIndicator otpProgress;

    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;
    @FXML private ProgressIndicator resetProgress;

    @FXML private TextField newPasswordTextField;
    @FXML private ToggleButton showNewPasswordBtn;
    @FXML private TextField confirmPasswordTextField;
    @FXML private ToggleButton showConfirmPasswordBtn;

    private final UserService userService = new UserService();
    private String targetEmail;

    @FXML
    public void initialize() {
        if (newPasswordTextField != null && newPasswordField != null) {
            newPasswordTextField.textProperty().bindBidirectional(newPasswordField.textProperty());
        }
        if (confirmPasswordTextField != null && confirmPasswordField != null) {
            confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        }
    }

    @FXML
    private void handleSendOtp() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (email.isEmpty() || phone.isEmpty()) {
            AlertUtil.showError("Error", "Please enter both Email and Phone.");
            return;
        }

        // Verify if user exists first
        if (!userService.checkEmailAndPhoneMatch(email, phone)) {
            AlertUtil.showError("Error", "Email and phone do not match our records.");
            return;
        }

        targetEmail = email;

        // Show instruction popup
        AlertUtil.showInfo("Instructions", 
            "1. Go to your Google Account (Security Section).\n" +
            "2. Generate a 16-character 'App Password'.\n" +
            "3. Copy that password and paste it in the next screen.");

        // Move to next step
        step1Box.setVisible(false);
        step1Box.setManaged(false);
        step2Box.setVisible(true);
        step2Box.setManaged(true);
    }

    @FXML
    private void handleResetPassword() {
        String appPassword = otpField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (appPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.showError("Error", "All fields are required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            AlertUtil.showError("Error", "Passwords do not match.");
            return;
        }

        resetButton.setDisable(true);
        resetProgress.setVisible(true);

        // Verification Task
        Task<Boolean> verifyTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Verify ownership using the provided App Password
                boolean isValid = EmailUtil.verifyAppPassword(targetEmail, appPassword);
                if (!isValid) {
                    throw new Exception("Invalid Google App Password. Verification Failed.");
                }
                // If valid, update database
                return userService.resetPassword(targetEmail, newPassword);
            }
        };

        verifyTask.setOnSucceeded(event -> {
            resetButton.setDisable(false);
            resetProgress.setVisible(false);
            if (verifyTask.getValue()) {
                AlertUtil.showInfo("Success", "Password reset successfully!");
                handleBackToLogin();
            }
        });

        verifyTask.setOnFailed(event -> {
            resetButton.setDisable(false);
            resetProgress.setVisible(false);
            AlertUtil.showError("Verification Failed", verifyTask.getException().getMessage());
        });

        new Thread(verifyTask).start();
    }

    @FXML
    private void toggleNewPasswordVisibility() {
        if (showNewPasswordBtn.isSelected()) {
            newPasswordTextField.setVisible(true);
            newPasswordTextField.setManaged(true);
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            showNewPasswordBtn.setText("🙈");
        } else {
            newPasswordTextField.setVisible(false);
            newPasswordTextField.setManaged(false);
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            showNewPasswordBtn.setText("👁");
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        if (showConfirmPasswordBtn.isSelected()) {
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            showConfirmPasswordBtn.setText("🙈");
        } else {
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            showConfirmPasswordBtn.setText("👁");
        }
    }

    @FXML
    private void handleBackToLogin() {
        MainApp.changeScene("login.fxml", "Login");
    }
}
