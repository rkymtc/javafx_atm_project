package com.hamitmizrak.ibb_ecodation_javafx.dto;

import com.hamitmizrak.ibb_ecodation_javafx.utils.ERole;
import lombok.*;

// Lombok
@Getter
@Setter
//@AllArgsConstructor // Parametreli Constructor
@NoArgsConstructor  // Parametresiz Constructor
@ToString
@Builder

// UserDTO
public class UserDTO {
    // Field
    private Integer id;
    private String username;
    private String password;
    private String email;
    private ERole role;


    // Parametresiz Constructor
    // Parametreli Constructor

    public UserDTO(Integer id, String username, String password, String email, ERole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Explicit getter and setter methods to avoid Lombok processing issues
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ERole getRole() {
        return role;
    }

    public void setRole(ERole role) {
        this.role = role;
    }
    
    // Static builder method to replace Lombok @Builder
    public static UserDTOBuilder builder() {
        return new UserDTOBuilder();
    }
    
    // Builder class
    public static class UserDTOBuilder {
        private Integer id;
        private String username;
        private String password;
        private String email;
        private ERole role;
        
        public UserDTOBuilder id(Integer id) {
            this.id = id;
            return this;
        }
        
        public UserDTOBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public UserDTOBuilder password(String password) {
            this.password = password;
            return this;
        }
        
        public UserDTOBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public UserDTOBuilder role(ERole role) {
            this.role = role;
            return this;
        }
        
        public UserDTO build() {
            return new UserDTO(id, username, password, email, role);
        }
    }

    /*
    public static void main(String[] args) {
        UserDTO userDTO= UserDTO.builder()
                .id(0)
                .username("username")
                .email("hamitmizrak@gmail.com")
                .password("root")
                .build();

        System.out.println(userDTO);
    }
    */


} //end Class
