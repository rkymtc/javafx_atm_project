package com.hamitmizrak.ibb_ecodation_javafx.dao;

import com.hamitmizrak.ibb_ecodation_javafx.database.SingletonPropertiesDBConnection;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;
import com.hamitmizrak.ibb_ecodation_javafx.utils.ERole;
import org.mindrot.jbcrypt.BCrypt;

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
        // Default Değerler
        this.connection = SingletonPropertiesDBConnection.getInstance().getConnection();
    }

    /// ////////////////////////////////////////////////////////////////////
    // CRUD
    // CREATE
    @Override
    public Optional<UserDTO> create(UserDTO userDTO) {
        // Password Hashing
        String hashedPassword = BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt());

        // SQL
        String sql = "INSERT INTO usertable (username,password,email,role) VALUES(?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userDTO.getUsername());
            preparedStatement.setString(2, userDTO.getPassword());
            preparedStatement.setString(3, userDTO.getEmail());
            preparedStatement.setString(4, userDTO.getRole().name());
            // CREATE, DELETE, UPDATE
            int affectedRows = preparedStatement.executeUpdate();

            // Eğer Ekleme başarılıysa
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userDTO.setId(generatedKeys.getInt(1)); // Otomatik ID set et
                        userDTO.setPassword(hashedPassword);
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
        String sql = "SELECT * FROM usertable";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery(sql);
            // Veritabanından gelen verileri almak
            while (resultSet.next()) {
                userDTOList.add(mapToObjectDTO(resultSet));
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
        String sql = "SELECT * FROM usertable WHERE email=?";
        return selectSingle(sql, name);
    }

    // FIND BY ID
    @Override
    public Optional<UserDTO> findById(int id) {
        String sql = "SELECT * FROM usertable WHERE id=?";
        return selectSingle(sql, id);
    }

    // UPDATE
    @Override
    public Optional<UserDTO> update(int id, UserDTO userDTO) {
        // Öncelikle ID ile nesneyi bul
        Optional<UserDTO> optionalUpdate = findById(id);
        if (optionalUpdate.isPresent()) {
            // Password Hashing
            String hashedPassword = BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt());
            // SQL
            String sql = "UPDATE usertable SET username=?, password=?, email=?0, role=?  WHERE id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userDTO.getUsername());
                preparedStatement.setString(2, userDTO.getPassword());
                preparedStatement.setString(3, userDTO.getEmail());
                preparedStatement.setString(4, userDTO.getRole().name());
                preparedStatement.setInt(5, id);

                // CREATE, DELETE, UPDATE
                int affectedRows = preparedStatement.executeUpdate();

                // Eğer Güncelleme başarılıysa
                if (affectedRows > 0) {
                    userDTO.setId(id); // Güncellenen userDTO için id'yi ekle
                    userDTO.setPassword(hashedPassword);
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
            String sql = "DELETE FROM usertable WHERE id=?";
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

    /// ////////////////////////////////////////////////////////////////
    // GENERICS METOTO (LIST,FIND)
    // ResultSet'ten UserDTO oluşturmayı tek bir yardımcı metot
    // ResultSetten UserDTO oluştur
    @Override
    public UserDTO mapToObjectDTO(ResultSet resultSet) throws SQLException {
        return UserDTO.builder()
                .id(resultSet.getInt("id"))
                .username(resultSet.getString("username"))
                .password(resultSet.getString("password"))
                .email(resultSet.getString("email"))
                .role(ERole.fromString(resultSet.getString("role"))    )
                .build();
    }

    // dizi elemanları(Değişkenler birden fazla olabilir)
    // ID veya NAME ile veri çektiğimizde bu ortak metot kullanılır
    // Generics ile Tek kayıt Döndüren Metot
    @Override
    public Optional<UserDTO> selectSingle(String sql, Object... params) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject((i + 1), params[i]);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapToObjectDTO(resultSet));
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return Optional.empty();
    }

    /// /////////////////////////////////////////////////////////////////////
    /// LOGIN (ILogin interface)
    @Override
    public Optional loginUser(String username, String password) {
        String sql = "SELECT * FROM usertable WHERE username=?, AND password=?";
        Optional<UserDTO> userOptional = selectSingle(sql, username, password);
        if(userOptional.isPresent()){
            UserDTO userDTO= userOptional.get();
            if(BCrypt.checkpw(password, userDTO.getPassword())){
                return Optional.of(userDTO);
            }
        }

        // Eğer veri yoksa null dönder.
        return Optional.empty();
    }

    // Database Username var mı ?
    public boolean isUsernameExists(String username){
        String sql = "SELECT 1 FROM usertable WHERE username=?";
        try(PreparedStatement ps= connection.prepareStatement(sql)){
            ps.setString(1,username);
            ResultSet rs= ps.executeQuery();
            return rs.next(); // kayıt varsa true dönder
        } catch (SQLException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
            return true; // Hata varsa güvenlik için false yerine true döneriz
        }
    }

    // Database Username var mı ?
    public boolean isEmailExists(String email){
        String sql = "SELECT 1 FROM usertable WHERE email=?";
        try(PreparedStatement ps= connection.prepareStatement(sql)){
            ps.setString(1,email);
            ResultSet rs= ps.executeQuery();
            return rs.next(); // kayıt varsa true dönder
        } catch (SQLException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
            return true; // Hata varsa güvenlik için false yerine true döneriz
        }
    }
} //end class
