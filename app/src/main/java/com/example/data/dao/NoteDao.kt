package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NoteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedNotes(): Flow<List<NoteItem>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): NoteItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem): Long

    @Update
    suspend fun updateNote(note: NoteItem)

    @Delete
    suspend fun deleteNote(note: NoteItem)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteItem>)
}
