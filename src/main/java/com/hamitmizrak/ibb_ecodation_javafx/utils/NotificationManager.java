package com.hamitmizrak.ibb_ecodation_javafx.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    public enum NotificationType {
        SUCCESS, WARNING, ERROR, INFO
    }

    // Class to represent a notification
    public static class Notification {
        private NotificationType type;
        private String message;
        private LocalDateTime timestamp;

        public Notification(NotificationType type, String message) {
            this.type = type;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        public NotificationType getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String prefix = "";
            switch (type) {
                case SUCCESS:
                    prefix = "✅";
                    break;
                case WARNING:
                    prefix = "⚠️";
                    break;
                case ERROR:
                    prefix = "❌";
                    break;
                case INFO:
                    prefix = "ℹ️";
                    break;
            }
            return prefix + " " + timestamp.format(formatter) + " - " + message;
        }
    }

    // Store notifications in a list
    private static final List<Notification> notificationHistory = new ArrayList<>();

    /**
     * Get all stored notifications
     * @return List of notifications
     */
    public static List<Notification> getNotificationHistory() {
        return new ArrayList<>(notificationHistory);
    }
    
    /**
     * Clear all stored notifications
     */
    public static void clearAllNotifications() {
        notificationHistory.clear();
    }

    /**
     * Show a popup notification
     * @param owner The owner window
     * @param message The message to display
     * @param type The type of notification
     */
    public static void showNotification(Window owner, String message, NotificationType type) {
        // Add to history
        Notification notification = new Notification(type, message);
        notificationHistory.add(notification);

        // Create popup UI
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        
        // Create content
        VBox content = new VBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(15));
        content.setSpacing(10);
        content.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        content.setMinWidth(300);
        content.setMaxWidth(300);
        
        // Set content background color based on notification type
        String backgroundColor;
        String textColor = "black";
        String icon;
        
        switch (type) {
            case SUCCESS:
                backgroundColor = "#e6ffee";
                icon = "✅";
                break;
            case WARNING:
                backgroundColor = "#fff9e6";
                icon = "⚠️";
                break;
            case ERROR:
                backgroundColor = "#ffe6e6";
                icon = "❌";
                break;
            case INFO:
            default:
                backgroundColor = "#e6f7ff";
                icon = "ℹ️";
                break;
        }
        
        content.setStyle("-fx-background-radius: 5; -fx-background-color: " + backgroundColor + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        
        // Header with icon and close button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeButton = new Button("✖");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + textColor + ";");
        closeButton.setOnAction(e -> popup.hide());
        
        header.getChildren().addAll(iconLabel, spacer, closeButton);
        
        // Message
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: " + textColor + ";");
        
        // Add components to content
        content.getChildren().addAll(header, messageLabel);
        
        popup.getContent().add(content);
        
        // Position the popup at bottom right of owner window
        if (owner != null) {
            popup.show(owner, 
                    owner.getX() + owner.getWidth() - 320, 
                    owner.getY() + owner.getHeight() - 100);
            
            // Animate the popup
            content.setOpacity(0);
            
            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(content.opacityProperty(), 0)),
                    new KeyFrame(Duration.millis(200), new KeyValue(content.opacityProperty(), 1))
            );
            fadeIn.play();
            
            // Auto hide after 5 seconds
            Timeline autoHide = new Timeline(
                    new KeyFrame(Duration.seconds(5), e -> {
                        Timeline fadeOut = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(content.opacityProperty(), 1)),
                                new KeyFrame(Duration.millis(200), e2 -> popup.hide(), new KeyValue(content.opacityProperty(), 0))
                        );
                        fadeOut.play();
                    })
            );
            autoHide.play();
        }
    }

    /**
     * Show a success notification
     * @param owner The owner window
     * @param message The message to display
     */
    public static void showSuccess(Window owner, String message) {
        showNotification(owner, message, NotificationType.SUCCESS);
    }

    /**
     * Show a warning notification
     * @param owner The owner window
     * @param message The message to display
     */
    public static void showWarning(Window owner, String message) {
        showNotification(owner, message, NotificationType.WARNING);
    }

    /**
     * Show an error notification
     * @param owner The owner window
     * @param message The message to display
     */
    public static void showError(Window owner, String message) {
        showNotification(owner, message, NotificationType.ERROR);
    }

    /**
     * Show an info notification
     * @param owner The owner window
     * @param message The message to display
     */
    public static void showInfo(Window owner, String message) {
        showNotification(owner, message, NotificationType.INFO);
    }

    /**
     * Show notification history dialog
     * @param owner The owner window
     */
    public static void showNotificationHistory(Window owner) {
        // Create popup UI for history
        Popup historyPopup = new Popup();
        historyPopup.setAutoHide(true);
        historyPopup.setAutoFix(true);
        
        // Create content
        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(15));
        content.setSpacing(10);
        content.setStyle("-fx-background-radius: 5; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        content.setMinWidth(400);
        content.setMaxWidth(400);
        content.setMinHeight(300);
        content.setMaxHeight(500);
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(10);
        
        Label titleLabel = new Label(LanguageManager.getString("notifications.title"));
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeButton = new Button("✖");
        closeButton.setStyle("-fx-background-color: transparent;");
        closeButton.setOnAction(e -> historyPopup.hide());
        
        header.getChildren().addAll(titleLabel, spacer, closeButton);
        
        // Notification list
        ListView<Notification> listView = new ListView<>();
        listView.getItems().addAll(notificationHistory);
        listView.setCellFactory(lv -> new javafx.scene.control.ListCell<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item.getType()) {
                        case SUCCESS:
                            setStyle("-fx-background-color: #e6ffee;");
                            break;
                        case WARNING:
                            setStyle("-fx-background-color: #fff9e6;");
                            break;
                        case ERROR:
                            setStyle("-fx-background-color: #ffe6e6;");
                            break;
                        case INFO:
                            setStyle("-fx-background-color: #e6f7ff;");
                            break;
                    }
                }
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);
        
        // Clear button
        Button clearButton = new Button(LanguageManager.getString("notifications.clear"));
        clearButton.setOnAction(e -> {
            notificationHistory.clear();
            listView.getItems().clear();
        });
        clearButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(clearButton);
        
        // Add components to content
        content.getChildren().addAll(header, listView, buttonBox);
        
        historyPopup.getContent().add(content);
        
        // Show popup
        if (owner != null) {
            historyPopup.show(owner, 
                    owner.getX() + (owner.getWidth() - 400) / 2, 
                    owner.getY() + (owner.getHeight() - 400) / 2);
        }
    }
} 