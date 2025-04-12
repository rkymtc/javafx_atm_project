package com.hamitmizrak.ibb_ecodation_javafx.controller;

import com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager;
import com.hamitmizrak.ibb_ecodation_javafx.utils.NotificationManager;
import com.hamitmizrak.ibb_ecodation_javafx.utils.NotificationManager.Notification;
import com.hamitmizrak.ibb_ecodation_javafx.utils.NotificationManager.NotificationType;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NotificationController implements Initializable {

    @FXML
    private TableView<Notification> notificationTable;
    
    @FXML
    private TableColumn<Notification, String> typeColumn;
    
    @FXML
    private TableColumn<Notification, String> messageColumn;
    
    @FXML
    private TableColumn<Notification, String> timestampColumn;
    
    @FXML
    private ComboBox<String> filterComboBox;
    
    @FXML
    private Button clearAllButton;
    
    private ObservableList<Notification> notificationList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up table columns
        typeColumn.setCellValueFactory(cellData -> {
            NotificationType type = cellData.getValue().getType();
            String icon = "";
            switch (type) {
                case SUCCESS:
                    icon = "✅";
                    break;
                case WARNING:
                    icon = "⚠️";
                    break;
                case ERROR:
                    icon = "❌";
                    break;
                case INFO:
                    icon = "ℹ️";
                    break;
            }
            return new SimpleStringProperty(icon);
        });
        
        messageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
        
        timestampColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            return new SimpleStringProperty(timestamp.format(formatter));
        });
        
        // Set up row factory for custom styling
        notificationTable.setRowFactory(tv -> new TableRow<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
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
        
        // Set column headers
        typeColumn.setText(LanguageManager.getString("notifications.type"));
        messageColumn.setText(LanguageManager.getString("notifications.message"));
        timestampColumn.setText(LanguageManager.getString("notifications.timestamp"));
        
        // Set up filter combo box with translated values
        filterComboBox.getItems().addAll(
            LanguageManager.getString("notifications.filter.all"),
            LanguageManager.getString("notifications.filter.info"),
            LanguageManager.getString("notifications.filter.success"),
            LanguageManager.getString("notifications.filter.warning"),
            LanguageManager.getString("notifications.filter.error")
        );
        
        filterComboBox.getSelectionModel().selectFirst();
        
        filterComboBox.setOnAction(event -> {
            applyFilter();
        });
        
        // Set up clear all button with translation
        clearAllButton.setText(LanguageManager.getString("notifications.clear"));
        clearAllButton.setOnAction(event -> {
            if (notificationList.isEmpty()) {
                return;
            }
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(LanguageManager.getString("notifications.clear.title"));
            alert.setHeaderText(LanguageManager.getString("notifications.clear.header"));
            alert.setContentText(LanguageManager.getString("notifications.clear.content"));
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    notificationList.clear();
                    NotificationManager.clearAllNotifications();
                    refreshTable();
                }
            });
        });
        
        // Initial load
        refreshTable();
    }
    
    /**
     * Refreshes the notification table with the latest notifications
     */
    public void refreshTable() {
        notificationList.clear();
        notificationList.addAll(NotificationManager.getNotificationHistory());
        notificationTable.setItems(notificationList);
        applyFilter();
    }
    
    /**
     * Applies the selected filter to the notifications table
     */
    private void applyFilter() {
        String selectedFilter = filterComboBox.getSelectionModel().getSelectedItem();
        
        if (selectedFilter.equals(LanguageManager.getString("notifications.filter.all"))) {
            notificationTable.setItems(notificationList);
        } else {
            NotificationType filterType = null;
            
            if (selectedFilter.equals(LanguageManager.getString("notifications.filter.info"))) {
                filterType = NotificationType.INFO;
            } else if (selectedFilter.equals(LanguageManager.getString("notifications.filter.success"))) {
                filterType = NotificationType.SUCCESS;
            } else if (selectedFilter.equals(LanguageManager.getString("notifications.filter.warning"))) {
                filterType = NotificationType.WARNING;
            } else if (selectedFilter.equals(LanguageManager.getString("notifications.filter.error"))) {
                filterType = NotificationType.ERROR;
            }
            
            if (filterType != null) {
                NotificationType finalFilterType = filterType;
                ObservableList<Notification> filteredList = FXCollections.observableArrayList(
                    notificationList.stream()
                        .filter(n -> n.getType() == finalFilterType)
                        .collect(Collectors.toList())
                );
                notificationTable.setItems(filteredList);
            }
        }
    }
} 