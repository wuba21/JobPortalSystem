package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.config.DBConnection;
import com.jobportal.model.User;
import com.jobportal.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextArea addressField;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;

    @FXML
    public void initialize() {
        genderCombo.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        loadUserData();
    }

    private void loadUserData() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            fullNameField.setText(user.getFullName() != null ? user.getFullName() : "");
            emailField.setText(user.getEmail() != null ? user.getEmail() : "");
            
            // Try to load extra data not fully populated in SessionManager.User if any
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT phone, address, gender FROM users WHERE id = ?")) {
                stmt.setInt(1, user.getId());
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    phoneField.setText(rs.getString("phone") != null ? rs.getString("phone") : "");
                    addressField.setText(rs.getString("address") != null ? rs.getString("address") : "");
                    String gender = rs.getString("gender");
                    if (gender != null && !gender.isEmpty()) {
                        genderCombo.setValue(gender);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSaveProfile() {
        if (fullNameField.getText().trim().isEmpty()) {
            showStatus("Full Name is required", false);
            return;
        }

        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        String sql = "UPDATE users SET full_name = ?, phone = ?, address = ?, gender = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fullNameField.getText().trim());
            stmt.setString(2, phoneField.getText().trim());
            stmt.setString(3, addressField.getText().trim());
            stmt.setString(4, genderCombo.getValue());
            stmt.setInt(5, user.getId());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                user.setFullName(fullNameField.getText().trim()); // Update session object
                MainApp.changeScene("profile.fxml", "My Profile"); // Refresh
                showStatus("Profile updated successfully!", true);
            } else {
                showStatus("Failed to update profile.", false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showStatus("Database error occurred.", false);
        }
    }

    @FXML
    private void handleBack() {
        MainApp.goBackToDashboard();
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setVisible(true);
        statusLabel.setText(message);
        if (success) {
            statusLabel.setTextFill(Color.GREEN);
        } else {
            statusLabel.setTextFill(Color.RED);
        }
    }
}
