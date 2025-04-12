package com.hamitmizrak.ibb_ecodation_javafx.tutorials.javacore._3_week;

//import lombok.*;

import java.util.Date;

// Lombok annotations replaced with explicit implementations
//@NoArgsConstructor
//@AllArgsConstructor
//@Data
//@Getter
//@Setter
//@ToString
//@Builder
public class Week3_06_Class_LOMBOK {

    // Field
    private Long id;
    private String name;
    private String surname;
    private Date createdDate;
    
    // No-args constructor
    public Week3_06_Class_LOMBOK() {
    }
    
    // All-args constructor
    public Week3_06_Class_LOMBOK(Long id, String name, String surname, Date createdDate) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.createdDate = createdDate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSurname() {
        return surname;
    }
    
    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    // ToString method
    @Override
    public String toString() {
        return "Week3_06_Class_LOMBOK{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
    
    // Builder implementation
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String name;
        private String surname;
        private Date createdDate;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder surname(String surname) {
            this.surname = surname;
            return this;
        }
        
        public Builder createdDate(Date createdDate) {
            this.createdDate = createdDate;
            return this;
        }
        
        public Week3_06_Class_LOMBOK build() {
            return new Week3_06_Class_LOMBOK(id, name, surname, createdDate);
        }
    }

    public static void main(String[] args) {
        Week3_06_Class_LOMBOK lombok= Week3_06_Class_LOMBOK.builder()
                .id(1L)
                .name("Hamit")
                .surname("Surname")
                .createdDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println(lombok);
    }
}
