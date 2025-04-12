package com.hamitmizrak.ibb_ecodation_javafx.controller;

import com.hamitmizrak.ibb_ecodation_javafx.dao.KdvDAO;
import com.hamitmizrak.ibb_ecodation_javafx.dto.KdvDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ThemeManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class KdvController {

    private final KdvDAO kdvDAO = new KdvDAO();

    @FXML private TableView<KdvDTO> kdvTable;
    @FXML private TableColumn<KdvDTO, Integer> idColumn;
    @FXML private TableColumn<KdvDTO, Double> amountColumn;
    @FXML private TableColumn<KdvDTO, Double> kdvRateColumn;
    @FXML private TableColumn<KdvDTO, Double> kdvAmountColumn;
    @FXML private TableColumn<KdvDTO, Double> totalAmountColumn;
    @FXML private TableColumn<KdvDTO, String> receiptColumn;
    @FXML private TableColumn<KdvDTO, LocalDate> dateColumn;
    @FXML private TableColumn<KdvDTO, String> descColumn;
    @FXML private TextField searchField;
    @FXML private Button themeToggleButton;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        kdvRateColumn.setCellValueFactory(new PropertyValueFactory<>("kdvRate"));
        kdvAmountColumn.setCellValueFactory(new PropertyValueFactory<>("kdvAmount"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        receiptColumn.setCellValueFactory(new PropertyValueFactory<>("receiptNumber"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        refreshTable();
        
        // Apply current theme
        if (kdvTable.getScene() != null) {
            ThemeManager.setTheme(kdvTable.getScene(), ThemeManager.isDarkTheme());
            updateThemeButton();
        } else {
            // Scene might not be ready yet, we need to wait
            kdvTable.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    ThemeManager.setTheme(newValue, ThemeManager.isDarkTheme());
                    updateThemeButton();
                }
            });
        }
    }
    
    private void updateThemeButton() {
        if (themeToggleButton != null) {
            themeToggleButton.setText(ThemeManager.isDarkTheme() ? "☀️ Aydınlık Mod" : "🌙 Karanlık Mod");
        }
    }
    
    @FXML
    private void toggleTheme() {
        Scene scene = themeToggleButton.getScene();
        ThemeManager.toggleTheme(scene, themeToggleButton);
    }

    public void refreshTable() {
        Optional<List<KdvDTO>> list = kdvDAO.list();
        list.ifPresent(data -> kdvTable.setItems(FXCollections.observableArrayList(data)));
    }

    private void applyFilter() {
        String keyword = searchField.getText().trim().toLowerCase();
        Optional<List<KdvDTO>> all = kdvDAO.list();
        List<KdvDTO> filtered = all.orElse(List.of()).stream()
                .filter(kdv -> kdv.getReceiptNumber().toLowerCase().contains(keyword))
                .toList();
        kdvTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void clearFilters() {
        searchField.clear();
        refreshTable();
    }

    @FXML
    public void addKdv(ActionEvent event) {
        KdvDTO newKdv = showKdvForm(null);
        if (newKdv != null && newKdv.isValid()) {
            kdvDAO.create(newKdv);
            refreshTable();
            showAlert("Başarılı", "KDV kaydı eklendi.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void updateKdv(ActionEvent event) {
        KdvDTO selected = kdvTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Güncellenecek bir kayıt seçin.", Alert.AlertType.WARNING);
            return;
        }
        KdvDTO updated = showKdvForm(selected);
        if (updated != null && updated.isValid()) {
            kdvDAO.update(selected.getId(), updated);
            refreshTable();
            showAlert("Başarılı", "KDV kaydı güncellendi.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void deleteKdv(ActionEvent event) {
        KdvDTO selected = kdvTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Silinecek bir kayıt seçin.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Silmek istiyor musunuz?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Kayıt: " + selected.getReceiptNumber());
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            kdvDAO.delete(selected.getId());
            refreshTable();
            showAlert("Silindi", "KDV kaydı silindi.", Alert.AlertType.INFORMATION);
        }
    }

    private void showAlert(String titleKey, String messageKey, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getString(titleKey));
        alert.setContentText(com.hamitmizrak.ibb_ecodation_javafx.utils.LanguageManager.getString(messageKey));
        com.hamitmizrak.ibb_ecodation_javafx.utils.ThemeManager.styleDialog(alert);
        alert.showAndWait();
    }

    private KdvDTO showKdvForm(KdvDTO existing) {
        Dialog<KdvDTO> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Yeni KDV Ekle" : "KDV Güncelle");

        TextField amountField = new TextField();
        TextField rateField = new TextField();
        TextField receiptField = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField descField = new TextField();
        ComboBox<String> exportCombo = new ComboBox<>();
        exportCombo.getItems().addAll("TXT", "PDF", "EXCEL");
        exportCombo.setValue("TXT");

        // Add style classes to form elements
        amountField.getStyleClass().add("input-field");
        rateField.getStyleClass().add("input-field");
        receiptField.getStyleClass().add("input-field");
        datePicker.getStyleClass().add("input-field");
        descField.getStyleClass().add("input-field");
        exportCombo.getStyleClass().add("input-field");

        if (existing != null) {
            amountField.setText(String.valueOf(existing.getAmount()));
            rateField.setText(String.valueOf(existing.getKdvRate()));
            receiptField.setText(existing.getReceiptNumber());
            datePicker.setValue(existing.getTransactionDate());
            descField.setText(existing.getDescription());
            exportCombo.setValue(existing.getExportFormat());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Tutar:"), amountField);
        grid.addRow(1, new Label("KDV Oranı (%):"), rateField);
        grid.addRow(2, new Label("Fiş No:"), receiptField);
        grid.addRow(3, new Label("Tarih:"), datePicker);
        grid.addRow(4, new Label("Açıklama:"), descField);
        grid.addRow(5, new Label("Format:"), exportCombo);

        // Apply the current theme to the dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("theme-dialog");
        Scene scene = dialogPane.getScene();
        
        if (scene != null) {
            ThemeManager.setTheme(scene, ThemeManager.isDarkTheme());
        }

        dialogPane.setContent(grid);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Add style classes to buttons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        
        if (okButton != null) okButton.getStyleClass().add("primary-button");
        if (cancelButton != null) cancelButton.getStyleClass().add("secondary-button");

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    return KdvDTO.builder()
                            .amount(Double.parseDouble(amountField.getText()))
                            .kdvRate(Double.parseDouble(rateField.getText()))
                            .receiptNumber(receiptField.getText())
                            .transactionDate(datePicker.getValue())
                            .description(descField.getText())
                            .exportFormat(exportCombo.getValue())
                            .build();
                } catch (Exception e) {
                    showAlert("Hata", "Geçersiz veri girdiniz!", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        Optional<KdvDTO> result = dialog.showAndWait();
        return result.orElse(null);
    }
}
