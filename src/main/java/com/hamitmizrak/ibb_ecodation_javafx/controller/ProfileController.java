package com.hamitmizrak.ibb_ecodation_javafx.controller;

import com.hamitmizrak.ibb_ecodation_javafx.dao.UserDAO;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ERole;
import com.hamitmizrak.ibb_ecodation_javafx.utils.FXMLPath;
import com.hamitmizrak.ibb_ecodation_javafx.utils.SceneHelper;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ThemeManager;
import com.hamitmizrak.ibb_ecodation_javafx.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ProfileController {

    @FXML
    private Label usernameLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private PasswordField currentPasswordField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button themeToggleButton;
    
    private UserDAO userDAO;
    private UserDTO currentUser;

    public ProfileController() {
        userDAO = new UserDAO();
    }

    @FXML
    public void initialize() {
        // Get the current logged-in user
        currentUser = UserSession.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            // Test için geçici kullanıcı oluştur - gerçek uygulamada bu kaldırılmalı
            System.out.println("UserSession boş! Test kullanıcısı oluşturuluyor...");
            currentUser = UserDTO.builder()
                .id(1)
                .username("test_user")
                .email("test@example.com")
                .role(ERole.USER)
                .password("test123")
                .build();
            UserSession.getInstance().setCurrentUser(currentUser);
        }
        
        // Display the user information
        usernameLabel.setText(currentUser.getUsername());
        emailLabel.setText(currentUser.getEmail());
        roleLabel.setText(currentUser.getRole().toString());
        
        // Set up theme button
        if (themeToggleButton != null && themeToggleButton.getScene() != null) {
            updateThemeButton();
        } else if (themeToggleButton != null) {
            // Scene might not be ready yet
            themeToggleButton.sceneProperty().addListener((observable, oldValue, newValue) -> {
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
    public void updatePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Hata", "Tüm alanları doldurun.", Alert.AlertType.ERROR);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showAlert("Hata", "Yeni şifre ve tekrarı uyuşmuyor.", Alert.AlertType.ERROR);
            return;
        }
        
        if (newPassword.length() < 4) {
            showAlert("Hata", "Şifre en az 4 karakter olmalıdır.", Alert.AlertType.ERROR);
            return;
        }
        
        // Try to change the password
        boolean success = userDAO.changePassword(currentUser.getId(), currentPassword, newPassword);
        if (success) {
            showAlert("Başarılı", "Şifreniz başarıyla güncellendi.", Alert.AlertType.INFORMATION);
            // Clear password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showAlert("Hata", "Şifre güncellenemedi. Mevcut şifrenizi kontrol edin.", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    public void returnToPreviousScreen() {
        try {
            // Return to the appropriate screen based on user role
            if (currentUser.getRole() == ERole.ADMIN) {
                // Return to admin panel
                SceneHelper.switchScene(FXMLPath.ADMIN, usernameLabel, "Admin Paneli");
            } else {
                // Return to user home screen
                SceneHelper.switchScene(FXMLPath.USER_HOME, usernameLabel, "Kullanıcı Paneli");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Hata", "Geri dönüş sırasında bir hata oluştu.", Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        ThemeManager.styleDialog(alert);
        alert.showAndWait();
    }
} 