package com.hamitmizrak.ibb_ecodation_javafx.controller;

import com.hamitmizrak.ibb_ecodation_javafx.dao.KdvDAO;
import com.hamitmizrak.ibb_ecodation_javafx.dao.UserDAO;
import com.hamitmizrak.ibb_ecodation_javafx.dto.KdvDTO;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ERole;
import com.hamitmizrak.ibb_ecodation_javafx.utils.FXMLPath;
import com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager;
import com.hamitmizrak.ibb_ecodation_javafx.utils.NotificationManager;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ThemeManager;
import com.hamitmizrak.ibb_ecodation_javafx.utils.UserSession;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

public class AdminController {

    private UserDAO userDAO;
    private KdvDAO kdvDAO;

    public AdminController() {
        userDAO = new UserDAO();
        kdvDAO = new KdvDAO();
    }

    // User İçin
    @FXML private TableView<UserDTO> userTable;
    @FXML private TableColumn<UserDTO, Integer> idColumn;
    @FXML private TableColumn<UserDTO, String> usernameColumn;
    @FXML private TableColumn<UserDTO, String> emailColumn;
    @FXML private TableColumn<UserDTO, String> passwordColumn;
    @FXML private TableColumn<UserDTO, String> roleColumn;
    //@FXML private ComboBox<String> roleComboBox; //// Sayfa açılır açılmaz geliyor
    @FXML private TextField searchField;
    @FXML private ComboBox<ERole> filterRoleComboBox;

    // KDV için
    @FXML
    private TableView<KdvDTO> kdvTable;
    @FXML
    private TableColumn<KdvDTO, Integer> idColumnKdv;
    @FXML
    private TableColumn<KdvDTO, Double> amountColumn;
    @FXML
    private TableColumn<KdvDTO, Double> kdvRateColumn;
    @FXML
    private TableColumn<KdvDTO, Double> kdvAmountColumn;
    @FXML
    private TableColumn<KdvDTO, Double> totalAmountColumn;
    @FXML
    private TableColumn<KdvDTO, String> receiptColumn;
    @FXML
    private TableColumn<KdvDTO, LocalDate> dateColumn;
    @FXML
    private TableColumn<KdvDTO, String> descColumn;
    @FXML
    private TextField searchKdvField;

    @FXML
    private Label clockLabel;

    @FXML
    private Button themeButton;
    
    @FXML
    private Button languageButton;

    @FXML
    private MenuBar menuBar;

    @FXML
    public void initialize() {
        // Debug için - kullanıcı oturumunu kontrol et
        UserDTO currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            System.out.println("Admin paneli açıldı. Aktif kullanıcı: " + currentUser.getUsername());
        } else {
            System.out.println("UYARI: Admin paneli açıldı fakat UserSession boş!");
        }
        
        // Zaman
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                    clockLabel.setText(now.format(formatter));
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // KULLANICI
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Rol filtreleme için ComboBox
        filterRoleComboBox.getItems().add(null); // boş seçenek: tüm roller
        filterRoleComboBox.getItems().addAll(ERole.values());
        filterRoleComboBox.setValue(null); // başlangıçta tüm roller

