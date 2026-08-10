package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AiRepository
import com.example.data.BackupRestoreManager
import com.example.data.ChatMessage
import com.example.data.FirestoreRepository
import com.example.data.UserPreferencesRepository
import com.example.data.WeatherInfo
import com.example.data.WeatherRepository
import com.example.data.model.Meeting
import com.example.data.model.NoteItem
import com.example.util.ReminderScheduler
import com.example.worker.DailySummaryWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val meetingDao = db.meetingDao()
    private val noteDao = db.noteDao()

    val prefsRepository = UserPreferencesRepository(application)
    val weatherRepository = WeatherRepository()
    val aiRepository = AiRepository()
    val firestoreRepository = FirestoreRepository()
    val backupRestoreManager = BackupRestoreManager(application, db, prefsRepository)

    private val _isAnalyzingNoteForMeeting = MutableStateFlow(false)
    val isAnalyzingNoteForMeeting: StateFlow<Boolean> = _isAnalyzingNoteForMeeting.asStateFlow()

    private val _cloudMeetingSuggestions = MutableStateFlow<List<Meeting>>(emptyList())
    val cloudMeetingSuggestions: StateFlow<List<Meeting>> = _cloudMeetingSuggestions.asStateFlow()

    // User Preferences
    val themeMode: StateFlow<Int> = prefsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val weatherLocation: StateFlow<String> = prefsRepository.weatherLocation
        .stateIn(viewModelScope, SharingStarted.Lazily, "Xã Liên Minh, TP. Hà Nội")

    val onboardingCompleted: StateFlow<Boolean> = prefsRepository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val lastBackupTime: StateFlow<Long> = prefsRepository.lastBackupTime
        .stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    // Meetings & Notes
    val allMeetings: StateFlow<List<Meeting>> = meetingDao.getAllMeetings()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allNotes: StateFlow<List<NoteItem>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Weather State
    private val _weatherState = MutableStateFlow(WeatherInfo())
    val weatherState: StateFlow<WeatherInfo> = _weatherState.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    // AI Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                text = "Xin chào Đồng chí! Tôi là Trợ lý AI hành chính UBND xã Liên Minh. Đồng chí cần hỗ trợ soạn thảo công văn, thông báo, tóm tắt văn bản hay công việc nào hôm nay?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Selected Month Date for Calendar
    private val _selectedCalendarDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    init {
        initInitialDataIfEmpty()
        observeWeatherLocation()
        DailySummaryWorker.scheduleDailySummaryWorker(application)
    }

    private fun observeWeatherLocation() {
        viewModelScope.launch {
            weatherLocation.collect { loc ->
                fetchWeather(loc)
            }
        }
    }

    fun fetchWeather(location: String) {
        viewModelScope.launch {
            _isWeatherLoading.value = true
            _weatherState.value = weatherRepository.getWeather(location)
            _isWeatherLoading.value = false
        }
    }

    fun updateWeatherLocation(newLoc: String) {
        viewModelScope.launch {
            prefsRepository.setWeatherLocation(newLoc)
        }
    }

    private fun initInitialDataIfEmpty() {
        viewModelScope.launch {
            val existingMeetings = meetingDao.getAllMeetings().first()
            if (existingMeetings.isEmpty()) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calTomorrow.time)

                val defaultMeetings = listOf(
                    Meeting(
                        title = "Họp Giao ban UBND xã tuần 32",
                        date = todayStr,
                        startTime = "08:00",
                        endTime = "10:30",
                        location = "Hội trường tầng 2 - UBND xã Liên Minh",
                        chairperson = "Đ/c Chủ tịch UBND xã",
                        attendees = "Cán bộ, công chức thuộc UBND xã",
                        preparation = "Báo cáo tiến độ giải ngân và cải cách hành chính",
                        priority = 1,
                        reminderMinutes = 15
                    ),
                    Meeting(
                        title = "Đối thoại về giải phóng mặt bằng đường trục xã",
                        date = todayStr,
                        startTime = "14:00",
                        endTime = "16:30",
                        location = "Nhà văn hóa thôn 3",
                        chairperson = "Đ/c Phó Chủ tịch UBND xã",
                        attendees = "Tổ công tác GPMB và đại diện hộ dân",
                        preparation = "Bản đồ trích đo và phương án bồi thường",
                        priority = 2,
                        reminderMinutes = 30
                    ),
                    Meeting(
                        title = "Kiểm tra công tác cải cách thủ tục hành chính",
                        date = tomorrowStr,
                        startTime = "09:00",
                        endTime = "11:30",
                        location = "Bộ phận Một cửa UBND xã",
                        chairperson = "Đoàn kiểm tra Thành phố",
                        attendees = "Bộ phận Cụm dịch vụ công",
                        priority = 3,
                        reminderMinutes = 60
                    )
                )

                defaultMeetings.forEach { m ->
                    val id = meetingDao.insertMeeting(m).toInt()
                    ReminderScheduler.scheduleMeetingReminder(getApplication(), m.copy(id = id))
                }
                com.example.receiver.MeetingWidgetProvider.updateAppWidget(getApplication())
            }

            val existingNotes = noteDao.getAllNotes().first()
            if (existingNotes.isEmpty()) {
                val defaultNotes = listOf(
                    NoteItem(
                        title = "Rà soát danh sách cấp thẻ BHYT đợt 3",
                        content = "Cần đối chiếu dữ liệu hộ nghèo và các đối tượng chính sách tại các thôn trước ngày 15/08.",
                        colorTag = "#2563EB",
                        isPinned = true
                    ),
                    NoteItem(
                        title = "Nhiệm vụ chuẩn bị Lễ kỷ niệm Quốc khánh 2/9",
                        content = "1. Trang trí khánh tiết tại trụ sở.\n2. Lập danh sách trao quà các gia đình người có công.\n3. Kiểm tra hệ thống đài phát thanh.",
                        colorTag = "#16A34A",
                        isPinned = false
                    )
                )
                defaultNotes.forEach { noteDao.insertNote(it) }
            }
        }
    }

    // Meeting Operations
    fun addMeeting(meeting: Meeting) {
        viewModelScope.launch {
            val id = meetingDao.insertMeeting(meeting).toInt()
            ReminderScheduler.scheduleMeetingReminder(getApplication(), meeting.copy(id = id))
            com.example.receiver.MeetingWidgetProvider.updateAppWidget(getApplication())
        }
    }

    fun updateMeeting(meeting: Meeting) {
        viewModelScope.launch {
            meetingDao.updateMeeting(meeting)
            if (meeting.isCompleted) {
                ReminderScheduler.cancelMeetingReminder(getApplication(), meeting.id)
            } else {
                ReminderScheduler.scheduleMeetingReminder(getApplication(), meeting)
            }
            com.example.receiver.MeetingWidgetProvider.updateAppWidget(getApplication())
        }
    }

    fun deleteMeeting(meeting: Meeting) {
        viewModelScope.launch {
            meetingDao.deleteMeeting(meeting)
            ReminderScheduler.cancelMeetingReminder(getApplication(), meeting.id)
            com.example.receiver.MeetingWidgetProvider.updateAppWidget(getApplication())
        }
    }

    fun toggleMeetingComplete(meeting: Meeting) {
        val updated = meeting.copy(isCompleted = !meeting.isCompleted)
        updateMeeting(updated)
    }

    // Note Operations
    fun addNote(note: NoteItem) {
        viewModelScope.launch {
            noteDao.insertNote(note)
        }
    }

    fun updateNote(note: NoteItem) {
        viewModelScope.launch {
            noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    fun toggleNotePin(note: NoteItem) {
        updateNote(note.copy(isPinned = !note.isPinned))
    }

    // AI Chat
    fun sendAiPrompt(promptText: String, attachmentText: String? = null, attachmentName: String? = null) {
        if (promptText.isBlank() && attachmentText.isNullOrBlank()) return

        val userMsg = ChatMessage(
            sender = "USER",
            text = promptText,
            attachmentName = attachmentName
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAiThinking.value = true
            val responseText = aiRepository.sendMessage(
                prompt = promptText,
                attachmentText = attachmentText,
                history = _chatMessages.value
            )
            val aiMsg = ChatMessage(
                sender = "AI",
                text = responseText
            )
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiThinking.value = false
        }
    }

    fun clearAiChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = "AI",
                text = "Xin chào Đồng chí! Tôi là Trợ lý AI hành chính UBND xã Liên Minh. Cuộc hội thoại đã được xóa sạch."
            )
        )
    }

    // Note to AI Action
    fun processNoteWithAi(note: NoteItem, actionType: String) {
        val prompt = when (actionType) {
            "NOTICE" -> "Hãy soạn thành thông báo chính thức dựa trên ghi chú sau:\nTiêu đề: ${note.title}\nNội dung: ${note.content}"
            "DISPATCH" -> "Hãy soạn thành công văn chỉ đạo hành chính dựa trên ghi chú sau:\nTiêu đề: ${note.title}\nNội dung: ${note.content}"
            "SUMMARY" -> "Hãy tóm tắt ngắn gọn các ý chính của ghi chú sau:\nTiêu đề: ${note.title}\nNội dung: ${note.content}"
            "TASKS" -> "Hãy lập danh sách các nhiệm vụ cụ thể và phân công thời hạn dựa trên ghi chú sau:\nTiêu đề: ${note.title}\nNội dung: ${note.content}"
            else -> "Xử lý ghi chú: ${note.title}"
        }
        sendAiPrompt(promptText = prompt)
    }

    fun analyzeNoteForMeetingSuggestion(note: NoteItem, onComplete: (Meeting) -> Unit) {
        viewModelScope.launch {
            _isAnalyzingNoteForMeeting.value = true
            val suggested = aiRepository.generateMeetingSuggestionFromNote(note)
            _isAnalyzingNoteForMeeting.value = false
            onComplete(suggested)
        }
    }

    fun syncMeetingSuggestionToFirestore(note: NoteItem, meeting: Meeting, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val noteSynced = firestoreRepository.syncNoteToFirestore(note)
            val meetingSaved = firestoreRepository.saveMeetingSuggestionToFirestore(note.id, meeting)
            if (noteSynced || meetingSaved) {
                onResult(true, "Đã đồng bộ ghi chú & gợi ý lịch họp lên Firebase Firestore!")
            } else {
                onResult(false, "Đã lưu bản ghi tạm thời. Vui lòng kiểm tra kết nối mạng/Firebase Firestore.")
            }
        }
    }

    fun fetchCloudMeetingSuggestions() {
        viewModelScope.launch {
            val suggestions = firestoreRepository.fetchMeetingSuggestionsFromFirestore()
            _cloudMeetingSuggestions.value = suggestions
        }
    }

    // Calendar
    fun selectCalendarDate(dateStr: String) {
        _selectedCalendarDate.value = dateStr
    }

    // Preferences & Settings
    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            prefsRepository.setThemeMode(mode)
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            prefsRepository.setOnboardingCompleted(completed)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setNotificationsEnabled(enabled)
        }
    }

    // Backup & Restore
    fun performBackup(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val file = backupRestoreManager.createBackup()
                onResult(true, file.absolutePath)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Lỗi sao lưu")
            }
        }
    }

    fun performRestore(file: File, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupRestoreManager.restoreFromZip(file)
            onResult(success)
        }
    }
}
