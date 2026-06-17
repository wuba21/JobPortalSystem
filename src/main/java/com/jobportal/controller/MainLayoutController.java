package com.jobportal.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.BorderPane;
import com.jobportal.util.SessionManager;
import javafx.scene.control.MenuItem;
import com.jobportal.MainApp;

public class MainLayoutController {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private MenuButton profileMenu;
    @FXML
    private MenuItem menuDashboard;
    @FXML
    private Button btnSignIn;

    @FXML
    public void initialize() {
        updateAuthButtons();
    }

    public void updateAuthButtons() {
        if (SessionManager.getUser() != null) {
            btnSignIn.setVisible(false);
            btnSignIn.setManaged(false);
            if (profileMenu != null) {
                profileMenu.setText("👤 " + SessionManager.getUser().getFullName());
                profileMenu.setVisible(true);
                profileMenu.setManaged(true);
            }
            if (menuDashboard != null) {
                menuDashboard.setVisible(true);
            }
        } else {
            btnSignIn.setVisible(true);
            btnSignIn.setManaged(true);
            if (profileMenu != null) {
                profileMenu.setVisible(false);
                profileMenu.setManaged(false);
            }
            if (menuDashboard != null) {
                menuDashboard.setVisible(false);
            }
        }
    }

    public void setCenterContent(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            mainBorderPane.setCenter(root);
            updateAuthButtons();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCenterContent(Parent root) {
        mainBorderPane.setCenter(root);
        updateAuthButtons();
    }

    @FXML
    private void handleNavHome() {
        setCenterContent("Home.fxml");
    }

    @FXML
    private void handleNavJobs() {
        setCenterContent("job_dashboard.fxml");
    }

    @FXML
    private void handleNavCompanies() {
        setCenterContent("companies.fxml");
    }

    @FXML
    private void handleNavContact() {
        setCenterContent("contact.fxml");
    }

    @FXML
    private void handleNavAbout() {
        setCenterContent("about.fxml");
    }

    @FXML
    private void handleSignIn() {
        setCenterContent("login.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        setCenterContent("login.fxml");
    }

    @FXML
    private void handleViewProfile() {
        MainApp.changeScene("profile.fxml", "My Profile");
    }

    @FXML
    private void handleSavedJobs() {
        setCenterContent("saved_jobs.fxml");
    }

    @FXML
    private void handleNotifications() {
        MainApp.changeScene("notifications.fxml", "Notifications");
    }

    @FXML
    private void handleMessages() {
        MainApp.changeScene("messages.fxml", "Messages");
    }

    @FXML
    private void handleSettings() {
        setCenterContent("settings.fxml");
    }

    @FXML
    private void handleCVBuilder() {
        MainApp.changeScene("cv_builder.fxml", "CV Builder");
    }

    @FXML
    private void handleDashboard() {
        if (SessionManager.isEmployer()) {
            MainApp.setRoot("employer_dashboard.fxml", "Employer Dashboard");
        } else if (SessionManager.isAdmin()) {
            MainApp.changeScene("admin_dashboard.fxml", "Admin Dashboard");
        } else {
            MainApp.changeScene("dashboard.fxml", "Dashboard");
        }
    }
}