        // Arama kutusu dinleme
        searchField.textProperty().addListener((observable, oldVal, newVal) -> applyFilters());
        filterRoleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String password, boolean empty) {
                super.updateItem(password, empty);
                setText((empty || password == null) ? null : "******");
            }
        });

        // Sayfa Açılır açılmaz geliyor
        //roleComboBox.setItems(FXCollections.observableArrayList("USER", "ADMIN", "MODERATOR"));
        //roleComboBox.getSelectionModel().select("USER");
        refreshTable();

        // KDV İÇİN
        // KDV tablosunu hazırla
        idColumnKdv.setCellValueFactory(new PropertyValueFactory<>("id"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        kdvRateColumn.setCellValueFactory(new PropertyValueFactory<>("kdvRate"));
        kdvAmountColumn.setCellValueFactory(new PropertyValueFactory<>("kdvAmount"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        receiptColumn.setCellValueFactory(new PropertyValueFactory<>("receiptNumber"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        searchKdvField.textProperty().addListener((obs, oldVal, newVal) -> applyKdvFilter());

        refreshKdvTable();

        // Initialize theme button based on current state
        if (themeButton != null) {
            themeButton.setText(ThemeManager.isDarkTheme() ? "☀️ Aydınlık Mod" : "🌙 Karanlık Mod");
        }
        
        // Initialize language button based on current state
        if (languageButton != null) {
            languageButton.setText("🌐 " + (LanguageManager.isTurkish() ? "EN" : "TR"));
        }
    }

    // KULLANICI
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

    @FXML
    public void clearFilters() {
        searchField.clear();
        filterRoleComboBox.setValue(null);
    }

    @FXML
    public void openKdvPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hamitmizrak/ibb_ecodation_javafx/view/kdv.fxml"));
            Parent kdvRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("KDV Paneli");
            stage.setScene(new Scene(kdvRoot));
            stage.show();
        } catch (IOException e) {
            showAlert("Hata", "KDV ekranı açılamadı!", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    private void refreshTable() {
        applyFilters();
        Optional<List<UserDTO>> optionalUsers = userDAO.list();
        List<UserDTO> userDTOList = optionalUsers.orElseGet(List::of);
        ObservableList<UserDTO> observableList = FXCollections.observableArrayList(userDTOList);
        userTable.setItems(observableList);
        
        // Show notification only if scene is not null (e.g. during initialization)
        if (userTable.getScene() != null && userTable.getScene().getWindow() != null) {
            Window owner = userTable.getScene().getWindow();
            NotificationManager.showSuccess(owner, LanguageManager.getString("alert.table.refresh"));
        }
    }

    private void showAlert(String titleKey, String messageKey, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(LanguageManager.getString(titleKey));
        alert.setContentText(LanguageManager.getString(messageKey));
        
        // Apply theme styling to dialog
        ThemeManager.styleDialog(alert);
        
        alert.showAndWait();
        
        // Also show as a notification, but only if scene is available
        if (userTable != null && userTable.getScene() != null && userTable.getScene().getWindow() != null) {
            Window owner = userTable.getScene().getWindow();
            if (type == Alert.AlertType.ERROR) {
                NotificationManager.showError(owner, LanguageManager.getString(messageKey));
            } else if (type == Alert.AlertType.WARNING) {
                NotificationManager.showWarning(owner, LanguageManager.getString(messageKey));
            } else if (type == Alert.AlertType.INFORMATION) {
                NotificationManager.showInfo(owner, LanguageManager.getString(messageKey));
            } else if (type == Alert.AlertType.CONFIRMATION) {
                NotificationManager.showInfo(owner, LanguageManager.getString(messageKey));
            }
        }
    }

    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.getString("confirm.logout.title"));
        alert.setHeaderText(LanguageManager.getString("confirm.logout.header"));
        alert.setContentText(LanguageManager.getString("confirm.logout.content"));
        
        // Apply theme styling to dialog
        ThemeManager.styleDialog(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Clear user session
                UserSession.getInstance().logout();
                System.out.println("Kullanıcı oturumu kapatıldı");
                
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(FXMLPath.LOGIN),
                    LanguageManager.getResourceBundle()
                );
                Parent root = loader.load();
                Scene loginScene = new Scene(root);
                ThemeManager.setTheme(loginScene, ThemeManager.isDarkTheme());
                
                Stage stage = (Stage) userTable.getScene().getWindow();
                stage.setScene(loginScene);
                stage.show();
            } catch (IOException e) {
                showAlert("alert.error", "login.screen.load.error", Alert.AlertType.ERROR);
            }
        }
    }

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

    @FXML
    public void openKdvCalculator() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("KDV Hesapla");
        dialog.setHeaderText("KDV Hesaplayıcı");

        TextField amountField = new TextField();
        ComboBox<String> kdvBox = new ComboBox<>();
        kdvBox.getItems().addAll("1%", "8%", "18%", "Özel");
        kdvBox.setValue("18%");
        TextField customKdv = new TextField(); customKdv.setDisable(true);
        TextField receiptField = new TextField();
        DatePicker datePicker = new DatePicker();
        Label resultLabel = new Label();

        // Apply style classes to form elements
        amountField.getStyleClass().add("input-field");
        customKdv.getStyleClass().add("input-field");
        receiptField.getStyleClass().add("input-field");
        datePicker.getStyleClass().add("input-field");
        
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

        // Apply theme styling to dialog
        ThemeManager.styleDialog(dialog);
        
        dialog.setResultConverter(button -> {
            if (button.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    double rate = switch (kdvBox.getValue()) {
                        case "1%" -> 1;
                        case "8%" -> 8;
                        case "18%" -> 18;
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

    private void showExportOptions(String content) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("TXT", "TXT", "PDF", "EXCEL", "MAIL");
        dialog.setTitle("Dışa Aktar");
        dialog.setHeaderText("KDV sonucu nasıl dışa aktarılsın?");
        dialog.setContentText("Format:");
        
        // Apply theme styling to dialog
        ThemeManager.styleDialog(dialog);
        
        dialog.showAndWait().ifPresent(choice -> {
            switch (choice) {
                case "TXT" -> exportAsTxt(content);
                case "PDF" -> exportAsPdf(content);
                case "EXCEL" -> exportAsExcel(content);
                case "MAIL" -> sendMail(content);
            }
        });
    }

    private void sendMail(String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("E-Posta Gönder");
        dialog.setHeaderText("KDV sonucunu göndereceğiniz e-posta adresini girin:");
        dialog.setContentText("E-posta:");
        
        // Apply theme styling to dialog
        ThemeManager.styleDialog(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(receiver -> {
            String senderEmail = "seninmailin@gmail.com"; // değiştir
            String senderPassword = "uygulama-sifresi"; // değiştir
            String host = "smtp.gmail.com";
            int port = 587;

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
                message.setSubject("KDV Hesaplama Sonucu");
                message.setText(content);

                Transport.send(message);

                showAlert("Başarılı", "Mail başarıyla gönderildi!", Alert.AlertType.INFORMATION);
            } catch (MessagingException e) {
                e.printStackTrace();
                showAlert("Hata", "Mail gönderilemedi.", Alert.AlertType.ERROR);
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
                String safeLine = line.replace("\t", "    ");
                stream.showText(safeLine);
                stream.newLine();
            }

            stream.endText();
            stream.close();

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

            // Stil tanımı (isteğe bağlı)
            var headerStyle = wb.createCellStyle();
            var font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Başlıkları yaz
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "Tutar", "KDV Oranı", "KDV Tutarı", "Toplam", "Fiş No", "Tarih", "Açıklama"};
            for (int i = 0; i < headers.length; i++) {
                var cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Satırları yaz
            int rowNum = 1;
            for (KdvDTO kdv : kdvTable.getItems()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(kdv.getId());
                row.createCell(1).setCellValue(kdv.getAmount());
                row.createCell(2).setCellValue(kdv.getKdvRate());
                row.createCell(3).setCellValue(kdv.getKdvAmount());
                row.createCell(4).setCellValue(kdv.getTotalAmount());
                row.createCell(5).setCellValue(kdv.getReceiptNumber());
                row.createCell(6).setCellValue(String.valueOf(kdv.getTransactionDate()));
                row.createCell(7).setCellValue(kdv.getDescription());
            }

            // Otomatik sütun genişliği ayarla
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Kaydet
            File file = new File(System.getProperty("user.home") + "/Desktop/kdv_" + System.currentTimeMillis() + ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            showAlert("Başarılı", "Excel masaüstüne kaydedildi", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Hata", "Excel kaydedilemedi.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    public void exportKdvAsTxt() {
        exportAsTxt(generateKdvSummary());
    }

    @FXML
    public void exportKdvAsPdf() {
        exportAsPdf(generateKdvSummary());
    }

    @FXML
    public void exportKdvAsExcel() {
        exportAsExcel(generateKdvSummary());
    }

    @FXML
    public void printKdvTable() {
        // Kdv tablosunu yazdır
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(kdvTable.getScene().getWindow())) {
            boolean success = job.printPage(kdvTable);
            if (success) {
                job.endJob();
                showAlert("Yazdırma", "KDV tablosu yazdırıldı.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Hata", "Yazdırma başarısız.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void sendKdvByMail() {
        sendMail(generateKdvSummary());
    }


    private String generateKdvSummary() {
        StringBuilder builder = new StringBuilder();
        builder.append("ID\tTutar\tKDV Oranı\tKDV Tutarı\tToplam\tFiş No\tTarih\tAçıklama\n");
        for (KdvDTO kdv : kdvTable.getItems()) {
            builder.append(String.format("%d\t%.2f\t%.2f%%\t%.2f\t%.2f\t%s\t%s\t%s\n",
                    kdv.getId(),
                    kdv.getAmount(),
                    kdv.getKdvRate(),
                    kdv.getKdvAmount(),
                    kdv.getTotalAmount(),
                    kdv.getReceiptNumber(),
                    kdv.getTransactionDate(),
                    kdv.getDescription()));
        }
        return builder.toString();
    }


    @FXML
    private void handleNew() {
        System.out.println("Yeni oluşturuluyor...");
    }

    @FXML
    private void handleOpen() {
        System.out.println("Dosya açılıyor...");
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void goToUsers(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/path/to/user.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void goToSettings(ActionEvent event) throws IOException {
       /* Parent root = FXMLLoader.load(getClass().getResource("/path/to/settings.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();*/
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hakkında");
        alert.setHeaderText("Uygulama Bilgisi");
        alert.setContentText("Bu uygulama JavaFX ile geliştirilmiştir.");
        alert.showAndWait();
    }




    /// //////////////////////////////////////////////////////////
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
                            .role(ERole.valueOf(roleComboBox.getValue().name())) // Enum'dan string'e dönüşüm
                            .build();
                }
                return null;
            });
        }
    }


    @FXML
    public void updateUser(ActionEvent actionEvent) {
        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            showAlert("Uyarı", "Lütfen güncellenecek bir kullanıcı seçin!", Alert.AlertType.WARNING);
            return;
        }

        // Show the popup for updating the user
        showUserUpdatePopup(selectedUser);
    }

    private void showUserUpdatePopup(UserDTO existingUser) {
        // Create the popup
        Popup popup = new Popup();
        popup.setAutoHide(true); // Click outside to close
        
        // Create the content using VBox
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMinWidth(450);
        content.setMaxWidth(450);
        
        // Create header
        Label titleLabel = new Label("Kullanıcı Güncelle");
        titleLabel.getStyleClass().addAll("title-label");
        
        // Create form fields
        TextField usernameField = new TextField(existingUser.getUsername());
        PasswordField passwordField = new PasswordField();
        TextField emailField = new TextField(existingUser.getEmail());
        
        ComboBox<ERole> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(ERole.values());
        
        // Set current role
        try {
            roleComboBox.setValue(ERole.fromString(String.valueOf(existingUser.getRole())));
        } catch (RuntimeException e) {
            roleComboBox.setValue(ERole.USER);
        }
        
        // Create a grid for form layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 20, 0));
        grid.getStyleClass().add("grid-pane");
        
        // Add field labels with styling
        Label usernameLabel = new Label("Kullanıcı Adı:");
        Label passwordLabel = new Label("Yeni Şifre:");
        Label emailLabel = new Label("E-posta:");
        Label roleLabel = new Label("Rol:");
        
        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(emailLabel, 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(roleLabel, 0, 3);
        grid.add(roleComboBox, 1, 3);
        
        // Ensure columns are properly sized
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(300);
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        
        // Create buttons with modern styling
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveButton = new Button("Güncelle");
        Button cancelButton = new Button("İptal");
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        // Add components to the content VBox
        content.getChildren().addAll(titleLabel, grid, buttonBox);
        
        // Set event handlers
        saveButton.setOnAction(e -> {
            // Validate fields
            if (usernameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                showAlert("Hata", "Tüm alanlar doldurulmalı!", Alert.AlertType.ERROR);
                return;
            }
            
            // Create updated user object
            UserDTO updatedUser = UserDTO.builder()
                    .username(usernameField.getText().trim())
                    .password(passwordField.getText().trim().isEmpty() 
                            ? existingUser.getPassword() 
                            : passwordField.getText().trim())
                    .email(emailField.getText().trim())
                    .role(ERole.valueOf(roleComboBox.getValue().name()))
                    .build();
            
            // Update in database
            Optional<UserDTO> updated = userDAO.update(existingUser.getId(), updatedUser);
            if (updated.isPresent()) {
                showAlert("Başarılı", "Kullanıcı güncellendi!", Alert.AlertType.INFORMATION);
                refreshTable();
            } else {
                showAlert("Hata", "Güncelleme işlemi başarısız!", Alert.AlertType.ERROR);
            }
            
            // Close popup
            popup.hide();
        });
        
        cancelButton.setOnAction(e -> popup.hide());
        
        // Add content to popup
        popup.getContent().add(content);
        
        // Apply theme styling
        ThemeManager.stylePopup(popup);
        
        // Center the popup on the window
        Window window = userTable.getScene().getWindow();
        double centerX = window.getX() + (window.getWidth() / 2) - 225;
        double centerY = window.getY() + (window.getHeight() / 2) - 200;
        popup.show(window, centerX, centerY);
    }


    @FXML
    public void deleteUser(ActionEvent actionEvent) {
        UserDTO selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            // Show notification instead of alert
            Window owner = userTable.getScene().getWindow();
            NotificationManager.showWarning(owner, "Lütfen silinecek bir kullanıcı seçin");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.getString("confirm.delete.title"));
        alert.setHeaderText(LanguageManager.getString("confirm.delete.header"));
        alert.setContentText(LanguageManager.getString("confirm.delete.content"));
        
        // Apply theme styling to dialog
        ThemeManager.styleDialog(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Optional<UserDTO> deleted = userDAO.delete(selectedUser.getId());
                if (deleted.isPresent()) {
                    refreshTable();
                    // Show notification instead of alert
                    Window owner = userTable.getScene().getWindow();
                    NotificationManager.showSuccess(owner, LanguageManager.getString("user.delete.success"));
                } else {
                    // Show notification instead of alert
                    Window owner = userTable.getScene().getWindow();
                    NotificationManager.showError(owner, LanguageManager.getString("user.delete.error"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Show notification instead of alert
                Window owner = userTable.getScene().getWindow();
                NotificationManager.showError(owner, e.getMessage());
            }
        }
    }

    // KDV
    // 📄 Listeyi yenile
    private void refreshKdvTable() {
        Optional<List<KdvDTO>> list = kdvDAO.list();
        list.ifPresent(data -> kdvTable.setItems(FXCollections.observableArrayList(data)));
    }

    // 🔎 Arama filtreleme
    private void applyKdvFilter() {
        String keyword = searchKdvField.getText().trim().toLowerCase();
        Optional<List<KdvDTO>> all = kdvDAO.list();
        List<KdvDTO> filtered = all.orElse(List.of()).stream()
                .filter(kdv -> kdv.getReceiptNumber().toLowerCase().contains(keyword))
                .toList();
        kdvTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // ➕ KDV ekle
    @FXML
    public void addKdv() {
        // Show KDV popup for creating a new KDV entry
        showKdvPopup(null);
    }
    
    // ✏️ KDV güncelle
    @FXML
    public void updateKdv() {
        KdvDTO selected = kdvTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Güncellenecek bir kayıt seçin.", Alert.AlertType.WARNING);
            return;
        }

        // Show KDV popup for updating an existing KDV entry
        showKdvPopup(selected);
    }

    // ❌ KDV sil
    @FXML
    public void deleteKdv() {
        KdvDTO selectedKdv = kdvTable.getSelectionModel().getSelectedItem();
        if (selectedKdv == null) {
            // Show notification instead of alert
            Window owner = kdvTable.getScene().getWindow();
            NotificationManager.showWarning(owner, "Lütfen silinecek bir KDV kaydı seçin");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.getString("confirm.delete.title"));
        alert.setHeaderText(LanguageManager.getString("confirm.delete.header"));
        alert.setContentText(LanguageManager.getString("confirm.delete.content"));
        
        // Apply theme styling to dialog
        ThemeManager.styleDialog(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean isDeleted = kdvDAO.delete(selectedKdv.getId()) != null;
                if (isDeleted) {
                    refreshKdvTable();
                    // Show notification instead of alert
                    Window owner = kdvTable.getScene().getWindow();
                    NotificationManager.showSuccess(owner, "KDV kaydı başarıyla silindi");
                } else {
                    // Show notification instead of alert
                    Window owner = kdvTable.getScene().getWindow();
                    NotificationManager.showError(owner, "KDV kaydı silinemedi");
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Show notification instead of alert
                Window owner = kdvTable.getScene().getWindow();
                NotificationManager.showError(owner, e.getMessage());
            }
        }
    }
    
    private void showKdvPopup(KdvDTO existing) {
        // Create the popup
        Popup popup = new Popup();
        popup.setAutoHide(true); // Click outside to close
        
        // Create the content using VBox
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setMinWidth(450);
        content.setMaxWidth(450);
        
        // Create header
        Label titleLabel = new Label(existing == null ? "Yeni KDV Ekle" : "KDV Güncelle");
        titleLabel.getStyleClass().addAll("title-label");
        
        // Create form fields
        TextField amountField = new TextField();
        TextField rateField = new TextField();
        TextField receiptField = new TextField();
        DatePicker datePicker = new DatePicker();
        TextField descField = new TextField();
        ComboBox<String> exportCombo = new ComboBox<>();
        exportCombo.getItems().addAll("TXT", "PDF", "EXCEL");
        exportCombo.setValue("TXT");
        
        if (existing != null) {
            amountField.setText(String.valueOf(existing.getAmount()));
            rateField.setText(String.valueOf(existing.getKdvRate()));
            receiptField.setText(existing.getReceiptNumber());
            datePicker.setValue(existing.getTransactionDate());
            descField.setText(existing.getDescription());
            exportCombo.setValue(existing.getExportFormat());
        }
        
        // Create a grid for form layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 20, 0));
        grid.getStyleClass().add("grid-pane");
        
        // Add field labels with styling
        Label amountLabel = new Label("Tutar:");
        Label rateLabel = new Label("KDV Oranı (%):");
        Label receiptLabel = new Label("Fiş No:");
        Label dateLabel = new Label("Tarih:");
        Label descLabel = new Label("Açıklama:");
        Label formatLabel = new Label("Format:");
        
        grid.add(amountLabel, 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(rateLabel, 0, 1);
        grid.add(rateField, 1, 1);
        grid.add(receiptLabel, 0, 2);
        grid.add(receiptField, 1, 2);
        grid.add(dateLabel, 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(descLabel, 0, 4);
        grid.add(descField, 1, 4);
        grid.add(formatLabel, 0, 5);
        grid.add(exportCombo, 1, 5);
        
        // Ensure columns are properly sized
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPrefWidth(280);
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        
        // Create buttons with modern styling
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveButton = new Button(existing == null ? "Ekle" : "Güncelle");
        Button cancelButton = new Button("İptal");
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        // Add components to the content VBox
        content.getChildren().addAll(titleLabel, grid, buttonBox);
        
        // Set event handlers
        saveButton.setOnAction(e -> {
            try {
                KdvDTO kdvDTO = KdvDTO.builder()
                        .amount(Double.parseDouble(amountField.getText()))
                        .kdvRate(Double.parseDouble(rateField.getText()))
                        .receiptNumber(receiptField.getText())
                        .transactionDate(datePicker.getValue())
                        .description(descField.getText())
                        .exportFormat(exportCombo.getValue())
                        .build();
                        
                if (kdvDTO.isValid()) {
                    if (existing == null) {
                        // Create new KDV entry
                        kdvDAO.create(kdvDTO);
                        showAlert("Başarılı", "KDV kaydı eklendi.", Alert.AlertType.INFORMATION);
                    } else {
                        // Update existing KDV entry
                        kdvDAO.update(existing.getId(), kdvDTO);
                        showAlert("Başarılı", "KDV kaydı güncellendi.", Alert.AlertType.INFORMATION);
                    }
                    refreshKdvTable();
                    popup.hide();
                } else {
                    showAlert("Hata", "Geçersiz KDV bilgileri!", Alert.AlertType.ERROR);
                }
            } catch (Exception ex) {
                showAlert("Hata", "Geçersiz veri girdiniz!", Alert.AlertType.ERROR);
            }
        });
        
        cancelButton.setOnAction(e -> popup.hide());
        
        // Add content to popup
        popup.getContent().add(content);
        
        // Apply theme styling
        ThemeManager.stylePopup(popup);
        
        // Center the popup on the window
        Window window = kdvTable.getScene().getWindow();
        double centerX = window.getX() + (window.getWidth() / 2) - 225;
        double centerY = window.getY() + (window.getHeight() / 2) - 200;
        popup.show(window, centerX, centerY);
    }

    // BİTİRME PROJESİ
    @FXML
    private void toggleTheme(ActionEvent event) {
        ThemeManager.toggleTheme(userTable.getScene(), themeButton);
        themeButton.setText(ThemeManager.isDarkTheme() ? "☀️ " + LanguageManager.getString("admin.theme.light") : "🌙 " + LanguageManager.getString("admin.theme.dark"));
        
        // Show notification
        Window owner = userTable.getScene().getWindow();
        NotificationManager.showInfo(owner, "Tema değiştirildi: " + (ThemeManager.isDarkTheme() ? "Karanlık Mod" : "Aydınlık Mod"));
    }

    @FXML
    private void languageTheme(ActionEvent event) {
        LanguageManager.toggleLanguage();
        updateUILanguage();
        languageButton.setText("🌐 " + (LanguageManager.isTurkish() ? "EN" : "TR"));
        
        // Show notification
        Window owner = userTable.getScene().getWindow();
        NotificationManager.showInfo(owner, "Dil değiştirildi: " + (LanguageManager.isTurkish() ? "Türkçe" : "English"));
    }
    
    /**
     * Update UI elements with current language strings
     */
    private void updateUILanguage() {
        // Update headers, labels, and other text elements
        Stage stage = (Stage) userTable.getScene().getWindow();
        stage.setTitle(LanguageManager.getString("app.title"));
        
        // Update all text elements with localized strings
        // For demonstration, I'll update some key elements
        
        // Main header label - Find the main title label in the scene hierarchy
        BorderPane root = (BorderPane) userTable.getScene().getRoot();
        VBox topContainer = (VBox) root.getTop();
        HBox headerBox = (HBox) topContainer.getChildren().get(0);
        
        for (Node node : headerBox.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getText().contains("Yönetim Paneli") || 
                    label.getText().contains("Admin Panel")) {
                    label.setText(LanguageManager.getString("admin.panel"));
                    break;
                }
            }
        }
        
        // Update menu items
        for (Menu menu : menuBar.getMenus()) {
            if (menu.getText().equals("Dosya") || menu.getText().equals("File")) {
                menu.setText(LanguageManager.getString("menu.file"));
                for (MenuItem item : menu.getItems()) {
                    if (item.getText().equals("Çıkış") || item.getText().equals("Exit")) {
                        item.setText(LanguageManager.getString("menu.file.exit"));
                    }
                }
            } else if (menu.getText().equals("Kullanıcı") || menu.getText().equals("User")) {
                menu.setText(LanguageManager.getString("menu.user"));
                for (MenuItem item : menu.getItems()) {
                    if (item.getText().equals("Kullanıcı Ekle") || item.getText().equals("Add User")) {
                        item.setText(LanguageManager.getString("menu.user.add"));
                    } else if (item.getText().equals("Kullanıcı Güncelle") || item.getText().equals("Update User")) {
                        item.setText(LanguageManager.getString("menu.user.update"));
                    } else if (item.getText().equals("Kullanıcı Sil") || item.getText().equals("Delete User")) {
                        item.setText(LanguageManager.getString("menu.user.delete"));
                    }
                }
            }
            // Update other menus similarly...
        }
        
        // Update table columns for user table
        idColumn.setText(LanguageManager.getString("user.id"));
        usernameColumn.setText(LanguageManager.getString("user.username"));
        emailColumn.setText(LanguageManager.getString("user.email"));
        passwordColumn.setText(LanguageManager.getString("user.password"));
        roleColumn.setText(LanguageManager.getString("user.role"));
        
        // Update table columns for KDV table
        idColumnKdv.setText(LanguageManager.getString("kdv.id"));
        amountColumn.setText(LanguageManager.getString("kdv.amount"));
        kdvRateColumn.setText(LanguageManager.getString("kdv.rate"));
        kdvAmountColumn.setText(LanguageManager.getString("kdv.kdv.amount"));
        totalAmountColumn.setText(LanguageManager.getString("kdv.total"));
        receiptColumn.setText(LanguageManager.getString("kdv.receipt"));
        dateColumn.setText(LanguageManager.getString("kdv.date"));
        descColumn.setText(LanguageManager.getString("kdv.description"));
        
        // Update prompts for text fields
        searchField.setPromptText(LanguageManager.getString("user.search"));
        searchKdvField.setPromptText(LanguageManager.getString("kdv.search"));
    }

    @FXML
    private void showNotifications(ActionEvent event) {
        try {
            // Düzeltilmiş dosya yolu
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/hamitmizrak/ibb_ecodation_javafx/notifications-view.fxml"));
            
            // ResourceBundle'ı doğru şekilde ayarla
            fxmlLoader.setResources(LanguageManager.getResourceBundle());
            
            Parent root = fxmlLoader.load();
            
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 400);
            
            // Apply current theme
            ThemeManager.setTheme(scene, ThemeManager.isDarkTheme());
            
            stage.setTitle(LanguageManager.getString("notifications.title"));
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(userTable.getScene().getWindow());
            stage.show();
            
            // Show notification for opening notifications panel
            Window owner = userTable.getScene().getWindow();
            NotificationManager.showInfo(owner, LanguageManager.getString("notifications.opened"));
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading notifications: " + e.getMessage());
            showAlert("alert.error", "notifications.load.error", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showProfile(ActionEvent event) {
        try {
            // Debug - kullanıcı oturumunu kontrol et
            UserDTO currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser != null) {
                System.out.println("Profil sayfasına geçiliyor. Aktif kullanıcı: " + currentUser.getUsername());
            } else {
                System.out.println("UYARI: Profil sayfasına geçilirken UserSession boş!");
                
                // Test için geçici kullanıcı (gerçek uygulamada kaldırılmalı)
                currentUser = UserDTO.builder()
                    .id(1)
                    .username("admin_test")
                    .email("admin@example.com")
                    .role(ERole.ADMIN)
                    .password("test123")
                    .build();
                UserSession.getInstance().setCurrentUser(currentUser);
                System.out.println("Test kullanıcısı oluşturuldu: " + currentUser.getUsername());
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource(FXMLPath.PROFILE),
                LanguageManager.getResourceBundle()
            );
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent);
            ThemeManager.setTheme(scene, ThemeManager.isDarkTheme());
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Profil Yönetimi");
            stage.show();
        } catch (Exception e) {
            System.out.println("Profil sayfasına yönlendirme başarısız: " + e.getMessage());
            e.printStackTrace();
            showAlert("Hata", "Profil sayfası açılamadı.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void backupData(ActionEvent event) {
        // Veritabanı yedekleme işlemleri burada yapılacak
    }

    @FXML
    private void restoreData(ActionEvent event) {
        // Daha önce alınmış bir yedek dosyadan veri geri yüklenecek
    }


    @FXML
    private void notebook(ActionEvent event) {
        // Daha önce alınmış bir yedek dosyadan veri geri yüklenecek
    }

}
