package com.hamitmizrak.ibb_ecodation_javafx.database;

import com.hamitmizrak.ibb_ecodation_javafx.utils.SpecialColor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SingletonDBConnection {

    // Field
    // DAtabase  Information Data
    private static final String URL = "jdbc:h2:./h2db/blog" + "AUTO_SERVER=TRUE";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    // Parametresiz Constructor
    private SingletonDBConnection() {

    }

    // For Database, Connection
    private static Connection connection;

    // Method
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println(SpecialColor.GREEN + "Veritabanı bağlantısı başarılı" + SpecialColor.RESET);
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
                System.out.println(SpecialColor.RED + "Veritabanı bağlantısı başarısız" + SpecialColor.RESET);
                throw new RuntimeException("Veritabanı bağlantısı başarısız");
            }
        }
        return connection;
    }
} // end class
