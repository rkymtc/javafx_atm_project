package com.hamitmizrak.ibb_ecodation_javafx.database;

import com.hamitmizrak.ibb_ecodation_javafx.utils.SpecialColor;

import java.sql.*;

public class SingletonDBConnection {

    // Field
    // Database  Information Data
    private static final String URL = "jdbc:h2:./h2db/user_management";
    //private static final String URL = "jdbc:h2:./h2db/user_management?" + "AUTO_SERVER=TRUE";
    //private static final String URL = "jdbc:h2:~/h2db/user_management"; //kök dizin
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    // Singleton Design pattern
    private static SingletonDBConnection instance;
    private  Connection connection;

    // Parametresiz Constructor (private ile dışarıdan erişilemez olmasını sağlamak)
    private SingletonDBConnection() {
        try {
            // JDBC Yükle
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
    public static void dataSet()  throws SQLException{
        // Singleton Instance ile Bağlantıyı Al
        SingletonDBConnection dbInstance = SingletonDBConnection.getInstance();
        Connection conn = dbInstance.getConnection();

        Statement stmt = conn.createStatement();

        // Örnek bir tablo oluştur
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(255), "
                + "email VARCHAR(255))";
        stmt.execute(createTableSQL);
        System.out.println("Users tablosu oluşturuldu!");

        // Veri Ekleme
        String insertDataSQL = "INSERT INTO Users (name, email) VALUES "
                + "('Ali Veli', 'ali@example.com'), "
                + "('Ayşe Fatma', 'ayse@example.com')";
        stmt.executeUpdate(insertDataSQL);
        System.out.println("Veriler eklendi!");

        // Veri Okuma
        String selectSQL = "SELECT * FROM Users";
        ResultSet rs = stmt.executeQuery(selectSQL);

        System.out.println("\nUsers Tablosu İçeriği:");
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("id") +
                    ", Name: " + rs.getString("name") +
                    ", Email: " + rs.getString("email"));
        }

        // Bağlantıyı Kapat
        SingletonDBConnection.closeConnection();
    }
    public static void main(String[] args) throws SQLException {
        //dataSet();
    }
} // end class
