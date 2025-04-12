package com.hamitmizrak.ibb_ecodation_javafx;

import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.FXMLPath;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ThemeManager;
import com.hamitmizrak.ibb_ecodation_javafx.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelloController {
    @FXML
    private Label welcomeText;
    
    @FXML
    private Button themeToggleButton;
    
    @FXML
    public void initialize() {
        // Get current user info
        UserDTO currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeText.setText("Hoş geldin, " + currentUser.getUsername() + "!");
        } else {
            welcomeText.setText("Hoş geldiniz!");
        }
        
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
        UserDTO currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeText.setText("Kullanıcı Bilgileri:\n" + 
                                "Ad: " + currentUser.getUsername() + "\n" +
                                "E-posta: " + currentUser.getEmail() + "\n" +
                                "Rol: " + currentUser.getRole());
        } else {
            welcomeText.setText("Kullanıcı bilgisi bulunamadı!");
        }
    }
    
    @FXML
    protected void openProfilePage(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource(FXMLPath.PROFILE)
            );
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent);
            ThemeManager.setTheme(scene, ThemeManager.isDarkTheme());
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Profil Yönetimi");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Profil sayfasına yönlendirme başarısız: " + e.getMessage());
        }
    }
}