package com.example.data

import android.content.Context
import com.example.data.model.Meeting
import com.example.data.model.NoteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupRestoreManager(
    private val context: Context,
    private val db: AppDatabase,
    private val prefs: UserPreferencesRepository
) {

    suspend fun createBackup(): File = withContext(Dispatchers.IO) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "LienMinhOffice_Backup_$timeStamp.zip"
        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val zipFile = File(backupDir, fileName)

        val meetings = db.meetingDao().getAllMeetings().first()
        val notes = db.noteDao().getAllNotes().first()
        val themeMode = prefs.themeMode.first()
        val weatherLocation = prefs.weatherLocation.first()

        val rootJson = JSONObject().apply {
            put("app", "VHXH Office")
            put("version", "1.0.0")
            put("dbVersion", 1)
            put("timestamp", System.currentTimeMillis())
            put("themeMode", themeMode)
            put("weatherLocation", weatherLocation)

            val meetingsArr = JSONArray()
            meetings.forEach { m ->
                meetingsArr.put(JSONObject().apply {
                    put("id", m.id)
                    put("title", m.title)
                    put("date", m.date)
                    put("startTime", m.startTime)
                    put("endTime", m.endTime)
                    put("location", m.location)
                    put("chairperson", m.chairperson)
                    put("attendees", m.attendees)
                    put("preparation", m.preparation)
                    put("documents", m.documents)
                    put("priority", m.priority)
                    put("reminderMinutes", m.reminderMinutes)
                    put("notes", m.notes)
                    put("isCompleted", m.isCompleted)
                    put("createdAt", m.createdAt)
                })
            }
            put("meetings", meetingsArr)

            val notesArr = JSONArray()
            notes.forEach { n ->
                notesArr.put(JSONObject().apply {
                    put("id", n.id)
                    put("title", n.title)
                    put("content", n.content)
                    put("checklistJson", n.checklistJson)
                    put("colorTag", n.colorTag)
                    put("reminderDate", n.reminderDate)
                    put("isPinned", n.isPinned)
                    put("attachmentsJson", n.attachmentsJson)
                    put("updatedAt", n.updatedAt)
                })
            }
            put("notes", notesArr)
        }

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            val entry = ZipEntry("backup_data.json")
            zos.putNextEntry(entry)
            zos.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        prefs.setLastBackupTime(System.currentTimeMillis())
        return@withContext zipFile
    }

    suspend fun restoreFromZip(zipFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            var jsonContent: String? = null
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "backup_data.json") {
                        jsonContent = zis.bufferedReader(Charsets.UTF_8).readText()
                        break
                    }
                    entry = zis.nextEntry
                }
            }

            if (jsonContent.isNullOrBlank()) return@withContext false

            val rootJson = JSONObject(jsonContent!!)
            val meetingsArr = rootJson.optJSONArray("meetings")
            val notesArr = rootJson.optJSONArray("notes")

            val restoredMeetings = mutableListOf<Meeting>()
            if (meetingsArr != null) {
                for (i in 0 until meetingsArr.length()) {
                    val obj = meetingsArr.getJSONObject(i)
                    restoredMeetings.add(
                        Meeting(
                            title = obj.getString("title"),
                            date = obj.getString("date"),
                            startTime = obj.getString("startTime"),
                            endTime = obj.optString("endTime", ""),
                            location = obj.optString("location", "Phòng họp UBND xã"),
                            chairperson = obj.optString("chairperson", "Lãnh đạo UBND xã"),
                            attendees = obj.optString("attendees", "Thành phần theo Mời"),
                            preparation = obj.optString("preparation", ""),
                            documents = obj.optString("documents", ""),
                            priority = obj.optInt("priority", 2),
                            reminderMinutes = obj.optInt("reminderMinutes", 15),
                            notes = obj.optString("notes", ""),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
            }

            val restoredNotes = mutableListOf<NoteItem>()
            if (notesArr != null) {
                for (i in 0 until notesArr.length()) {
                    val obj = notesArr.getJSONObject(i)
                    restoredNotes.add(
                        NoteItem(
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            checklistJson = obj.optString("checklistJson", "[]"),
                            colorTag = obj.optString("colorTag", "#2563EB"),
                            reminderDate = obj.optString("reminderDate", ""),
                            isPinned = obj.optBoolean("isPinned", false),
                            attachmentsJson = obj.optString("attachmentsJson", "[]")
                        )
                    )
                }
            }

            if (restoredMeetings.isNotEmpty()) {
                db.meetingDao().deleteAll()
                db.meetingDao().insertAll(restoredMeetings)
            }

            if (restoredNotes.isNotEmpty()) {
                db.noteDao().deleteAll()
                db.noteDao().insertAll(restoredNotes)
            }

            if (rootJson.has("themeMode")) {
                prefs.setThemeMode(rootJson.getInt("themeMode"))
            }
            if (rootJson.has("weatherLocation")) {
                prefs.setWeatherLocation(rootJson.getString("weatherLocation"))
            }

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
