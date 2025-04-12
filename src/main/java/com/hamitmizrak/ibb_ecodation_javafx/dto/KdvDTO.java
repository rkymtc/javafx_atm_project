package com.hamitmizrak.ibb_ecodation_javafx.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * 📦 KDV Hesaplama Veri Transfer Objesi (DTO)
 * Tutar, oran, fiş bilgileri ve hesaplama sonuçlarını kapsar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class KdvDTO {

    // 🔢 Veritabanı için benzersiz ID
    private Integer id;

    // 💰 KDV'siz tutar (ana para)
    private Double amount;

    // 📈 Uygulanan KDV oranı (% olarak)
    private Double kdvRate;

    // 🧮 Hesaplanan KDV tutarı (amount * kdvRate / 100)
    private Double kdvAmount;

    // 💳 Genel toplam (amount + kdvAmount)
    private Double totalAmount;

    // 🧾 Fatura veya fiş numarası
    private String receiptNumber;

    // 📅 İşlem tarihi
    private LocalDate transactionDate;

    // 🗒️ İsteğe bağlı açıklama (not, kategori vs.)
    private String description;

    // 📤 Kaydın dışa aktarılma durumu (TXT, PDF, EXCEL gibi)
    private String exportFormat;

    // Explicit getter and setter methods to avoid Lombok processing issues
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getKdvRate() {
        return kdvRate;
    }

    public void setKdvRate(Double kdvRate) {
        this.kdvRate = kdvRate;
    }

    public Double getKdvAmount() {
        return kdvAmount;
    }

    public void setKdvAmount(Double kdvAmount) {
        this.kdvAmount = kdvAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }
    
    // Static builder method to replace Lombok @Builder
    public static KdvDTOBuilder builder() {
        return new KdvDTOBuilder();
    }
    
    // Builder class
    public static class KdvDTOBuilder {
        private Integer id;
        private Double amount;
        private Double kdvRate;
        private Double kdvAmount;
        private Double totalAmount;
        private String receiptNumber;
        private LocalDate transactionDate;
        private String description;
        private String exportFormat;
        
        public KdvDTOBuilder id(Integer id) {
            this.id = id;
            return this;
        }
        
        public KdvDTOBuilder amount(Double amount) {
            this.amount = amount;
            return this;
        }
        
        public KdvDTOBuilder kdvRate(Double kdvRate) {
            this.kdvRate = kdvRate;
            return this;
        }
        
        public KdvDTOBuilder kdvAmount(Double kdvAmount) {
            this.kdvAmount = kdvAmount;
            return this;
        }
        
        public KdvDTOBuilder totalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }
        
        public KdvDTOBuilder receiptNumber(String receiptNumber) {
            this.receiptNumber = receiptNumber;
            return this;
        }
        
        public KdvDTOBuilder transactionDate(LocalDate transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }
        
        public KdvDTOBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public KdvDTOBuilder exportFormat(String exportFormat) {
            this.exportFormat = exportFormat;
            return this;
        }
        
        public KdvDTO build() {
            KdvDTO kdvDTO = new KdvDTO();
            kdvDTO.id = this.id;
            kdvDTO.amount = this.amount;
            kdvDTO.kdvRate = this.kdvRate;
            kdvDTO.kdvAmount = this.kdvAmount;
            kdvDTO.totalAmount = this.totalAmount;
            kdvDTO.receiptNumber = this.receiptNumber;
            kdvDTO.transactionDate = this.transactionDate;
            kdvDTO.description = this.description;
            kdvDTO.exportFormat = this.exportFormat;
            return kdvDTO;
        }
    }

    // ✅ Geçerlilik kontrolü (istersen GUI tarafında validation için kullanılabilir)
    public boolean isValid() {
        return amount != null && kdvRate != null && amount > 0 && kdvRate >= 0 && transactionDate != null;
    }

    // 🔁 Otomatik hesaplama (veritabanına kaydetmeden önce)
    public void calculateTotals() {
        this.kdvAmount = amount * kdvRate / 100;
        this.totalAmount = amount + this.kdvAmount;
    }

    // 📄 Dışa aktarım için sade metin formatı (TXT veya PDF çıktısı için)
    public String toExportString() {
        return String.format("""
                Fiş No     : %s
                Tarih      : %s
                Açıklama   : %s
                Tutar      : %.2f ₺
                KDV Oranı  : %% %.1f
                KDV Tutarı : %.2f ₺
                Genel Toplam: %.2f ₺
                """,
                receiptNumber,
                transactionDate,
                description != null ? description : "-",
                amount,
                kdvRate,
                kdvAmount,
                totalAmount
        );
    }
}
