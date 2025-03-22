# JavaFX Desktop
[Java FX GitHub Address](https://github.com/hamitmizrak/ibb_ecodation_javafx)
[Java Core](https://github.com/hamitmizrak/ibb_ecodation_javacore.git )

## Git
```sh 
git init
git add .
git commit -m "initalize javafx"
git remote add origin URL
git push -u origin master
```

## Git Clone 
```sh 
git clone https://github.com/hamitmizrak/ibb_ecodation_javafx
```

## JDK Dikkat
```sh 
JDK JavaFx bizlere önerdiği JDK sürümü 17'dir.
```

## Eğer JDK ile alakalı hatalar alırsak nereleri JDK 17 yapmalıyız ?
```sh 
Settings => Build, Execution => Compiler => Build Compiler (JDK 17 seçelim)
Projects Structure => Project (JDK 17 seçelim)
Projects Structure => Modules => Module,Source,Dependency (JDK 17 seçelim)
Projects Structure => SDK =>  (JDK 17 seçelim)

Build => Rebuild Project
```

## Eğer durduk yere veya JDK değiştirdikten sonra sistem çalışmazsa;
```sh 
Build => Rebuild Project
```

## Maven Codes
```sh 
mvn clean
mvn clean install
mvn clean package
mvn clean package -DskipTests
```

## SingletoDesign Pattern
```sh 

```

 **Singleton Design Pattern** kullanarak **H2 Database** için bir **Connection (Bağlantı) sınıfı** oluşturalım. 

---

### **Neden Singleton Pattern Kullanıyoruz?**
- **Bağlantı havuzu yönetimi**: Tek bir bağlantı örneği, kaynakların gereksiz yere tükenmesini engeller.
- **Bellek yönetimi**: Tek bir bağlantı nesnesi, bellek tüketimini optimize eder.
- **Senkronizasyon**: Eşzamanlı erişimlerin düzgün yönetilmesini sağlar.

---

### **Kodun İçeriği:**
1. **DatabaseConnection Singleton Sınıfı**
    - H2 veritabanına bağlanır.
    - `getInstance()` metodu ile tek bir örnek oluşturur.
    - `getConnection()` metodu ile bağlantıyı döndürür.
    - Uygulama kapatıldığında kaynakları temizler.

2. **TestDatabaseConnection Ana Sınıfı**
    - Singleton örneğini kullanarak H2'ye bağlanır.
    - Örnek bir tablo oluşturur ve veri ekler.
    - Eklenen veriyi okur ve görüntüler.

---

### **Kod:**
```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Singleton Design Pattern ile H2 Database Bağlantı Yönetimi
public class DatabaseConnection {
    private static DatabaseConnection instance; // Singleton örneği
    private Connection connection;
    
    // H2 Database Bağlantı Bilgileri
    private static final String URL = "jdbc:h2:~/testdb"; // H2 dosya tabanlı veritabanı
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    // Özel Constructor (Dışarıdan erişilemez)
    private DatabaseConnection() {
        try {
            // JDBC sürücüsünü yükle
            Class.forName("org.h2.Driver");
            // Bağlantıyı oluştur
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("H2 Database bağlantısı başarılı!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Bağlantı oluşturulamadı!");
        }
    }

    // Singleton Instance Alma Metodu
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Bağlantıyı Alma Metodu
    public Connection getConnection() {
        return connection;
    }

    // Kaynakları Temizleme (Uygulama Kapanırken)
    public static void closeConnection() {
        if (instance != null && instance.connection != null) {
            try {
                instance.connection.close();
                System.out.println("Veritabanı bağlantısı kapatıldı.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

// Singleton Sınıfı Kullanan Test Sınıfı
class TestDatabaseConnection {
    public static void main(String[] args) {
        // Singleton Instance ile Bağlantıyı Al
        DatabaseConnection dbInstance = DatabaseConnection.getInstance();
        Connection conn = dbInstance.getConnection();

        try {
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
            DatabaseConnection.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

### **Kodun Çalışma Mantığı:**
1. **`DatabaseConnection` Singleton Sınıfı**
    - İlk çağrıldığında `instance` değişkeni `null` olduğu için `new DatabaseConnection()` ile nesne oluşturulur.
    - `getInstance()` ile bu tek örnek çağrılır.
    - `getConnection()` metodu ile bağlantı nesnesi döndürülür.

2. **H2 Veritabanı ile İşlemler**
    - `CREATE TABLE` ile `Users` tablosu oluşturulur (eğer yoksa).
    - `INSERT INTO` ile tabloya iki kullanıcı eklenir.
    - `SELECT * FROM` sorgusu ile eklenen veriler çekilir ve ekrana yazdırılır.

3. **Bağlantıyı Yönetme**
    - Program sonunda `DatabaseConnection.closeConnection()` ile bağlantı kapatılır.

---

### **Çıktı Örneği:**
```
H2 Database bağlantısı başarılı!
Users tablosu oluşturuldu!
Veriler eklendi!

Users Tablosu İçeriği:
ID: 1, Name: Ali Veli, Email: ali@example.com
ID: 2, Name: Ayşe Fatma, Email: ayse@example.com
Veritabanı bağlantısı kapatıldı.
```

---

### **Avantajlar:**
✅ **Singleton** kullanarak tek bir bağlantı nesnesiyle işlem yapıyoruz.  
✅ **H2 Database** ile bağımsız ve hafif bir test ortamı sağlıyoruz.  
✅ **Thread-Safe** ve **Lazy Initialization** sayesinde gereksiz bağlantı oluşturulmuyor.  
✅ **Bağlantı yönetimi** sayesinde kaynak israfı önleniyor.

Bu kodu kendi projelerinde rahatlıkla kullanabilirsin! 🚀


## Singleton Config Properties
```sh 

```
Mevcut **SingletonDBConnection** sınıfına ekleyebileceğin bazı geliştirmeler ve ekstra özellikler:

### **1. Daha Esnek ve Özelleştirilebilir Bağlantı Yönetimi**
Şu an bağlantı bilgileri sınıf içinde sabit olarak tanımlanmış. Aşağıdaki geliştirmeleri yapabilirsin:
- **Config dosyasından (properties veya environment) bağlantı bilgilerini okumak.**
- **Bağlantı zaman aşımı eklemek.**
- **Birden fazla veritabanı bağlantısını yönetmek (multi-database support).**

#### **Geliştirilmiş Bağlantı Yapılandırması**
```java
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SingletonDBConnection {
    private static SingletonDBConnection instance;
    private Connection connection;

    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    // Constructor (private)
    private SingletonDBConnection() {
        try {
            loadDatabaseConfig(); // Konfigürasyonu oku
            Class.forName("org.h2.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Veritabanı bağlantısı başarılı");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Veritabanı bağlantısı başarısız!");
        }
    }

    // Konfigürasyonu yükleme
    private static void loadDatabaseConfig() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(fis);
            URL = properties.getProperty("db.url", "jdbc:h2:~/h2db/user_management");
            USERNAME = properties.getProperty("db.username", "sa");
            PASSWORD = properties.getProperty("db.password", "");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Veritabanı yapılandırması yüklenemedi!");
        }
    }

    // Singleton Instance
    public static synchronized SingletonDBConnection getInstance() {
        if (instance == null) {
            instance = new SingletonDBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

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
}
```
#### **Örnek `config.properties` Dosyası**
```
db.url=jdbc:h2:~/h2db/user_management
db.username=sa
db.password=
```
**➜ Avantajları:**
- Bağlantı bilgileri hard-coded yerine **config dosyasından** alınır.
- Farklı veritabanı bağlantılarını yönetmek daha kolay olur.
- Uygulamanın daha taşınabilir ve güvenli olması sağlanır.

---

### **2. Bağlantı Sağlığını Kontrol Etme (Connection Health Check)**
Bağlantının açık olup olmadığını anlamak için aşağıdaki metodu ekleyebilirsin:
```java
public boolean isConnectionValid() {
    try {
        return connection != null && !connection.isClosed();
    } catch (SQLException e) {
        return false;
    }
}
```
Kullanımı:
```java
if(SingletonDBConnection.getInstance().isConnectionValid()) {
    System.out.println("Bağlantı aktif!");
} else {
    System.out.println("Bağlantı kapalı!");
}
```
**➜ Avantajları:**
- Bağlantının düşüp düşmediğini anlayarak yeniden bağlanma stratejileri geliştirilebilir.

---

### **3. Bağlantı Yeniden Başlatma (Reconnect)**
Eğer bağlantı zamanla koparsa, aşağıdaki gibi bir **reconnect()** metodu ekleyebilirsin:
```java
public void reconnect() {
    try {
        if (connection == null || connection.isClosed()) {
            instance = new SingletonDBConnection();
            System.out.println("Veritabanına yeniden bağlanıldı.");
        }
    } catch (SQLException e) {
        throw new RuntimeException("Bağlantı yeniden başlatılamadı!", e);
    }
}
```
Kullanımı:
```java
SingletonDBConnection.getInstance().reconnect();
```
**➜ Avantajları:**
- Uygulama çalışırken bağlantı koparsa, programın çökmesini önler.
- Otomatik bağlantı yenileme özelliği eklenmiş olur.

---

### **4. Logging Mekanizması Ekleme**
Şu an hata yönetimi `System.out.println()` ile yapılıyor. Bunun yerine bir **Logger** kullanabilirsin:
```java
import java.util.logging.Logger;

private static final Logger LOGGER = Logger.getLogger(SingletonDBConnection.class.getName());

private SingletonDBConnection() {
    try {
        Class.forName("org.h2.Driver");
        this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        LOGGER.info("Veritabanı bağlantısı başarılı!");
    } catch (Exception e) {
        LOGGER.severe("Bağlantı hatası: " + e.getMessage());
        throw new RuntimeException("Veritabanı bağlantısı başarısız!", e);
    }
}
```
**➜ Avantajları:**
- Hataların **log dosyasına** yazılması sağlanır.
- Sistem takip edilebilir hale gelir.

---

### **5. Bağlantı Havuzu (Connection Pool) Kullanımı**
- Eğer uygulamada çok fazla eşzamanlı bağlantı gerekecekse, **Singleton yerine Connection Pool (HikariCP gibi kütüphaneler)** kullanılabilir.
- HikariCP gibi kütüphaneler kullanarak performansı artırabilirsin.

Örnek:
```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class HikariCPDatabase {
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:~/h2db/user_management");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10); // Maksimum 10 bağlantı
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```
**➜ Avantajları:**
- Tek bir bağlantı yerine, havuzdan **birden fazla bağlantı yönetilebilir**.
- Performans önemli ölçüde artar.

---

### **Sonuç ve Özet**
Mevcut **SingletonDBConnection** sınıfına ekleyebileceğin özellikler:
✅ **Bağlantı bilgilerini config dosyasından okumak.**  
✅ **Bağlantının durumunu kontrol etmek (`isConnectionValid()`).**  
✅ **Bağlantıyı yeniden başlatmak (`reconnect()`).**  
✅ **Logging mekanizması eklemek (`Logger`).**  
✅ **Bağlantı havuzu (HikariCP) kullanmak.**

**Gelecekte ekleyebileceğin özellikler:**
- **Bağlantı hatalarını otomatik olarak ele alan bir Retry mekanizması.**
- **Şifreleri güvenli hale getirmek için şifreleme mekanizması (örneğin, Java KeyStore kullanımı).**
- **Bağlantı yönetimini daha esnek hale getirmek için Dependency Injection (DI) ile yönetmek (Spring, Guice).**

Bu eklemelerle kodunu daha sağlam, esnek ve güvenli hale getirebilirsin! 🚀


## Other
```sh 

```







