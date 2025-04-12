package com.hamitmizrak.ibb_ecodation_javafx.dao;

import com.hamitmizrak.ibb_ecodation_javafx.dto.NotebookDTO;

import java.util.List;
import java.util.Optional;

public interface NoteBookDao extends ICrud<NotebookDTO> {
    // Özel methodlar buraya eklenebilir
    Optional<List<NotebookDTO>> findByUserID(Long userId);
    Optional<List<NotebookDTO>> findByCategory(String category);
    Optional<List<NotebookDTO>> findPinnedNotes();
    Optional<List<NotebookDTO>> findNotesWithReminders();
} 