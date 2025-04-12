package com.hamitmizrak.ibb_ecodation_javafx.controller;

import com.hamitmizrak.ibb_ecodation_javafx.HelloApplication;
import com.hamitmizrak.ibb_ecodation_javafx.dao.NoteBookDaoImpl;
import com.hamitmizrak.ibb_ecodation_javafx.dto.NotebookDTO;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NoteBookController implements Initializable {

    // DAO
    private final NoteBookDaoImpl noteBookDao = new NoteBookDaoImpl();
    
    // Geçerli kullanıcı
    private UserDTO currentUser;
    
    // Hatırlatıcı zamanlayıcısı
    private ScheduledExecutorService reminderScheduler;
    
    // UI Bileşenleri
    @FXML
    private TextField txtTitle;
    
    @FXML
    private TextArea txtContent;
    
    @FXML
    private ComboBox<String> cmbCategory;
    
    @FXML
    private CheckBox chkPinned;
    
    @FXML
    private DatePicker dpReminderDate;
    
    @FXML
    private ComboBox<String> cmbReminderHour;
    
    @FXML
    private ComboBox<String> cmbReminderMinute;
    
    @FXML
    private TableView<NotebookDTO> tblNotes;
    
    @FXML
    private TableColumn<NotebookDTO, Long> colId;
    
    @FXML
    private TableColumn<NotebookDTO, String> colTitle;
    
    @FXML
    private TableColumn<NotebookDTO, String> colCategory;
    
    @FXML
    private TableColumn<NotebookDTO, String> colCreatedDate;
    
    @FXML
    private TableColumn<NotebookDTO, String> colReminderDateTime;
    
    @FXML
    private TableColumn<NotebookDTO, Boolean> colPinned;
    
    @FXML
    private Label lblStatus;
    
    @FXML
    private Button btnSave;
    
    @FXML
    private Button btnUpdate;
    
    @FXML
    private Button btnDelete;
    
    @FXML
    private Button btnClear;
    
    private ObservableList<NotebookDTO> notesList = FXCollections.observableArrayList();
    
    // Seçilen not
    private NotebookDTO selectedNote;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Kategori seçeneklerini doldur
        cmbCategory.setItems(FXCollections.observableArrayList(
            "Kişisel", "İş", "Okul", "Alışveriş", "Önemli", "Diğer"
        ));
        
        // Saat ve dakika seçeneklerini doldur
        List<String> hours = new ArrayList<>();
        List<String> minutes = new ArrayList<>();
        
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%02d", i));
        }
        
        cmbReminderHour.setItems(FXCollections.observableArrayList(hours));
        cmbReminderMinute.setItems(FXCollections.observableArrayList(minutes));
        
        // Başlangıç değerleri
        LocalTime now = LocalTime.now();
        cmbReminderHour.setValue(String.format("%02d", now.getHour()));
        cmbReminderMinute.setValue(String.format("%02d", now.getMinute()));
        dpReminderDate.setValue(LocalDate.now());
        
        // Tablo sütunlarını yapılandır
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        // Tarih formatını özelleştir
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        colCreatedDate.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getCreatedDate();
            return new SimpleStringProperty(dateTime != null ? dateTime.format(dateFormatter) : "");
        });
        
        colReminderDateTime.setCellValueFactory(cellData -> {
            LocalDateTime reminderDateTime = cellData.getValue().getReminderDateTime();
            return new SimpleStringProperty(reminderDateTime != null ? reminderDateTime.format(dateFormatter) : "");
        });
        
        colPinned.setCellValueFactory(new PropertyValueFactory<>("pinned"));
        
        // Tablodan seçim yapma işleyicisi
        tblNotes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedNote = newVal;
            if (selectedNote != null) {
                displayNoteDetails(selectedNote);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            } else {
                clearForm();
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
            }
        });
        
        // Başlangıç durumu
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        
        // Hatırlatıcı zamanlayıcısını başlat
        startReminderScheduler();
    }
    
    // Kullanıcıyı ayarla ve notları listele
    public void setUser(UserDTO user) {
        this.currentUser = user;
        loadUserNotes();
    }
    
    // Kullanıcıya ait notları yükle
    private void loadUserNotes() {
        if (currentUser != null) {
            Optional<List<NotebookDTO>> userNotes = noteBookDao.findByUserID(currentUser.getId().longValue());
            userNotes.ifPresent(notes -> {
                notesList.clear();
                notesList.addAll(notes);
                tblNotes.setItems(notesList);
            });
        }
    }
    
    // Seçilen notun detaylarını form alanlarına yükle
    private void displayNoteDetails(NotebookDTO note) {
        txtTitle.setText(note.getTitle());
        txtContent.setText(note.getContent());
        cmbCategory.setValue(note.getCategory());
        chkPinned.setSelected(note.isPinned());
        
        // Hatırlatıcı zamanını ayarla (eğer varsa)
        if (note.getReminderDateTime() != null) {
            dpReminderDate.setValue(note.getReminderDateTime().toLocalDate());
            cmbReminderHour.setValue(String.format("%02d", note.getReminderDateTime().getHour()));
            cmbReminderMinute.setValue(String.format("%02d", note.getReminderDateTime().getMinute()));
        } else {
            dpReminderDate.setValue(LocalDate.now());
            LocalTime now = LocalTime.now();
            cmbReminderHour.setValue(String.format("%02d", now.getHour()));
            cmbReminderMinute.setValue(String.format("%02d", now.getMinute()));
        }
    }
    
    // Form alanlarından NotebookDTO oluştur
    private NotebookDTO createNoteFromForm() {
        NotebookDTO note = new NotebookDTO();
        note.setTitle(txtTitle.getText().trim());
        note.setContent(txtContent.getText().trim());
        note.setCategory(cmbCategory.getValue());
        note.setPinned(chkPinned.isSelected());
        
        // Hatırlatıcı zamanını ayarla
        if (dpReminderDate.getValue() != null) {
            LocalDate reminderDate = dpReminderDate.getValue();
            int hour = Integer.parseInt(cmbReminderHour.getValue());
            int minute = Integer.parseInt(cmbReminderMinute.getValue());
            
            LocalDateTime reminderDateTime = LocalDateTime.of(
                reminderDate, 
                LocalTime.of(hour, minute)
            );
            
            note.setReminderDateTime(reminderDateTime);
        }
        
        note.setUserDTO(currentUser);
        
        return note;
    }
    
    // Yeni not kaydet
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        NotebookDTO newNote = createNoteFromForm();
        Optional<NotebookDTO> savedNote = noteBookDao.create(newNote);
        
        if (savedNote.isPresent()) {
            showStatus("Not başarıyla kaydedildi!", false);
            clearForm();
            loadUserNotes();
        } else {
            showStatus("Not kaydedilirken bir hata oluştu!", true);
        }
    }
    
    // Notu güncelle
    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedNote == null) {
            showStatus("Lütfen güncellenecek bir not seçin!", true);
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        NotebookDTO updatedNote = createNoteFromForm();
        Optional<NotebookDTO> result = noteBookDao.update(selectedNote.getId().intValue(), updatedNote);
        
        if (result.isPresent()) {
            showStatus("Not başarıyla güncellendi!", false);
            clearForm();
            loadUserNotes();
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
        } else {
            showStatus("Not güncellenirken bir hata oluştu!", true);
        }
    }
    
    // Notu sil
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedNote == null) {
            showStatus("Lütfen silinecek bir not seçin!", true);
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Not Sil");
        confirmDialog.setHeaderText("Not Silme Onayı");
        confirmDialog.setContentText("Bu notu silmek istediğinizden emin misiniz?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Optional<NotebookDTO> deleteResult = noteBookDao.delete(selectedNote.getId().intValue());
            
            if (deleteResult.isPresent()) {
                showStatus("Not başarıyla silindi!", false);
                clearForm();
                loadUserNotes();
                btnUpdate.setDisable(true);
                btnDelete.setDisable(true);
            } else {
                showStatus("Not silinirken bir hata oluştu!", true);
            }
        }
    }
    
    // Formu temizle
    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        tblNotes.getSelectionModel().clearSelection();
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }
    
    // Form alanlarını temizle
    private void clearForm() {
        txtTitle.clear();
        txtContent.clear();
        cmbCategory.setValue(null);
        chkPinned.setSelected(false);
        dpReminderDate.setValue(LocalDate.now());
        
        LocalTime now = LocalTime.now();
        cmbReminderHour.setValue(String.format("%02d", now.getHour()));
        cmbReminderMinute.setValue(String.format("%02d", now.getMinute()));
        
        selectedNote = null;
    }
    
    // Form alanlarını doğrula
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (txtTitle.getText() == null || txtTitle.getText().trim().isEmpty()) {
            errorMessage.append("Lütfen bir başlık girin.\n");
        }
        
        if (cmbCategory.getValue() == null) {
            errorMessage.append("Lütfen bir kategori seçin.\n");
        }
        
        if (dpReminderDate.getValue() != null) {
            LocalDate reminderDate = dpReminderDate.getValue();
            int hour = Integer.parseInt(cmbReminderHour.getValue());
            int minute = Integer.parseInt(cmbReminderMinute.getValue());
            
            LocalDateTime reminderDateTime = LocalDateTime.of(
                reminderDate, 
                LocalTime.of(hour, minute)
            );
            
            if (reminderDateTime.isBefore(LocalDateTime.now())) {
                errorMessage.append("Hatırlatıcı zamanı geçmiş bir zaman olamaz.\n");
            }
        }
        
        if (errorMessage.length() > 0) {
            showStatus(errorMessage.toString(), true);
            return false;
        }
        
        return true;
    }
    
    // Durum mesajı göster
    private void showStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        
        // 5 saniye sonra mesajı temizle
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> lblStatus.setText(""));
            }
        }, 5000);
    }
    
    // Hatırlatıcı zamanlayıcısını başlat
    private void startReminderScheduler() {
        if (reminderScheduler != null && !reminderScheduler.isShutdown()) {
            reminderScheduler.shutdown();
        }
        
        reminderScheduler = Executors.newScheduledThreadPool(1);
        
        // Her dakika çalışacak
        reminderScheduler.scheduleAtFixedRate(() -> {
            // Hatırlatılacak notları kontrol et
            checkReminders();
        }, 0, 1, TimeUnit.MINUTES);
    }
    
    // Hatırlatıcıları kontrol et
    private void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        Optional<List<NotebookDTO>> notesWithReminders = noteBookDao.findNotesWithReminders();
        
        if (notesWithReminders.isPresent()) {
            for (NotebookDTO note : notesWithReminders.get()) {
                if (note.getReminderDateTime() != null) {
                    // Şu anki zamanla hatırlatıcı zamanı arasında en fazla 1 dakika varsa bildirim göster
                    LocalDateTime reminderTime = note.getReminderDateTime();
                    long diffSeconds = Math.abs(reminderTime.toEpochSecond(java.time.ZoneOffset.UTC) - 
                                              now.toEpochSecond(java.time.ZoneOffset.UTC));
                    
                    if (diffSeconds <= 60) {
                        Platform.runLater(() -> showNotificationPopup(note));
                    }
                }
            }
        }
    }
    
    // Bildirim popup'ını göster
    private void showNotificationPopup(NotebookDTO note) {
        try {
            FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("notifications-view.fxml"),
                LanguageManager.getResourceBundle()
            );
            
            Parent root = loader.load();
            
            NotificationController controller = loader.getController();
            controller.setNote(note); // Changed setNoteData to setNote
            
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); 
            popupStage.setTitle("Not Hatırlatıcı");
            popupStage.setResizable(false);
            
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Bildirim gösterilirken bir hata oluştu!", true);
        }
    }
    
    // Controller kapatıldığında çağrılır
    public void shutdown() {
        if (reminderScheduler != null && !reminderScheduler.isShutdown()) {
            reminderScheduler.shutdown();
        }
    }
} 