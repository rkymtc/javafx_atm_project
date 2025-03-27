package com.hamitmizrak.ibb_ecodation_javafx.controller;

import com.hamitmizrak.ibb_ecodation_javafx.dao.UserDAO;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ERole;
import com.hamitmizrak.ibb_ecodation_javafx.utils.FXMLPath;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

// Admin Controller
public class AdminController {
    // Injection
    // Veri tabanı işlemleri için)
    private UserDAO userDAO;

    // Parametresiz Constructor
    public AdminController() {
        userDAO = new UserDAO();
    }

    /// ////////////////////////////////////////////////////////////////////////////
    // Kullanıcı tablosunu ve Sutunlarını tanımlamak
    // Dikkat: TableView => import javafx.scene.control.TableView;
    @FXML private TableView<UserDTO> userTable;
    @FXML private TableColumn<UserDTO, Integer> idColumn;
    @FXML private TableColumn<UserDTO, String> usernameColumn;
    @FXML private TableColumn<UserDTO, String> emailColumn;
    @FXML private TableColumn<UserDTO, String> passwordColumn;
    @FXML private TableColumn<UserDTO, String> roleColumn;
    @FXML private TextField searchField;
    @FXML
    private ComboBox<ERole> filterRoleComboBox;

    /// ////////////////////////////////////////////////////////////////////////////
    /// Admin sayfası açılırken hazır olsun
    @FXML
    public void initialize() {
        // Tablo sütunlarını UserDTO nesnesinin ilgili alanlarına bağlıyoruz
        // Dikkat: buradaki id, username, email,password
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Rol Filtreleme için ComboBox
        filterRoleComboBox.getItems().add(null); // Boş seçenek: tüm roller
        filterRoleComboBox.getItems().addAll(ERole.values());
        filterRoleComboBox.setValue(null); // Başlangıçtaki roller

        // Arama Kutusunu Dinleme(Listener)
        searchField.textProperty().addListener((observable, oldValue,newValue) ->applyFilters());
        filterRoleComboBox.valueProperty().addListener((observable, oldValue,newValue) ->applyFilters());

        // Şifre sütununu maskeleme özelliği ekleniyor
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String password, boolean empty) {
                super.updateItem(password, empty);
                if (empty || password == null) {
                    setText(null);
                } else {
                    setText("******"); // Şifreleri gizlemek için yıldız işareti kullanılıyor
                }
            }
        });

        // Kullanıcı listesini tabloya yükleme
        refreshTable();
    }

    ///////////////////////////////////////////////////////////////////////////
    /// applyFilters()
    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase().trim();
        ERole selectedRole = filterRoleComboBox.getValue();

        Optional<List<UserDTO>> optionalUsers = userDAO.list();
        List<UserDTO> fullList = optionalUsers.orElseGet(List::of);

        List<UserDTO> filteredList = fullList.stream()
                .filter(user -> {
                    boolean matchesKeyword = keyword.isEmpty() ||
                            user.getUsername().toLowerCase().contains(keyword) ||
                            user.getEmail().toLowerCase().contains(keyword) ||
                            user.getRole().getDescription().toLowerCase().contains(keyword);
                    boolean matchesRole = (selectedRole == null) || user.getRole() == selectedRole;
                    return matchesKeyword && matchesRole;
                })
                .toList();
        userTable.setItems(FXCollections.observableArrayList(filteredList));
    }

    ///////////////////////////////////////////////////////////////////////////
    /// ClearFilters
    @FXML
    public void clearFilters() {
        searchField.clear();
        filterRoleComboBox.setValue(null);
    }
    ///////////////////////////////////////////////////////////////////////////
    // Kullanıcı listesini veritabanından alıp tabloyu günceller
    @FXML
    private void refreshTable() {
        // Kullanıcı Listesini Optional olarak Al
        Optional<List<UserDTO>> optionalUsers = userDAO.list(); // Kullanıcı listesini getir

        // Optional al ve eğer boşsa bir liste kullan
        List<UserDTO> userDTOList= optionalUsers.orElseGet(List::of);

        // ObservableList Çevir
        ObservableList<UserDTO> userObservableList= FXCollections.observableArrayList(userDTOList);

        // Tabloyu Yükle
        userTable.setItems(userObservableList);

        // Show Alert
        showAlert("Bilgi", "Tablo başarıyla yenilendi!", Alert.AlertType.INFORMATION);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Kullanıcıya mesaj göstermek için genel bir metod
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Kullanıcı oturumu kapatma işlemi
    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Çıkış Yap");
        alert.setHeaderText("Oturumdan çıkmak istiyor musunuz?");
        alert.setContentText("Emin misiniz?");

        Optional<ButtonType> result = alert.showAndWait();
        // Eğer kullanıcıdan OK gelmişse
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(FXMLPath.LOGIN));
                Parent root = loader.load();
                Stage stage = (Stage) userTable.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                showAlert("Hata", "Giriş sayfasına yönlendirme başarısız!", Alert.AlertType.ERROR);
            }
        }
    }

    /// ////////////////////////////////////////////////////////////////////////////
    /// ////////////////////////////////////////////////////////////////////////////
    /// EXPORT
    @FXML
    public void printTable() {
        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) {
            showAlert("Yazıcı Bulunamadı", "Yazıcı sistemde tanımlı değil.", Alert.AlertType.ERROR);
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(userTable.getScene().getWindow())) {
            boolean success = job.printPage(userTable);
            if (success) {
                job.endJob();
                showAlert("Yazdırma", "Tablo başarıyla yazdırıldı.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Yazdırma Hatası", "Yazdırma işlemi başarısız oldu.", Alert.AlertType.ERROR);
            }
        }
    }

    /// ////////////////////////////////////////////////////////////////////////////
    /// ////////////////////////////////////////////////////////////////////////////
    /// KDV CALCULATOR
    private void showExportOptions(String content) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("TXT", "TXT", "PDF", "EXCEL");
        dialog.setTitle("Dışa Aktar");
        dialog.setHeaderText("KDV sonucu hangi formatta kaydedilsin?");
        dialog.setContentText("Format:");
        dialog.showAndWait().ifPresent(choice -> {
            switch (choice) {
                case "TXT" -> exportAsTxt(content);
                case "PDF" -> exportAsPdf(content);
                case "EXCEL" -> exportAsExcel(content);
            }
        });
    }

    private void exportAsTxt(String content) {
        try {
            Path path = Paths.get(System.getProperty("user.home"), "Desktop",
                    "kdv_" + System.currentTimeMillis() + ".txt");
            Files.writeString(path, content);
            showAlert("Başarılı", "TXT masaüstüne kaydedildi", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Hata", "TXT kaydedilemedi.", Alert.AlertType.ERROR);
        }
    }

    private void exportAsPdf(String content) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(doc, page);
            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA, 12);
            stream.setLeading(14.5f);
            stream.newLineAtOffset(50, 750);
            for (String line : content.split("\n")) {
                stream.showText(line); stream.newLine();
            }
            stream.endText(); stream.close();
            File file = new File(System.getProperty("user.home") + "/Desktop/kdv_" + System.currentTimeMillis() + ".pdf");
            doc.save(file);
            showAlert("Başarılı", "PDF masaüstüne kaydedildi", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Hata", "PDF kaydedilemedi.", Alert.AlertType.ERROR);
        }
    }

    private void exportAsExcel(String content) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("KDV");
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                sheet.createRow(i).createCell(0).setCellValue(lines[i]);
            }
            File file = new File(System.getProperty("user.home") + "/Desktop/kdv_" + System.currentTimeMillis() + ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            showAlert("Başarılı", "Excel masaüstüne kaydedildi", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Hata", "Excel kaydedilemedi.", Alert.AlertType.ERROR);
        }
    }

    /// ////////////////////////////////////////////////////////////////////////////
    /// ////////////////////////////////////////////////////////////////////////////
    /// HESAP MAKİNESİ(CALCULATOR)

    // Eğer uygulaman Linux/macOS'ta çalışabilir olacaksa, şu şekilde platform kontrolü de ekleyebilirsin:
    @FXML
    public void openCalculator() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                Runtime.getRuntime().exec("calc");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open -a Calculator");
            } else if (os.contains("nux")) {
                Runtime.getRuntime().exec("gnome-calculator"); // Linux için
            } else {
                showAlert("Hata", "Bu işletim sistemi desteklenmiyor!", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            showAlert("Hata", "Hesap makinesi açılamadı.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /// ////////////////////////////////////////////////////////////////////////////
    /// ////////////////////////////////////////////////////////////////////////////
    /// KDV CALCULATOR
    // KDV
    @FXML
    public void openKdvCalculator() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("KDV Hesapla");
        dialog.setHeaderText("KDV Hesaplayıcı");

        TextField amountField = new TextField();
        ComboBox<String> kdvBox = new ComboBox<>();
        kdvBox.getItems().addAll("1%", "8%", "18%","20%", "Özel");
        kdvBox.setValue("20%");
        TextField customKdv = new TextField(); customKdv.setDisable(true);
        TextField receiptField = new TextField();
        DatePicker datePicker = new DatePicker();
        Label resultLabel = new Label();

        kdvBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            customKdv.setDisable(!"Özel".equals(newVal));
            if (!"Özel".equals(newVal)) customKdv.clear();
        });

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Tutar:"), amountField);
        grid.addRow(1, new Label("KDV Oranı:"), kdvBox);
        grid.addRow(2, new Label("Özel Oran:"), customKdv);
        grid.addRow(3, new Label("Fiş No:"), receiptField);
        grid.addRow(4, new Label("Tarih:"), datePicker);
        grid.add(resultLabel, 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Hesapla", ButtonBar.ButtonData.OK_DONE), ButtonType.CLOSE);

        dialog.setResultConverter(button -> {
            if (button.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    double rate = switch (kdvBox.getValue()) {
                        case "1%" -> 1;
                        case "8%" -> 8;
                        case "18%" -> 18;
                        case "20%" -> 20;
                        default -> Double.parseDouble(customKdv.getText());
                    };
                    double kdv = amount * rate / 100;
                    double total = amount + kdv;

                    String result = String.format("""
                            Fiş No: %s
                            Tarih: %s
                            Ara Toplam: %.2f ₺
                            KDV (%%%.1f): %.2f ₺
                            Genel Toplam: %.2f ₺
                            """,
                            receiptField.getText(), datePicker.getValue(),
                            amount, rate, kdv, total);
                    resultLabel.setText(result);
                    showExportOptions(result);
                } catch (Exception e) {
                    showAlert("Hata", "Geçersiz giriş.", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /// ////////////////////////////////////////////////////////////////////////////
    /// ////////////////////////////////////////////////////////////////////////////
    /// DIALOG (ADD)
    private static class AddUserDialog extends Dialog<UserDTO> {
        private final TextField usernameField = new TextField();
        private final PasswordField passwordField = new PasswordField();
        private final TextField emailField = new TextField();
        private final ComboBox<String> roleComboBox = new ComboBox<>();

        public AddUserDialog() {
            setTitle("Yeni Kullanıcı Ekle");
            setHeaderText("Yeni kullanıcı bilgilerini girin");

            // Manuel Ekleme
            //roleComboBox.getItems().addAll("USER", "ADMIN", "MODERATOR");
            //roleComboBox.setValue("USER");

            ComboBox<ERole> roleComboBox = new ComboBox<>();
            roleComboBox.getItems().addAll(ERole.values());
            roleComboBox.setValue(ERole.USER); // Varsayılan seçim

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Kullanıcı Adı:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Şifre:"), 0, 1);
            grid.add(passwordField, 1, 1);
            grid.add(new Label("E-posta:"), 0, 2);
            grid.add(emailField, 1, 2);
            grid.add(new Label("Rol:"), 0, 3);
            grid.add(roleComboBox, 1, 3);

            getDialogPane().setContent(grid);

            ButtonType addButtonType = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    return UserDTO.builder()
                            .username(usernameField.getText().trim())
                            .password(passwordField.getText().trim())
                            .email(emailField.getText().trim())
                            .role(roleComboBox.getValue())
                            .build();
                }
                return null;
            });
        }
    }

    @FXML
    public void addUser(ActionEvent actionEvent) {
        AddUserDialog dialog = new AddUserDialog();
        Optional<UserDTO> result = dialog.showAndWait();

        result.ifPresent(newUser -> {
            if (newUser.getUsername().isEmpty() || newUser.getPassword().isEmpty() || newUser.getEmail().isEmpty()) {
                showAlert("Hata", "Tüm alanlar doldurulmalı!", Alert.AlertType.ERROR);
                return;
            }

            if (userDAO.isUsernameExists(newUser.getUsername())) {
                showAlert("Uyarı", "Bu kullanıcı adı zaten kayıtlı!", Alert.AlertType.WARNING);
                return;
            }

            if (userDAO.isEmailExists(newUser.getEmail())) {
                showAlert("Uyarı", "Bu e-posta zaten kayıtlı!", Alert.AlertType.WARNING);
                return;
            }

            Optional<UserDTO> createdUser = userDAO.create(newUser);
            if (createdUser.isPresent()) {
                showAlert("Başarılı", "Kullanıcı başarıyla eklendi!", Alert.AlertType.INFORMATION);
                refreshTable();
            } else {
                showAlert("Hata", "Kullanıcı eklenemedi!", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    public void addUserEski(ActionEvent actionEvent) {
        // Sayfa açılır açılmaz geliyor
        //String role = roleComboBox.getValue();

        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Kullanıcı Ekle");
        usernameDialog.setHeaderText("Kullanıcı Adı");
        usernameDialog.setContentText("Yeni kullanıcı adı giriniz:");
        Optional<String> optionalUsername = usernameDialog.showAndWait();
        if (optionalUsername.isEmpty()) return;
        String username = optionalUsername.get().trim();

        if (userDAO.isUsernameExists(username)) {
            showAlert("Uyarı", "Bu kullanıcı adı zaten kayıtlı!", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Kullanıcı Ekle");
        passwordDialog.setHeaderText("Şifre");
        passwordDialog.setContentText("Yeni şifre giriniz:");
        Optional<String> optionalPassword = passwordDialog.showAndWait();
        if (optionalPassword.isEmpty()) return;
        String password = optionalPassword.get().trim();

        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Kullanıcı Ekle");
        emailDialog.setHeaderText("E-posta");
        emailDialog.setContentText("Yeni e-posta giriniz:");
        Optional<String> optionalEmail = emailDialog.showAndWait();
        if (optionalEmail.isEmpty()) return;
        String email = optionalEmail.get().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert("Hata", "Lütfen tüm alanları doldurun!", Alert.AlertType.ERROR);
            return;
        }

        if (userDAO.isEmailExists(email)) {
            showAlert("Uyarı", "Bu e-posta zaten kayıtlı!", Alert.AlertType.WARNING);
            return;
        }

        UserDTO newUser = UserDTO.builder()
                .username(username)
                .password(password)
                .email(email)
                //.role(role) //// Sayfa açılır açılmaz geliyor
                .build();

        Optional<UserDTO> createdUser = userDAO.create(newUser);
        if (createdUser.isPresent()) {
            showAlert("Başarılı", "Kullanıcı başarıyla eklendi!", Alert.AlertType.INFORMATION);
            refreshTable();
        } else {
            showAlert("Hata", "Kullanıcı eklenirken hata oluştu!", Alert.AlertType.ERROR);
        }
    }

    /// ////////////////////////////////////////////////////////////////////////////
    /// ////////////////////////////////////////////////////////////////////////////
    /// DIALOG (UPDATE)
    private static class UpdateUserDialog extends Dialog<UserDTO> {
        private final TextField usernameField = new TextField();
        private final PasswordField passwordField = new PasswordField();
        private final TextField emailField = new TextField();
        private final ComboBox<ERole> roleComboBox = new ComboBox<>();

        public UpdateUserDialog(UserDTO existingUser) {
            setTitle("Kullanıcı Güncelle");
            setHeaderText("Kullanıcı bilgilerini düzenleyin");

            usernameField.setText(existingUser.getUsername());
            emailField.setText(existingUser.getEmail());

            // 🔥 ENUM kullanımıyla rol listesi
            roleComboBox.getItems().addAll(ERole.values());

            // 🔥 Mevcut role'u enum olarak seç
            try {
                roleComboBox.setValue(ERole.fromString(String.valueOf(existingUser.getRole())));
            } catch (RuntimeException e) {
                roleComboBox.setValue(ERole.USER); // Yedek: varsayılan rol
            }

            // Layout
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Kullanıcı Adı:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Yeni Şifre:"), 0, 1);
            grid.add(passwordField, 1, 1);
            grid.add(new Label("E-posta:"), 0, 2);
            grid.add(emailField, 1, 2);
            grid.add(new Label("Rol:"), 0, 3);
            grid.add(roleComboBox, 1, 3);

            getDialogPane().setContent(grid);

            ButtonType updateButtonType = new ButtonType("Güncelle", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

            // Sonuç döndür
            setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    return UserDTO.builder()
                            .username(usernameField.getText().trim())
                            .password(passwordField.getText().trim().isEmpty()
                                    ? existingUser.getPassword()
                                    : passwordField.getText().trim())
                            .email(emailField.getText().trim())
                            .role(ERole.valueOf(roleComboBox.getValue().name())) // Enum’dan string’e dönüşüm
                            .build();
                }
                return null;
            });
        }
    }

    @FXML
    public void updateUserEski(ActionEvent actionEvent) {
        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert("Uyarı", "Lütfen güncellenecek bir kullanıcı seçin!", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog usernameDialog = new TextInputDialog(selectedUser.getUsername());
        usernameDialog.setTitle("Kullanıcı Adı Güncelle");
        usernameDialog.setHeaderText("Yeni kullanıcı adını girin:");
        Optional<String> newUsername = usernameDialog.showAndWait();
        if (newUsername.isEmpty()) return;

        TextInputDialog passwordDialog = new TextInputDialog();
        passwordDialog.setTitle("Şifre Güncelle");
        passwordDialog.setHeaderText("Yeni şifreyi girin:");
        Optional<String> newPassword = passwordDialog.showAndWait();
        if (newPassword.isEmpty()) return;

        TextInputDialog emailDialog = new TextInputDialog(selectedUser.getEmail());
        emailDialog.setTitle("Email Güncelle");
        emailDialog.setHeaderText("Yeni e-posta adresini girin:");
        Optional<String> newEmail = emailDialog.showAndWait();
        if (newEmail.isEmpty()) return;

        // Sayfa açılır açılmaz geliyor
        //String role = roleComboBox.getValue();

        UserDTO updatedUser = UserDTO.builder()
                .username(newUsername.get())
                .password(newPassword.get())
                .email(newEmail.get())
                //.role(role) //// Sayfa açılır açılmaz geliyor
                .build();

        Optional<UserDTO> result = userDAO.update(selectedUser.getId(), updatedUser);
        if (result.isPresent()) {
            showAlert("Başarılı", "Kullanıcı başarıyla güncellendi!", Alert.AlertType.INFORMATION);
            refreshTable();
        } else {
            showAlert("Hata", "Güncelleme sırasında hata oluştu!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void updateUser(ActionEvent actionEvent) {
        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert("Uyarı", "Lütfen güncellenecek bir kullanıcı seçin!", Alert.AlertType.WARNING);
            return;
        }

        UpdateUserDialog dialog = new UpdateUserDialog(selectedUser);
        Optional<UserDTO> result = dialog.showAndWait();

        result.ifPresent(updatedUser -> {
            if (updatedUser.getUsername().isEmpty() || updatedUser.getPassword().isEmpty() || updatedUser.getEmail().isEmpty()) {
                showAlert("Hata", "Tüm alanlar doldurulmalı!", Alert.AlertType.ERROR);
                return;
            }

            Optional<UserDTO> updated = userDAO.update(selectedUser.getId(), updatedUser);
            if (updated.isPresent()) {
                showAlert("Başarılı", "Kullanıcı güncellendi!", Alert.AlertType.INFORMATION);
                refreshTable();
            } else {
                showAlert("Hata", "Güncelleme işlemi başarısız!", Alert.AlertType.ERROR);
            }
        });
    }


    /// ////////////////////////////////////////////////////////////////////////////
    /// ////////////////////////////////////////////////////////////////////////////
    /// DIALOG (DELETE)
    @FXML
    public void deleteUser(ActionEvent actionEvent) {
        Optional<UserDTO> selectedUser = Optional.ofNullable(userTable.getSelectionModel().getSelectedItem());
        selectedUser.ifPresent(user -> {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Silme Onayı");
            confirmationAlert.setHeaderText("Kullanıcıyı silmek istiyor musunuz?");
            confirmationAlert.setContentText("Silinecek kullanıcı: " + user.getUsername());
            Optional<ButtonType> isDelete = confirmationAlert.showAndWait();
            if (isDelete.isPresent() && isDelete.get() == ButtonType.OK) {
                Optional<UserDTO> deleteUser = userDAO.delete(user.getId());
                if (deleteUser.isPresent()) {
                    showAlert("Başarılı", "Kullanıcı başarıyla silindi", Alert.AlertType.INFORMATION);
                    refreshTable();
                } else {
                    showAlert("Başarısız", "Silme işlemi başarısız oldu", Alert.AlertType.ERROR);
                }
            }
        });
    }
}

