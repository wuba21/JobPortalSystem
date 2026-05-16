package com.jobportal.controller;

import com.jobportal.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class GroupController {

    @FXML private ImageView logoImageView;
    @FXML private GridPane membersGrid;

    @FXML
    public void initialize() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/images/uog.jpg");
            if (imageStream != null) {
                logoImageView.setImage(new Image(imageStream));
            } else {
                System.err.println("Warning: Logo image '/images/uog.jpg' not found.");
            }
        } catch (Exception e) {
            System.err.println("Error loading university logo: " + e.getMessage());
        }
        List<Member> groupMembers = Arrays.asList(
                new Member("Kirubel Melaku", "01336/16"),
                new Member("Tilahun Misikir", "02870/16"),
                new Member("Mihretu Muluneh", "00341/16"),
                new Member("Wubante Tilahun", "02170/16"),
                new Member("Melkamu Antehun", "02418/16"),
                new Member("Biruk Getaneh", "01241/16")
        );
        membersGrid.getChildren().clear();

        Label nameHeader = new Label("Name");
        nameHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #000000; -fx-underline: true;");

        Label idHeader = new Label("ID");
        idHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #000000; -fx-underline: true;");

        membersGrid.add(nameHeader, 0, 0);
        membersGrid.add(idHeader, 1, 0);

        int row = 1;
        for (Member m : groupMembers) {
            Label nameLabel = new Label(m.getName());
            nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");

            Label idLabel = new Label(m.getId());
            idLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");

            membersGrid.add(nameLabel, 0, row);
            membersGrid.add(idLabel, 1, row);
            row++;
        }
    }

    @FXML
    private void handleClose() {
        MainApp.changeScene("Home.fxml", "Home");
    }

    /**
     * Inner Model Class for Group Members explicitly reduced to core properties
     */
    public static class Member {
        private final String name;
        private final String id;

        public Member(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }
    }
}
