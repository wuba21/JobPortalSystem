package com.jobportal.controller;

import com.jobportal.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.InputStream;
import java.awt.Desktop;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;

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
                new Member("Wubante Tilahun", "02170/16", "https://web.facebook.com/profile.php?id=100077897349623", "https://www.linkedin.com/in/wubante-tilahun-776ab7377/"),
                new Member("Melkamu Antehun", "02418/16"),
                new Member("Biruk Getaneh", "01241/16")
        );
        membersGrid.getChildren().clear();

        Label nameHeader = new Label("Name");
        nameHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #000000; -fx-underline: true;");

        Label idHeader = new Label("ID");
        idHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #000000; -fx-underline: true;");

        Label socialHeader = new Label("Social Links");
        socialHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #000000; -fx-underline: true;");

        membersGrid.add(nameHeader, 0, 0);
        membersGrid.add(idHeader, 1, 0);
        membersGrid.add(socialHeader, 2, 0);

        int row = 1;
        for (Member m : groupMembers) {
            Label nameLabel = new Label(m.getName());
            nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");

            Label idLabel = new Label(m.getId());
            idLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");

            HBox socialBox = new HBox(10);
            if (m.getFacebookUrl() != null && !m.getFacebookUrl().isEmpty()) {
                Hyperlink fbLink = new Hyperlink("Facebook");
                fbLink.setStyle("-fx-text-fill: #1877f2; -fx-font-weight: bold; -fx-underline: true;");
                fbLink.setOnAction(e -> openUrl(m.getFacebookUrl()));
                socialBox.getChildren().add(fbLink);
            }
            if (m.getLinkedinUrl() != null && !m.getLinkedinUrl().isEmpty()) {
                Hyperlink inLink = new Hyperlink("LinkedIn");
                inLink.setStyle("-fx-text-fill: #0a66c2; -fx-font-weight: bold; -fx-underline: true;");
                inLink.setOnAction(e -> openUrl(m.getLinkedinUrl()));
                socialBox.getChildren().add(inLink);
            }

            membersGrid.add(nameLabel, 0, row);
            membersGrid.add(idLabel, 1, row);
            membersGrid.add(socialBox, 2, row);
            row++;
        }
    }

    @FXML
    private void handleClose() {
        MainApp.changeScene("Home.fxml", "Home");
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.err.println("Desktop browsing is not supported.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not open URL: " + url);
        }
    }

    /**
     * Inner Model Class for Group Members explicitly reduced to core properties
     */
    public static class Member {
        private final String name;
        private final String id;
        private final String facebookUrl;
        private final String linkedinUrl;

        public Member(String name, String id) {
            this(name, id, null, null);
        }

        public Member(String name, String id, String facebookUrl, String linkedinUrl) {
            this.name = name;
            this.id = id;
            this.facebookUrl = facebookUrl;
            this.linkedinUrl = linkedinUrl;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getFacebookUrl() {
            return facebookUrl;
        }

        public String getLinkedinUrl() {
            return linkedinUrl;
        }
    }
}
