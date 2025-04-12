package com.hamitmizrak.ibb_ecodation_javafx.utils;

import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;

/**
 * Singleton class to manage the current user session
 */
public class UserSession {
    private static UserSession instance;
    private UserDTO currentUser;

    private UserSession() {
        // Private constructor for singleton
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(UserDTO user) {
        this.currentUser = user;
    }

    public UserDTO getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }
} 