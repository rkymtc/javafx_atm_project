package com.hamitmizrak.ibb_ecodation_javafx;

import com.hamitmizrak.ibb_ecodation_javafx.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;
    
    @FXML
    private Button themeToggleButton;
    
    @FXML
    public void initialize() {
        welcomeText.setText("Welcome to JavaFX Application!");
        
        // Apply current theme when the scene is ready
        if (welcomeText.getScene() != null) {
            ThemeManager.setTheme(welcomeText.getScene(), ThemeManager.isDarkTheme());
            updateThemeButton();
        } else {
            // Scene might not be ready yet, we need to wait
            welcomeText.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    ThemeManager.setTheme(newValue, ThemeManager.isDarkTheme());
                    updateThemeButton();
                }
            });
        }
    }
    
    private void updateThemeButton() {
        if (themeToggleButton != null) {
            themeToggleButton.setText(ThemeManager.isDarkTheme() ? "☀️ Aydınlık Mod" : "🌙 Karanlık Mod");
        }
    }
    
    @FXML
    private void toggleTheme() {
        Scene scene = themeToggleButton.getScene();
        ThemeManager.toggleTheme(scene, themeToggleButton);
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}