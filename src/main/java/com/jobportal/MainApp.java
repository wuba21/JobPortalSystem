package com.jobportal;

import com.jobportal.controller.MainLayoutController;
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
        }
    }
    
    public static void changeCenterScene(Parent root, String title) {
        if (mainController != null) {
            primaryStage.setTitle("Job Portal - " + title);
            mainController.setCenterContent(root);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
