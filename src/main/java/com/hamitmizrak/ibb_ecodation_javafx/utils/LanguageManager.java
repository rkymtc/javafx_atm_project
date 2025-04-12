package com.hamitmizrak.ibb_ecodation_javafx.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    
    // Constants for language resources
    public static final String RESOURCE_BUNDLE_BASE_NAME = "com.hamitmizrak.ibb_ecodation_javafx.lang.messages";
    
    // Default locale is Turkish
    private static Locale currentLocale = new Locale("tr", "TR");
    
    // ResourceBundle for language strings
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME, currentLocale);
    
    /**
     * Toggle between Turkish and English languages
     */
    public static void toggleLanguage() {
        if (currentLocale.getLanguage().equals("tr")) {
            // Switch to English
            setLocale(new Locale("en", "US"));
        } else {
            // Switch to Turkish
            setLocale(new Locale("tr", "TR"));
        }
    }
    
    /**
     * Set a specific locale
     * @param locale The locale to set
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME, currentLocale);
    }
    
    /**
     * Get a localized string from the resources
     * @param key The resource key
     * @return The localized string
     */
    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            return key; // Return the key as fallback
        }
    }
    
    /**
     * Get the current locale
     * @return The current locale
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * Check if the current language is Turkish
     * @return True if Turkish, false if other language
     */
    public static boolean isTurkish() {
        return currentLocale.getLanguage().equals("tr");
    }
} 