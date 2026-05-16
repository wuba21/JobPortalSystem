package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.exception.ValidationException;
import com.jobportal.model.User;
import com.jobportal.service.UserService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private ToggleButton showPasswordBtn;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator loginProgress;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
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
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        loginButton.setDisable(true);
        loginProgress.setVisible(true);

        // Run database authentication inside a JavaFX Task (Multithreaded)
        Task<User> loginTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                return userService.login(email, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            loginButton.setDisable(false);
            loginProgress.setVisible(false);
            User user = loginTask.getValue();
            SessionManager.setCurrentUser(user);
            AlertUtil.showInfo("Success", "Welcome back, " + user.getFullName() + "!");
            if (SessionManager.isAdmin()) {
                MainApp.changeScene("dashboard.fxml", "Dashboard Overview");
            } else if (SessionManager.isEmployer()) {
                MainApp.changeScene("dashboard.fxml", "Employer Dashboard");
            } else {
                MainApp.changeScene("dashboard.fxml", "Dashboard Overview");
            }
        });

        loginTask.setOnFailed(event -> {
            loginButton.setDisable(false);
            loginProgress.setVisible(false);
            Throwable ex = loginTask.getException();
            if (ex instanceof ValidationException) {
                AlertUtil.showError("Login Failed", ex.getMessage());
            } else {
                AlertUtil.showError("Login Error", "An unexpected error occurred.");
                ex.printStackTrace();
            }
        });

        Thread bgThread = new Thread(loginTask);
        bgThread.setDaemon(true);
        bgThread.start();
    }

    @FXML
    private void handleRegister() {
        MainApp.changeScene("register.fxml", "Register");
    }

    @FXML
    private void handleForgotPassword() {
        MainApp.changeScene("forgot_password.fxml", "Forgot Password");
    }

    @FXML
    private void handleBackToHome() {
        MainApp.changeScene("Home.fxml", "Home");
    }
}
