package com.jobportal.controller;

import com.jobportal.MainApp;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import java.io.IOException;

public class HomeController {

    // 🔷 LOGIN BUTTON
    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        MainApp.changeScene("login.fxml", "Login");
    }

    // 🔷 REGISTER BUTTON
    @FXML
    private void handleRegister(ActionEvent event) throws IOException {
        MainApp.changeScene("register.fxml", "Register");
    }

    // 🔷 BROWSE JOBS (PUBLIC VIEW)
    @FXML
    private void handleBrowseJobs(ActionEvent event) throws IOException {
        MainApp.changeScene("job_dashboard.fxml", "Browse Jobs");
    }

    // 🔷 POST JOB (LOGIN REQUIRED LOGIC)
    @FXML
    private void handlePostJob(ActionEvent event) throws IOException {
        // Simple check (later replace with SessionManager)
        boolean isLoggedIn = false;

        if (!isLoggedIn) {
            MainApp.changeScene("login.fxml", "Login Required");
        } else {
            MainApp.changeScene("job-form.fxml", "Post Job");
        }
    }

    // 🔷 SHOW GROUP MEMBERS 
    @FXML
    private void handleShowGroup(ActionEvent event) throws IOException {
        MainApp.changeScene("group.fxml", "Group Members");
    }

    // 🔷 FOOTER NAVIGATION
    @FXML
    private void handleNavHome(javafx.scene.input.MouseEvent event) {
        MainApp.changeScene("Home.fxml", "Home");
    }

    @FXML
    private void handleNavJobs(javafx.scene.input.MouseEvent event) {
        MainApp.changeScene("job_dashboard.fxml", "Browse Jobs");
    }

    @FXML
    private void handleNavAbout(javafx.scene.input.MouseEvent event) {
        MainApp.changeScene("about.fxml", "About Us");
    }

    @FXML
    private void handleNavContact(javafx.scene.input.MouseEvent event) {
        MainApp.changeScene("contact.fxml", "Contact Us");
    }

    @FXML
    private void handleFooterHover(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Label lbl = (javafx.scene.control.Label) event.getSource();
        lbl.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 14px; -fx-cursor: hand; -fx-underline: true;");
    }

    @FXML
    private void handleFooterExit(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Label lbl = (javafx.scene.control.Label) event.getSource();
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-cursor: hand; -fx-underline: false;");
    }

    @FXML
    private void handleIconHover(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Label lbl = (javafx.scene.control.Label) event.getSource();
        lbl.setScaleX(1.2);
        lbl.setScaleY(1.2);
    }

    @FXML
    private void handleIconExit(javafx.scene.input.MouseEvent event) {
        javafx.scene.control.Label lbl = (javafx.scene.control.Label) event.getSource();
        lbl.setScaleX(1.0);
        lbl.setScaleY(1.0);
    }
}