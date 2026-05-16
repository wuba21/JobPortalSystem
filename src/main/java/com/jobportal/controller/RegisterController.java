package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.config.DBConnection;
import com.jobportal.exception.ValidationException;
import com.jobportal.model.User;
import com.jobportal.service.UserService;
import com.jobportal.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> userTypeCombo;
    @FXML private TextField companyNameField;
    @FXML private TextField industryField;

    private final UserService userService = new UserService();

    @FXML private TextField passwordTextField;
    @FXML private ToggleButton showPasswordBtn;
    @FXML private TextField confirmPasswordTextField;
    @FXML private ToggleButton showConfirmPasswordBtn;

    @FXML
    public void initialize() {
        userTypeCombo.getItems().addAll("JOB_SEEKER", "EMPLOYER");
        userTypeCombo.setValue("JOB_SEEKER");

        genderCombo.getItems().addAll("Male", "Female", "Other");
        genderCombo.setValue("Male");

        userTypeCombo.setOnAction(e -> {
            boolean isEmployer = "EMPLOYER".equals(userTypeCombo.getValue());
            companyNameField.setVisible(isEmployer);
            companyNameField.setManaged(isEmployer);
            industryField.setVisible(isEmployer);
            industryField.setManaged(isEmployer);
        });

        companyNameField.setVisible(false);
        companyNameField.setManaged(false);
        industryField.setVisible(false);
        industryField.setManaged(false);
        
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
        if (confirmPasswordTextField != null && confirmPasswordField != null) {
            confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        if (showPasswordBtn.isSelected()) {
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            showPasswordBtn.setText("🙈");
        } else {
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            showPasswordBtn.setText("👁");
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
    private void handleRegister() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!password.equals(confirmPassword)) {
            AlertUtil.showError("Error", "Passwords do not match.");
            return;
        }

        User user = new User();
        user.setFullName(fullNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPassword(password);
        user.setPhone(phoneField.getText().trim());
        user.setAddress(addressField.getText().trim());
        user.setUserType(userTypeCombo.getValue());
        user.setGender(genderCombo.getValue());

        try {
            boolean success = userService.register(user);
            if (success) {
                // If employer, also insert into employers table
                if ("EMPLOYER".equals(user.getUserType())) {
                    insertEmployer(user.getId(), companyNameField.getText().trim(), industryField.getText().trim());
                }
                AlertUtil.showInfo("Success", "Registration successful! Please login.");
                MainApp.changeScene("login.fxml", "Login");
            } else {
                AlertUtil.showError("Error", "Registration failed. Please try again.");
            }
        } catch (ValidationException e) {
            AlertUtil.showError("Validation Error", e.getMessage());
        }
    }

    private void insertEmployer(int userId, String companyName, String industry) {
        String sql = "INSERT INTO employers (user_id, company_name, industry) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, companyName);
            stmt.setString(3, industry);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Insert employer error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        MainApp.changeScene("login.fxml", "Login");
    }
}
