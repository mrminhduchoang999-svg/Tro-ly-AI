package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val checklistJson: String = "[]", // JSON array of checklist items
    val colorTag: String = "#2563EB", // Hex color string
    val reminderDate: String = "",
    val isPinned: Boolean = false,
    val attachmentsJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis()
)

data class ChecklistEntry(
    val id: String,
    val text: String,
    val isChecked: Boolean = false
)
