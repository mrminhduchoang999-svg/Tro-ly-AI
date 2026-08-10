package com.example.data

import android.util.Log
import com.example.data.model.Meeting
import com.example.data.model.NoteItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRepository {

    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "FirebaseFirestore initialization failed", e)
            null
        }
    }

    suspend fun syncNoteToFirestore(note: NoteItem): Boolean = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext false
        try {
            val noteMap = mapOf(
                "id" to note.id,
                "title" to note.title,
                "content" to note.content,
                "colorTag" to note.colorTag,
                "isPinned" to note.isPinned,
                "updatedAt" to note.updatedAt,
                "syncedAt" to System.currentTimeMillis()
            )
            firestore.collection("notes")
                .document(note.id.toString())
                .set(noteMap)
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error syncing note to Firestore: ${e.message}")
            false
        }
    }

    suspend fun saveMeetingSuggestionToFirestore(noteId: Int, meeting: Meeting): Boolean = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext false
        try {
            val suggestionMap = mapOf(
                "noteId" to noteId,
                "title" to meeting.title,
                "date" to meeting.date,
                "startTime" to meeting.startTime,
                "endTime" to meeting.endTime,
                "location" to meeting.location,
                "chairperson" to meeting.chairperson,
                "attendees" to meeting.attendees,
                "preparation" to meeting.preparation,
                "documents" to meeting.documents,
                "priority" to meeting.priority,
                "reminderMinutes" to meeting.reminderMinutes,
                "createdFromAi" to true,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("meeting_suggestions")
                .add(suggestionMap)
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error saving meeting suggestion to Firestore: ${e.message}")
            false
        }
    }

    suspend fun fetchMeetingSuggestionsFromFirestore(): List<Meeting> = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext emptyList()
        try {
            val snapshot = firestore.collection("meeting_suggestions")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                Meeting(
                    title = title,
                    date = doc.getString("date") ?: "",
                    startTime = doc.getString("startTime") ?: "08:00",
                    endTime = doc.getString("endTime") ?: "10:00",
                    location = doc.getString("location") ?: "Phòng họp UBND xã",
                    chairperson = doc.getString("chairperson") ?: "Lãnh đạo UBND xã",
                    attendees = doc.getString("attendees") ?: "Cán bộ chuyên môn",
                    preparation = doc.getString("preparation") ?: "",
                    documents = doc.getString("documents") ?: "",
                    priority = (doc.getLong("priority") ?: 2L).toInt(),
                    reminderMinutes = (doc.getLong("reminderMinutes") ?: 15L).toInt()
                )
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error fetching meeting suggestions: ${e.message}")
            emptyList()
        }
    }
}
