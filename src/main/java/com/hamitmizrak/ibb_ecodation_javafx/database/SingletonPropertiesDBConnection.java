package com.hamitmizrak.ibb_ecodation_javafx.database;

import org.h2.tools.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SingletonPropertiesDBConnection {

    // Field
    // Database  Information Data
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    // Singleton Design pattern
    private static SingletonPropertiesDBConnection instance;
    private Connection connection;

    // Parametresiz Constructor (private ile dışarıdan erişilemez olmasını sağlamak)
    private SingletonPropertiesDBConnection() {
        try {
            // Konfigürasyonu oku
            loadDatabaseConfig();

            // H2DB Yükle
            Class.forName("org.h2.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Veritabanı bağlantısı başarılı");

            // H2DB
            H2DbStarting();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Veritabanı bağlantısı başarısız!");
        }
    }

    // Singleton Instance
    public static synchronized SingletonPropertiesDBConnection getInstance() {
        if (instance == null) {
            instance = new SingletonPropertiesDBConnection();
        }
        return instance;
    }

    // Connection
    public Connection getConnection() {
        return connection;
    }

    // Close
    public static void closeConnection() {
        if (instance != null && instance.connection != null) {
            try {
                instance.connection.close();
                System.out.println("Veritabanı bağlantısı kapatıldı.");
            } catch (SQLException e) {
                throw new RuntimeException("Bağlantı kapatılırken hata oluştu!", e);
            }
        }
    }

    ///  ////////////////////////////////////////////////////////////////////////////////////
    ///  ////////////////////////////////////////////////////////////////////////////////////
    // Konfigürasyonu yükleme
    private static void loadDatabaseConfig() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(fis);
            URL = properties.getProperty("db.url", "jdbc:h2:./h2db/user_management");
            //URL = properties.getProperty("db.url", "jdbc:h2:~/h2db/user_management"); // Ram belleğe kaydeder
            USERNAME = properties.getProperty("db.username", "sa");
            PASSWORD = properties.getProperty("db.password", "");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Veritabanı yapılandırması yüklenemedi!");
        }
    }

    ///  ////////////////////////////////////////////////////////////////////////////////////
    /// H2DB PORT RUNNING
    // H2DB
    // H2 Web Konsolunu başlatmak için
    /*
        H2DB URL     : jdbc:h2:./h2db/user_management
        H2DB USER    : sa
        H2DB PASSWORD:
    */
    private void H2DbStarting() {
        try {
            Server server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("H2 Web Console is running at: http://localhost:8082");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

} // end SingletonPropertiesDBConnection
