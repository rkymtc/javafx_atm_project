package com.hamitmizrak.ibb_ecodation_javafx.controller;

import com.hamitmizrak.ibb_ecodation_javafx.dao.UserDAO;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ERole;
import com.hamitmizrak.ibb_ecodation_javafx.utils.FXMLPath;
import com.hamitmizrak.ibb_ecodation_javafx.utils.SceneHelper;
import com.hamitmizrak.ibb_ecodation_javafx.utils.SpecialColor;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.Optional;

public class RegisterController {
    private UserDAO userDAO;

    public RegisterController() {
        userDAO = new UserDAO();
    }

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private Button themeToggleButton;
    
    @FXML
    public void initialize() {
        // Apply current theme when the scene is ready
        if (usernameField.getScene() != null) {
            ThemeManager.setTheme(usernameField.getScene(), ThemeManager.isDarkTheme());
            updateThemeButton();
        } else {
            // Scene might not be ready yet, we need to wait
            usernameField.sceneProperty().addListener((observable, oldValue, newValue) -> {
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

    private void showAlert(String titleKey, String messageKey, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getString(titleKey));
        alert.setContentText(com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getString(messageKey));
        com.hamitmizrak.ibb_ecodation_javafx.utils.ThemeManager.styleDialog(alert);
        alert.showAndWait();
    }

    @FXML
    private void specialOnEnterPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            register();
        }
    }

    @FXML
    public void register() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert("alert.error", "register.empty.fields", Alert.AlertType.ERROR);
            return;
        }

        // Kullanıcı adı kontrolü
        if (userDAO.isUsernameExists(username)) {
            showAlert("alert.warning", "register.username.exists", Alert.AlertType.WARNING);
            return;
        }

        // Email kontrolü
        if (userDAO.isEmailExists(email)) {
            showAlert("alert.warning", "register.email.exists", Alert.AlertType.WARNING);
            return;
        }

        UserDTO userDTO = UserDTO.builder()
                .username(username)
                .password(password)
                .email(email)
                .role(ERole.USER) // enum olarak
                .build();

        Optional<UserDTO> createdUser = userDAO.create(userDTO);
        if (createdUser.isPresent()) {
            showAlert("alert.success", "register.success.message", Alert.AlertType.INFORMATION);
            switchToLogin();
        } else {
            showAlert("alert.error", "register.error.message", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void switchToLogin() {
        try {
            // Use SceneHelper to maintain theme consistency
            SceneHelper.switchScene(FXMLPath.LOGIN, usernameField, 
                com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getString("login.title"));
        } catch (Exception e) {
            System.out.println(SpecialColor.RED + "Login Sayfasına yönlendirme başarısız" + SpecialColor.RESET);
            e.printStackTrace();
            showAlert("alert.error", "login.screen.load.error", Alert.AlertType.ERROR);
        }
    }
}