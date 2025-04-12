package com.hamitmizrak.ibb_ecodation_javafx.utils;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;

public class ThemeManager {
    
    // Constants for theme paths
    public static final String LIGHT_THEME = "/com/hamitmizrak/ibb_ecodation_javafx/css/light-theme.css";
    public static final String DARK_THEME = "/com/hamitmizrak/ibb_ecodation_javafx/css/dark-theme.css";
    
    // Current theme tracker
    private static boolean isDarkTheme = false;
    
    /**
     * Toggle the theme of the scene between light and dark
     * @param scene The scene to apply the theme to
     * @param themeButton The button that toggles the theme (to update its text)
     */
    public static void toggleTheme(Scene scene, Button themeButton) {
        if (scene == null) return;
        
        // Clear existing stylesheets
        scene.getStylesheets().clear();
        
        if (isDarkTheme) {
            // Switch to light theme
            scene.getStylesheets().add(Objects.requireNonNull(
                    ThemeManager.class.getResource(LIGHT_THEME)).toExternalForm());
            themeButton.setText(LanguageManager.getString("admin.theme.dark"));
            isDarkTheme = false;
        } else {
            // Switch to dark theme
            scene.getStylesheets().add(Objects.requireNonNull(
                    ThemeManager.class.getResource(DARK_THEME)).toExternalForm());
            themeButton.setText(LanguageManager.getString("admin.theme.light"));
            isDarkTheme = true;
        }
    }
    
    /**
     * Set a specific theme to the scene
     * @param scene The scene to apply the theme to
     * @param darkTheme True for dark theme, false for light theme
     */
    public static void setTheme(Scene scene, boolean darkTheme) {
        if (scene == null) return;
        
        // Clear existing stylesheets
        scene.getStylesheets().clear();
        
        if (darkTheme) {
            scene.getStylesheets().add(Objects.requireNonNull(
                    ThemeManager.class.getResource(DARK_THEME)).toExternalForm());
            isDarkTheme = true;
        } else {
            scene.getStylesheets().add(Objects.requireNonNull(
                    ThemeManager.class.getResource(LIGHT_THEME)).toExternalForm());
            isDarkTheme = false;
        }
    }
    
    /**
     * Apply theme styling to any dialog
     * @param dialog The dialog to style
     */
    public static void styleDialog(Dialog<?> dialog) {
        if (dialog == null) return;
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("theme-dialog");
        Scene scene = dialogPane.getScene();
        
        if (scene != null) {
            setTheme(scene, isDarkTheme);
            
            // Style buttons
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            Button applyButton = (Button) dialogPane.lookupButton(ButtonType.APPLY);
            Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
            
            if (okButton != null) okButton.getStyleClass().add("primary-button");
            if (applyButton != null) applyButton.getStyleClass().add("primary-button");
            if (cancelButton != null) cancelButton.getStyleClass().add("secondary-button");
            if (closeButton != null) closeButton.getStyleClass().add("secondary-button");
            
            // Style text fields
            dialogPane.lookupAll(".text-field").forEach(node -> {
                if (node instanceof TextField) {
                    node.getStyleClass().add("input-field");
                }
            });
        }
    }
    
    /**
     * Apply theme styling to a popup window
     * @param popup The popup to style
     */
    public static void stylePopup(Popup popup) {
        if (popup == null) return;
        
        // Add appropriate style class to the popup content
        popup.getContent().forEach(node -> {
            node.getStyleClass().add("theme-popup");
            
            // Style all buttons inside the popup
            node.lookupAll(".button").forEach(btn -> {
                if (btn instanceof Button) {
                    Button button = (Button) btn;
                    if (button.getText() != null) {
                        if (button.getText().contains("Güncelle") || 
                            button.getText().contains("Kaydet") || 
                            button.getText().contains("Ekle") || 
                            button.getText().contains("Tamam") ||
                            button.getText().contains("OK") ||
                            button.getText().contains("Giriş")) {
                            button.getStyleClass().add("primary-button");
                        } else if (button.getText().contains("İptal") || 
                                  button.getText().contains("Sil") || 
                                  button.getText().contains("Kapat") ||
                                  button.getText().contains("Vazgeç") ||
                                  button.getText().contains("Cancel")) {
                            button.getStyleClass().add("secondary-button");
                        }
                    }
                }
            });
            
            // Style all text fields inside the popup
            node.lookupAll(".text-field").forEach(field -> {
                field.getStyleClass().add("input-field");
            });
            
            // Style all password fields inside the popup
            node.lookupAll(".password-field").forEach(field -> {
                field.getStyleClass().add("input-field");
            });
            
            // Style all combo boxes inside the popup
            node.lookupAll(".combo-box").forEach(comboBox -> {
                comboBox.getStyleClass().add("input-field");
            });
            
            // Style all date pickers inside the popup
            node.lookupAll(".date-picker").forEach(datePicker -> {
                datePicker.getStyleClass().add("input-field");
            });
        });
        
        // Ensure the popup's owner window has the theme applied
        Window owner = popup.getOwnerWindow();
        if (owner instanceof Stage) {
            Stage stage = (Stage) owner;
            if (stage.getScene() != null) {
                setTheme(stage.getScene(), isDarkTheme);
            }
        }
    }
    
    /**
     * Apply theme styling to any Stage
     * @param stage The stage to style
     */
    public static void styleStage(Stage stage) {
        if (stage == null || stage.getScene() == null) return;
        
        setTheme(stage.getScene(), isDarkTheme);
    }
    
    /**
     * Check if the current theme is dark
     * @return True if dark theme is active, false otherwise
     */
    public static boolean isDarkTheme() {
        return isDarkTheme;
    }
} 