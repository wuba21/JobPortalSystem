package com.jobportal.controller;

import com.jobportal.config.DBConnection;
import com.jobportal.util.AlertUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CompaniesController {

    @FXML private FlowPane companiesContainer;

    @FXML
    public void initialize() {
        loadCompanies();
    }

    private void loadCompanies() {
        Task<List<CompanyData>> task = new Task<>() {
            @Override
            protected List<CompanyData> call() throws Exception {
                List<CompanyData> list = new ArrayList<>();
                String sql = "SELECT e.id, e.company_name, e.company_description, COUNT(j.id) as job_count " +
                             "FROM employers e " +
                             "LEFT JOIN jobs j ON e.id = j.employer_id AND j.is_active = TRUE " +
                             "GROUP BY e.id, e.company_name, e.company_description " +
                             "ORDER BY job_count DESC, e.company_name ASC";
                try (Connection conn = DBConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        list.add(new CompanyData(
                                rs.getInt("id"),
                                rs.getString("company_name"),
                                rs.getString("company_description"),
                                rs.getInt("job_count")
                        ));
                    }
                }
                return list;
            }
        };

        task.setOnSucceeded(e -> {
            companiesContainer.getChildren().clear();
            List<CompanyData> companies = task.getValue();
            
            // Define some colors for the circles
            String[] bgColors = {"#e0f2fe", "#fce7f3", "#fef3c7", "#dbeafe", "#d1fae5"};
            String[] textColors = {"#0ea5e9", "#db2777", "#d97706", "#2563eb", "#059669"};

            for (int i = 0; i < companies.size(); i++) {
                CompanyData c = companies.get(i);
                
                VBox card = new VBox(15);
                card.setAlignment(Pos.CENTER);
                card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 5); -fx-cursor: hand;");
                card.setPrefWidth(240.0);

                StackPane circlePane = new StackPane();
                
                card.setOnMouseClicked(event -> {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/company_details.fxml"));
                        javafx.scene.Parent root = loader.load();
                        CompanyDetailsController controller = loader.getController();
                        controller.setCompanyData(c.id, c.name, c.description, c.jobCount);
                        com.jobportal.MainApp.changeCenterScene(root, c.name + " Details");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        AlertUtil.showError("Error", "Could not load company details.");
                    }
                });

                Circle circle = new Circle(40.0, Color.web(bgColors[i % bgColors.length]));
                Label initialLabel = new Label(c.name.substring(0, 1).toUpperCase());
                initialLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + textColors[i % textColors.length] + ";");
                
                circlePane.getChildren().addAll(circle, initialLabel);

                Label nameLabel = new Label(c.name);
                nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

                Label descLabel = new Label(c.description != null ? (c.description.length() > 25 ? c.description.substring(0, 25) + "..." : c.description) : "Employer");
                descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-alignment: center;");
                descLabel.setWrapText(true);

                Label jobsLabel = new Label(c.jobCount + " Open Jobs");
                jobsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #059669;");

                card.getChildren().addAll(circlePane, nameLabel, descLabel, jobsLabel);
                companiesContainer.getChildren().add(card);
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            System.err.println("Failed to load companies");
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private static class CompanyData {
        int id;
        String name;
        String description;
        int jobCount;

        CompanyData(int id, String name, String description, int jobCount) {
            this.id = id;
            this.name = name != null ? name : "Unknown";
            this.description = description;
            this.jobCount = jobCount;
        }
    }
}
