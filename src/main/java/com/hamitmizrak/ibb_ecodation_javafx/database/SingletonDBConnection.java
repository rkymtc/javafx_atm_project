package com.hamitmizrak.ibb_ecodation_javafx.database;

import com.hamitmizrak.ibb_ecodation_javafx.utils.SpecialColor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SingletonDBConnection {

    // Field
    // Database  Information Data
    //private static final String URL = "jdbc:h2:./h2db/user_management" + "AUTO_SERVER=TRUE";
    private static final String URL = "jdbc:h2:~/h2db/user_management";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    // Singleton Design pattern
    private static SingletonDBConnection instance;
    private  Connection connection;

    // Parametresiz Constructor (private ile dışarıdan erişilemez olmasını sağlamak)
    private SingletonDBConnection() {
        try {
            // JDBC Yüksle
            Class.forName("org.h2.Driver");
            // Bağlantı oluşturmak
            this.connection= DriverManager.getConnection(URL, USERNAME,PASSWORD);
            System.out.println(SpecialColor.GREEN + "Veritabanı bağlantısı başarılı" + SpecialColor.RESET);
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println(SpecialColor.RED + "Veritabanı bağlantısı başarısız" + SpecialColor.RESET);
            throw new RuntimeException("Veritabanı bağlantısı başarısız");
        }
    }

    // Singleton Design Intance
    public static synchronized SingletonDBConnection getInstance(){
        if(instance==null){
            instance= new SingletonDBConnection();
        }
        return instance;
    }

    // Bağlantı nesnesi çağırma
    public Connection getConnection() {
        return connection;
    }

    // Database Kapatmak
    public static void closeConnection(){
        if(instance!=null && instance.connection!=null){
            try {
                instance.connection.close();
                System.out.println(SpecialColor.RED+ "Veritabanı bağlantısı kapatıldı");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Database Test
    public static void main(String[] args) {

    }
} // end class
