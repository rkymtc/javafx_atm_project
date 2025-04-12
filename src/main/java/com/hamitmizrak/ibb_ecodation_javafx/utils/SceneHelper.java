package com.hamitmizrak.ibb_ecodation_javafx.utils;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneHelper {

    public static void switchScene(String fxmlPath, Node currentNode, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneHelper.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene scene = new Scene(root);
            
            // Apply current theme to new scene
            if (ThemeManager.isDarkTheme()) {
                ThemeManager.setTheme(scene, true);
            } else {
                ThemeManager.setTheme(scene, false);
            }
            
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("FXML Yüklenemedi", "Dosya yolu: " + fxmlPath);
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata: " + title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
