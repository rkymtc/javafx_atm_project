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


## JavaFX
```sh 

```


## JavaFX
```sh 

```

## JavaFX
```sh 

```



## JavaFX
```sh 

```


## JavaFX
```sh 

```



## JavaFX
```sh 

```


## JavaFX
```sh 

```


## JavaFX
```sh 

```


## JavaFX
```sh 

```


## JavaFX
```sh 

```

## JavaFX
```sh 

```

## JavaFX
```sh 

```

## JavaFX
```sh 

```





