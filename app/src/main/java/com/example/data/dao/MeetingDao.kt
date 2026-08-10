package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Meeting
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY date ASC, startTime ASC")
    fun getAllMeetings(): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE date = :date ORDER BY startTime ASC")
    fun getMeetingsByDate(date: String): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE date = :date ORDER BY startTime ASC")
    suspend fun getMeetingsByDateSync(date: String): List<Meeting>

    @Query("SELECT * FROM meetings WHERE isCompleted = 0 ORDER BY date ASC, startTime ASC")
    fun getUpcomingMeetings(): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE isCompleted = 0 ORDER BY date ASC, startTime ASC")
    suspend fun getUpcomingMeetingsSync(): List<Meeting>

    @Query("SELECT * FROM meetings WHERE id = :id")
    suspend fun getMeetingById(id: Int): Meeting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: Meeting): Long

    @Update
    suspend fun updateMeeting(meeting: Meeting)

    @Delete
    suspend fun deleteMeeting(meeting: Meeting)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: Int)

    @Query("DELETE FROM meetings")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meetings: List<Meeting>)
}
