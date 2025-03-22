package com.hamitmizrak.ibb_ecodation_javafx.dao;

import com.hamitmizrak.ibb_ecodation_javafx.database.SingletonDBConnection;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.SpecialColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// UserDAO
public class UserDAO implements IDaoImplements<UserDTO> {

    // Injection
    private Connection connection;

    // Parametresiz Constructor
    public UserDAO() {
        this.connection = SingletonDBConnection.getInstance().getConnection();
    }

    /// ////////////////////////////////////////////////////////////////////
    // CRUD
    // CREATE
    @Override
    public Optional<UserDTO> create(UserDTO userDTO) {
        String sql = "INSERT INTO users (username,password,email) VALUES(?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userDTO.getUsername());
            preparedStatement.setString(2, userDTO.getPassword());
            preparedStatement.setString(3, userDTO.getEmail());
            // CREATE, DELETE, UPDATE
            int affectedRows = preparedStatement.executeUpdate();

            // Eğer Ekleme başarılıysa
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userDTO.setId(generatedKeys.getInt(1)); // OTomatik ID set et
                        return Optional.of(userDTO);
                    }
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // Eğer Ekleme başarısızsa boş veri dönder
        return Optional.empty();
    }

    // LIST
    @Override
    public Optional<List<UserDTO>> list() {
        List<UserDTO> userDTOList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery(sql);

            // Veritabanından gelen verileri almak
            while (resultSet.next()) {
                userDTOList.add(UserDTO.builder()
                        .id(resultSet.getInt("id"))
                        .username(resultSet.getString("username"))
                        .password(resultSet.getString("password"))
                        .email(resultSet.getString("email"))
                        .build());
            }
            return userDTOList.isEmpty() ? Optional.empty() : Optional.of(userDTOList);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // Eğer Listeleme başarısızsa boş veri dönder
        return Optional.empty();
    }

    // FIND BY NAME
    @Override
    public Optional<UserDTO> findByName(String name) {
        //String sql = "SELECT * FROM users WHERE username=?";
        String sql = "SELECT * FROM users WHERE email=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery(sql);
            // Veritabanından gelen veri varsa
            if (resultSet.next()) {
                UserDTO userDTO = UserDTO.builder()
                        .id(resultSet.getInt("id"))
                        .username(resultSet.getString("username"))
                        .email(resultSet.getString("email"))
                        .password(resultSet.getString("password"))
                        .build();
                return Optional.of(userDTO);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // Eğer Bulunamazsa boş dönder
        return Optional.empty();
    }

    // FIND BY ID
    @Override
    public Optional<UserDTO> findById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery(sql);
            // Veritabanından gelen veri varsa
            if (resultSet.next()) {
                UserDTO userDTO = UserDTO.builder()
                        .id(resultSet.getInt("id"))
                        .username(resultSet.getString("username"))
                        .email(resultSet.getString("email"))
                        .password(resultSet.getString("password"))
                        .build();
                return Optional.of(userDTO);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // Eğer Bulunamazsa boş dönder
        System.out.println(SpecialColor.GREEN + " Aradaığınız " + id + " id bulunamadı.");
        return Optional.empty();
    }

    // UPDATE
    @Override
    public Optional<UserDTO> update(int id, UserDTO userDTO) {
        Optional<UserDTO> optionalUpdate = findById(id);
        if (optionalUpdate.isPresent()) {
            String sql = "UPDATE users SET username=?, password=?, email=?  WHERE id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userDTO.getUsername());
                preparedStatement.setString(2, userDTO.getPassword());
                preparedStatement.setString(3, userDTO.getEmail());
                preparedStatement.setInt(4, userDTO.getId());

                // CREATE, DELETE, UPDATE
                int affectedRows = preparedStatement.executeUpdate();

                // Eğer Güncelleme başarılıysa
                if (affectedRows > 0) {
                    userDTO.setId(id); // Güncellenen userDTO için id'yi ekle
                    return Optional.of(userDTO);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        // Eğer Güncellenecek id veri yoksa boş dönder.
        return Optional.empty();
    }

    // DELETE
    @Override
    public Optional<UserDTO> delete(int id) {
        Optional<UserDTO> optionalDelete = findById(id);
        if (optionalDelete.isPresent()) {
            String sql = "DELETE FROM users WHERE id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, id);

                // CREATE, DELETE, UPDATE
                int affectedRows = preparedStatement.executeUpdate();

                // Eğer Güncelleme başarılıysa
                if (affectedRows > 0) {
                    return optionalDelete;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        // Eğer Silinecek id veri yoksa boş dönder.
        return Optional.empty();
    }

} //end class
