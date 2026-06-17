package com.jobportal;

import com.jobportal.controller.MainLayoutController;
import com.jobportal.controller.EmployerDashboardController;
import com.jobportal.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static MainLayoutController mainController;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_layout.fxml"));
        Parent root = loader.load();
        mainController = loader.getController();
        
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle("Job Portal System");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
        
        mainController.setCenterContent("Home.fxml");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void changeScene(String fxmlFile, String title) {
        if (mainController != null) {
            primaryStage.setTitle("Job Portal - " + title);
            mainController.setCenterContent(fxmlFile);
        } else if (SessionManager.isEmployer() && EmployerDashboardController.getInstance() != null) {
            primaryStage.setTitle("Job Portal - " + title);
            EmployerDashboardController.getInstance().loadCenterPage(fxmlFile);
        } else {
            // If main layout is not loaded (e.g. from standalone dashboard), restore it first
            restoreMainLayout(fxmlFile, title);
        }
    }
    
    public static void changeCenterScene(Parent root, String title) {
        if (mainController != null) {
            primaryStage.setTitle("Job Portal - " + title);
            mainController.setCenterContent(root);
        } else if (SessionManager.isEmployer() && EmployerDashboardController.getInstance() != null) {
            primaryStage.setTitle("Job Portal - " + title);
            EmployerDashboardController.getInstance().loadCenterPage(root);
        }
    }

    public static void setRoot(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
            primaryStage.setTitle("Job Portal - " + title);
            mainController = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void restoreMainLayout(String initialCenterFxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/main_layout.fxml"));
            Parent root = loader.load();
            mainController = loader.getController();
            primaryStage.getScene().setRoot(root);
            primaryStage.setTitle("Job Portal - " + title);
            mainController.setCenterContent(initialCenterFxml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goBackToDashboard() {
        if (SessionManager.isAdmin()) {
            // Admin always goes to admin dashboard; restore main layout if needed
            if (mainController != null) {
                mainController.setCenterContent("admin_dashboard.fxml");
                primaryStage.setTitle("Job Portal - Admin Dashboard");
            } else {
                restoreMainLayout("admin_dashboard.fxml", "Admin Dashboard");
            }
        } else if (SessionManager.isEmployer()) {
            if (mainController == null && EmployerDashboardController.getInstance() != null) {
                // Already inside the standalone employer dashboard — just swap center
                EmployerDashboardController.getInstance().loadCenterPage("employer_overview.fxml");
            } else {
                // Either coming from main_layout (public pages) or a cold start —
                // swap root to the full standalone employer dashboard
                setRoot("employer_dashboard.fxml", "Employer Dashboard");
            }
        } else {
            // Job Seeker — keep (or restore) main layout and show dashboard
            if (mainController != null) {
                mainController.setCenterContent("dashboard.fxml");
                primaryStage.setTitle("Job Portal - Dashboard");
            } else {
                restoreMainLayout("dashboard.fxml", "Dashboard Overview");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
