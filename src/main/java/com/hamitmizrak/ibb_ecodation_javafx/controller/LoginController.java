package com.hamitmizrak.ibb_ecodation_javafx.controller;

import com.hamitmizrak.ibb_ecodation_javafx.dao.UserDAO;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ERole;
import com.hamitmizrak.ibb_ecodation_javafx.utils.FXMLPath;
import com.hamitmizrak.ibb_ecodation_javafx.utils.SceneHelper;
import com.hamitmizrak.ibb_ecodation_javafx.utils.SpecialColor;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ThemeManager;
import com.hamitmizrak.ibb_ecodation_javafx.utils.UserSession;
import javafx.event.ActionEvent;
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

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class LoginController {
    private UserDAO userDAO;

    public LoginController() {
        userDAO = new UserDAO();
    }

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button themeToggleButton;
    
    @FXML
    public void initialize() {
        // Apply default theme
        if (usernameField.getScene() != null) {
            ThemeManager.setTheme(usernameField.getScene(), false); // Light theme by default
            updateThemeButton();
        } else {
            // Scene might not be ready yet, we need to wait until the scene is ready
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
        ThemeManager.styleDialog(alert);
        alert.showAndWait();
    }

    @FXML
    private void specialOnEnterPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            login();
        }
    }

    @FXML
    public void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        Optional<UserDTO> optionalLoginUserDTO = userDAO.loginUser(username, password);

        if (optionalLoginUserDTO.isPresent()) {
            UserDTO userDTO = optionalLoginUserDTO.get();
            
            // Store the logged-in user in the session
            UserSession.getInstance().setCurrentUser(userDTO);
            
            showAlert("alert.success", "login.success.message", Alert.AlertType.INFORMATION);

            if (userDTO.getRole() == ERole.ADMIN) {
                openAdminPane();
            } else {
                openUserHomePane();
            }
        } else {
            showAlert("alert.error", "login.error.message", Alert.AlertType.ERROR);
        }
    }

    private void openUserHomePane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource(FXMLPath.USER_HOME),
                com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getResourceBundle()
            );
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent);
            ThemeManager.setTheme(scene, ThemeManager.isDarkTheme());
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getString("user.panel.title"));
            stage.show();
        } catch (Exception e) {
            System.out.println(SpecialColor.RED + "Kullanıcı paneline yönlendirme başarısız" + SpecialColor.RESET);
            e.printStackTrace();
            showAlert("alert.error", "user.panel.load.error", Alert.AlertType.ERROR);
        }
    }

    private void openAdminPane() {
        try {
            System.out.println("Opening admin panel, getting resource: " + FXMLPath.ADMIN);
            URL resourceUrl = getClass().getResource(FXMLPath.ADMIN);
            if (resourceUrl == null) {
                System.out.println(SpecialColor.RED + "Admin FXML resource not found: " + FXMLPath.ADMIN + SpecialColor.RESET);
                showAlert("alert.error", "admin.panel.load.error", Alert.AlertType.ERROR);
                return;
            }
            System.out.println("Resource found: " + resourceUrl);
            
            FXMLLoader fxmlLoader = new FXMLLoader(
                resourceUrl,
                com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getResourceBundle()
            );
            
            try {
                Parent parent = fxmlLoader.load();
                Scene scene = new Scene(parent);
                ThemeManager.setTheme(scene, ThemeManager.isDarkTheme());
                
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle(com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getString("admin.title"));
                stage.show();
            } catch (IOException e) {
                System.out.println(SpecialColor.RED + "Error loading admin FXML: " + e.getMessage() + SpecialColor.RESET);
                e.printStackTrace();
                showAlert("alert.error", "admin.panel.load.error", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.out.println(SpecialColor.RED + "Admin Sayfasına yönlendirme başarısız: " + e.getMessage() + SpecialColor.RESET);
            e.printStackTrace();
            showAlert("alert.error", "admin.panel.load.error", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void switchToRegister(ActionEvent actionEvent) {
        try {
            // Use SceneHelper to keep the theme consistent
            SceneHelper.switchScene(FXMLPath.REGISTER, usernameField, 
                com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getString("register.title"));
        } catch (Exception e) {
            System.out.println(SpecialColor.RED + "Register Sayfasına yönlendirme başarısız" + SpecialColor.RESET);
            e.printStackTrace();
            showAlert("alert.error", "register.screen.load.error", Alert.AlertType.ERROR);
        }
    }
}