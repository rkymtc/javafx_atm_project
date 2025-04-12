package com.hamitmizrak.ibb_ecodation_javafx.dao;

import com.hamitmizrak.ibb_ecodation_javafx.database.SingletonPropertiesDBConnection;
import com.hamitmizrak.ibb_ecodation_javafx.dto.NotebookDTO;
import com.hamitmizrak.ibb_ecodation_javafx.dto.UserDTO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteBookDaoImpl implements NoteBookDao {

    private Connection connection;
    private UserDAO userDAO; // User işlemleri için

    // Constructor
    public NoteBookDaoImpl() {
        this.connection = SingletonPropertiesDBConnection.getInstance().getConnection();
        this.userDAO = new UserDAO();
        
        try {
            createNotebookTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Notebook tablosunu oluştur
    private void createNotebookTableIfNotExists() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS notebook (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                content TEXT,
                created_date TIMESTAMP,
                updated_date TIMESTAMP,
                category VARCHAR(100),
                pinned BOOLEAN DEFAULT FALSE,
                reminder_date_time TIMESTAMP,
                user_id INT,
                FOREIGN KEY (user_id) REFERENCES usertable(id)
            );
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Notebook tablosu başarıyla oluşturuldu veya zaten var.");
        }
    }

    @Override
    public Optional<NotebookDTO> create(NotebookDTO notebookDTO) {
        String sql = """
            INSERT INTO notebook 
            (title, content, created_date, updated_date, category, pinned, reminder_date_time, user_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, notebookDTO.getTitle());
            ps.setString(2, notebookDTO.getContent());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(5, notebookDTO.getCategory());
            ps.setBoolean(6, notebookDTO.isPinned());
            
            if (notebookDTO.getReminderDateTime() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(notebookDTO.getReminderDateTime()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            
            if (notebookDTO.getUserDTO() != null) {
                ps.setInt(8, notebookDTO.getUserDTO().getId().intValue());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notebookDTO.setId(generatedKeys.getLong(1));
                        return Optional.of(notebookDTO);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<List<NotebookDTO>> list() {
        List<NotebookDTO> notebookList = new ArrayList<>();
        String sql = "SELECT * FROM notebook ORDER BY pinned DESC, created_date DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                notebookList.add(mapResultSetToNotebookDTO(rs));
            }
            
            return Optional.of(notebookList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<NotebookDTO> findByName(String title) {
        String sql = "SELECT * FROM notebook WHERE title = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNotebookDTO(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<NotebookDTO> findById(int id) {
        String sql = "SELECT * FROM notebook WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNotebookDTO(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<NotebookDTO> update(int id, NotebookDTO notebookDTO) {
        String sql = """
            UPDATE notebook 
            SET title = ?, content = ?, updated_date = ?, category = ?, 
                pinned = ?, reminder_date_time = ? 
            WHERE id = ?
        """;
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, notebookDTO.getTitle());
            ps.setString(2, notebookDTO.getContent());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, notebookDTO.getCategory());
            ps.setBoolean(5, notebookDTO.isPinned());
            
            if (notebookDTO.getReminderDateTime() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(notebookDTO.getReminderDateTime()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            
            ps.setInt(7, id);
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                notebookDTO.setId((long) id);
                return Optional.of(notebookDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<NotebookDTO> delete(int id) {
        // Önce notu bulalım
        Optional<NotebookDTO> noteToDelete = findById(id);
        
        if (noteToDelete.isPresent()) {
            String sql = "DELETE FROM notebook WHERE id = ?";
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                
                int affectedRows = ps.executeUpdate();
                
                if (affectedRows > 0) {
                    return noteToDelete;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<List<NotebookDTO>> findByUserID(Long userId) {
        List<NotebookDTO> userNotes = new ArrayList<>();
        String sql = "SELECT * FROM notebook WHERE user_id = ? ORDER BY pinned DESC, created_date DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userNotes.add(mapResultSetToNotebookDTO(rs));
                }
                
                return Optional.of(userNotes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<List<NotebookDTO>> findByCategory(String category) {
        List<NotebookDTO> categoryNotes = new ArrayList<>();
        String sql = "SELECT * FROM notebook WHERE category = ? ORDER BY pinned DESC, created_date DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categoryNotes.add(mapResultSetToNotebookDTO(rs));
                }
                
                return Optional.of(categoryNotes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<List<NotebookDTO>> findPinnedNotes() {
        List<NotebookDTO> pinnedNotes = new ArrayList<>();
        String sql = "SELECT * FROM notebook WHERE pinned = TRUE ORDER BY created_date DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                pinnedNotes.add(mapResultSetToNotebookDTO(rs));
            }
            
            return Optional.of(pinnedNotes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<List<NotebookDTO>> findNotesWithReminders() {
        List<NotebookDTO> notesWithReminders = new ArrayList<>();
        String sql = "SELECT * FROM notebook WHERE reminder_date_time IS NOT NULL ORDER BY reminder_date_time";
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                notesWithReminders.add(mapResultSetToNotebookDTO(rs));
            }
            
            return Optional.of(notesWithReminders);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    // ResultSet'ten NotebookDTO'ya dönüştürme yardımcı metodu
    private NotebookDTO mapResultSetToNotebookDTO(ResultSet rs) throws SQLException {
        NotebookDTO notebook = new NotebookDTO();
        notebook.setId(rs.getLong("id"));
        notebook.setTitle(rs.getString("title"));
        notebook.setContent(rs.getString("content"));
        
        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            notebook.setCreatedDate(createdDate.toLocalDateTime());
        }
        
        Timestamp updatedDate = rs.getTimestamp("updated_date");
        if (updatedDate != null) {
            notebook.setUpdatedDate(updatedDate.toLocalDateTime());
        }
        
        notebook.setCategory(rs.getString("category"));
        notebook.setPinned(rs.getBoolean("pinned"));
        
        Timestamp reminderDateTime = rs.getTimestamp("reminder_date_time");
        if (reminderDateTime != null) {
            notebook.setReminderDateTime(reminderDateTime.toLocalDateTime());
        }
        
        // Kullanıcı bilgilerini al
        int userId = rs.getInt("user_id");
        if (userId > 0) {
            UserDTO user = userDAO.findById(userId).orElse(null);
            notebook.setUserDTO(user);
        }
        
        return notebook;
    }
} 