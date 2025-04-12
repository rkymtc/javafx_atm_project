package com.hamitmizrak.ibb_ecodation_javafx.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    
    // Base name for the resource bundle
    private static final String RESOURCE_BUNDLE_BASE_NAME = "lang.messages";
    
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
     * Get a localized string from the resource bundle
     * @param key The key in the resource bundle
     * @return The localized string
     */
    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            // Return the key itself if not found
            return key;
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
     * Check if current language is Turkish
     * @return true if Turkish, false otherwise
     */
    public static boolean isTurkish() {
        return currentLocale.getLanguage().equals("tr");
    }
    
    /**
     * Get the current ResourceBundle
     * @return The current ResourceBundle
     */
    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
} 