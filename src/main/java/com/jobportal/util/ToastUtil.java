package com.jobportal.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Non-intrusive toast notifications for user feedback.
 * Replaces Alert dialogs for a modern, sleek user experience.
 * Auto-hides after 3 seconds with fade-out animation.
 */
public class ToastUtil {

    private static final int TOAST_TIMEOUT = 3000; // 3 seconds
    private static final int TOAST_WIDTH = 350;

    /**
     * Show a toast notification with custom styling.
     * 
     * @param owner   Parent window
     * @param message Message to display
     * @param type    Notification type (SUCCESS, ERROR, INFO, WARNING)
     */
    public static void show(Window owner, String message, NotificationType type) {
        if (owner == null)
            return;

        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setHideOnEscape(true);

            Label label = new Label(message);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-wrap-text: true;");
            label.setMaxWidth(TOAST_WIDTH - 60);

            HBox toastLayout = new HBox(10);
            toastLayout.setAlignment(Pos.CENTER);
            toastLayout.getChildren().add(label);
            toastLayout.setPrefWidth(TOAST_WIDTH);

            // Apply styling based on notification type
            String bgColor = type.getColor();
            toastLayout.setStyle("-fx-background-color: " + bgColor + "; " +
                    "-fx-padding: 15px 30px; " +
                    "-fx-background-radius: 25px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");

            popup.getContent().add(toastLayout);

            // Positioning at the bottom center
            popup.setOnShown(e -> {
                popup.setX(owner.getX() + owner.getWidth() / 2 - TOAST_WIDTH / 2);
                popup.setY(owner.getY() + owner.getHeight() - 80);
            });

            // Show popup
            popup.show(owner);

            // Fade out animation
            Timeline timeline = new Timeline();
            KeyFrame keyFrame1 = new KeyFrame(Duration.millis(TOAST_TIMEOUT),
                    new KeyValue(popup.opacityProperty(), 1.0));
            KeyFrame keyFrame2 = new KeyFrame(Duration.millis(TOAST_TIMEOUT + 500),
                    new KeyValue(popup.opacityProperty(), 0.0));

            timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);
            timeline.setOnFinished(e -> popup.hide());
            timeline.play();
        });
    }

    /**
     * Show success notification.
     * 
     * @param owner   Parent window
     * @param message Success message
     */
    public static void showSuccess(Window owner, String message) {
        show(owner, "✅  " + message, NotificationType.SUCCESS);
    }

    /**
     * Show error notification.
     * 
     * @param owner   Parent window
     * @param message Error message
     */
    public static void showError(Window owner, String message) {
        show(owner, "❌  " + message, NotificationType.ERROR);
    }

    /**
     * Show info notification.
     * 
     * @param owner   Parent window
     * @param message Info message
     */
    public static void showInfo(Window owner, String message) {
        show(owner, "ℹ️  " + message, NotificationType.INFO);
    }

    /**
     * Show warning notification.
     * 
     * @param owner   Parent window
     * @param message Warning message
     */
    public static void showWarning(Window owner, String message) {
        show(owner, "⚠️  " + message, NotificationType.WARNING);
    }

    /**
     * Notification type enumeration with color mappings.
     */
    public enum NotificationType {
        SUCCESS("#10b981"), // Green
        ERROR("#ef4444"), // Red
        INFO("#3b82f6"), // Blue
        WARNING("#f59e0b"); // Amber

        private final String color;

        NotificationType(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }
}
