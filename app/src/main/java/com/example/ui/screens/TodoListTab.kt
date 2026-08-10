package com.example.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChecklistEntry
import com.example.data.model.Meeting
import com.example.data.model.NoteItem
import com.example.ui.MainViewModel
import com.example.ui.components.IosToastMessage
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.PriorityBlue
import com.example.ui.theme.PriorityGray
import com.example.ui.theme.PriorityRed
import com.example.ui.theme.PriorityYellow
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListTab(
    viewModel: MainViewModel,
    onNavigateToAiTab: () -> Unit,
    showAddMeetingDialogInitial: Boolean = false,
    onAddMeetingDialogHandled: () -> Unit = {},
    onShowIosToast: (IosToastMessage) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val allMeetings by viewModel.allMeetings.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()

    var activeMeetingFilter by remember { mutableStateOf("Hôm nay") }
    var searchQuery by remember { mutableStateOf("") }

    var showAddMeetingDialog by remember { mutableStateOf(showAddMeetingDialogInitial) }
    var meetingToEdit by remember { mutableStateOf<Meeting?>(null) }
    var selectedMeetingDetails by remember { mutableStateOf<Meeting?>(null) }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteItem?>(null) }
    var selectedNoteForAi by remember { mutableStateOf<NoteItem?>(null) }
    var noteForMeetingSuggestion by remember { mutableStateOf<NoteItem?>(null) }
    var suggestedMeetingResult by remember { mutableStateOf<Meeting?>(null) }
    val isAnalyzingNoteForMeeting by viewModel.isAnalyzingNoteForMeeting.collectAsState()

    if (showAddMeetingDialogInitial) {
        showAddMeetingDialog = true
        onAddMeetingDialogHandled()
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Theo dõi Lịch họp & Ghi chú",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddMeetingDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lịch họp", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showAddNoteDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ghi chú", fontSize = 12.sp)
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm lịch họp, ghi chú...", fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // KKHUNG 1: LỊCH HỌP
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lịch họp & Sự kiện",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${allMeetings.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showAddMeetingDialog = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Thêm lịch họp",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter Chips
                        val filterOptions = listOf("Hôm nay", "Ngày mai", "Tuần này", "Tất cả", "Đã hoàn thành")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filterOptions) { filter ->
                                val isSelected = activeMeetingFilter == filter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { activeMeetingFilter = filter }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = filter,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val filteredMeetings = remember(allMeetings, activeMeetingFilter, searchQuery) {
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                            val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calTomorrow.time)

                            allMeetings.filter { meeting ->
                                val matchesSearch = meeting.title.contains(searchQuery, ignoreCase = true) ||
                                        meeting.location.contains(searchQuery, ignoreCase = true) ||
                                        meeting.chairperson.contains(searchQuery, ignoreCase = true)

                                val matchesFilter = when (activeMeetingFilter) {
                                    "Hôm nay" -> meeting.date == todayStr && !meeting.isCompleted
                                    "Ngày mai" -> meeting.date == tomorrowStr && !meeting.isCompleted
                                    "Tuần này" -> !meeting.isCompleted
                                    "Đã hoàn thành" -> meeting.isCompleted
                                    else -> true
                                }
                                matchesSearch && matchesFilter
                            }
                        }

                        if (filteredMeetings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không có lịch họp nào phù hợp.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                filteredMeetings.forEach { meeting ->
                                    MeetingListItemFull(
                                        meeting = meeting,
                                        onClick = { selectedMeetingDetails = meeting },
                                        onCompleteToggle = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.toggleMeetingComplete(meeting)
                                            val isComp = !meeting.isCompleted
                                            onShowIosToast(
                                                IosToastMessage(
                                                    title = if (isComp) "Đã hoàn thành lịch họp" else "Chuyển sang đang chờ",
                                                    subtitle = meeting.title,
                                                    icon = if (isComp) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                                    iconTint = if (isComp) SuccessGreen else PriorityYellow
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // KHUNG 2: GHI CHÚ CÔNG VIỆC
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ghi chú công việc",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${allNotes.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showAddNoteDialog = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Thêm ghi chú",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val filteredNotes = remember(allNotes, searchQuery) {
                            allNotes.filter {
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.content.contains(searchQuery, ignoreCase = true)
                            }
                        }

                        if (filteredNotes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có ghi chú nào phù hợp.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                filteredNotes.forEach { note ->
                                    NoteCardFull(
                                        note = note,
                                        onClick = { noteToEdit = note },
                                        onTogglePin = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.toggleNotePin(note)
                                            val isPin = !note.isPinned
                                            onShowIosToast(
                                                IosToastMessage(
                                                    title = if (isPin) "Đã ghim ghi chú" else "Đã bỏ ghim ghi chú",
                                                    subtitle = note.title,
                                                    icon = Icons.Default.PushPin,
                                                    iconTint = PriorityBlue
                                                )
                                            )
                                        },
                                        onDelete = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.deleteNote(note)
                                            onShowIosToast(
                                                IosToastMessage(
                                                    title = "Đã xóa ghi chú",
                                                    subtitle = note.title,
                                                    icon = Icons.Default.Delete,
                                                    iconTint = PriorityRed
                                                )
                                            )
                                        },
                                        onProcessAi = { selectedNoteForAi = note },
                                        onSuggestMeeting = {
                                            noteForMeetingSuggestion = note
                                            suggestedMeetingResult = null
                                            viewModel.analyzeNoteForMeetingSuggestion(note) { suggested ->
                                                suggestedMeetingResult = suggested
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Add / Edit Meeting Dialog
    if (showAddMeetingDialog || meetingToEdit != null) {
        AddEditMeetingDialog(
            existingMeeting = meetingToEdit,
            onDismiss = {
                showAddMeetingDialog = false
                meetingToEdit = null
            },
            onSave = { meeting ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (meetingToEdit != null) {
                    viewModel.updateMeeting(meeting)
                    onShowIosToast(
                        IosToastMessage(
                            title = "Đã cập nhật lịch họp",
                            subtitle = meeting.title,
                            icon = Icons.Default.Event,
                            iconTint = IosSystemBlue
                        )
                    )
                } else {
                    viewModel.addMeeting(meeting)
                    onShowIosToast(
                        IosToastMessage(
                            title = "Đã lên lịch họp thành công",
                            subtitle = "${meeting.title} • ${meeting.date}",
                            icon = Icons.Default.EventAvailable,
                            iconTint = SuccessGreen
                        )
                    )
                }
                showAddMeetingDialog = false
                meetingToEdit = null
            }
        )
    }

    // Meeting Details Sheet
    if (selectedMeetingDetails != null) {
        MeetingDetailsBottomSheet(
            meeting = selectedMeetingDetails!!,
            onDismiss = { selectedMeetingDetails = null },
            onCompleteToggle = { meeting ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.toggleMeetingComplete(meeting)
                val isComp = !meeting.isCompleted
                onShowIosToast(
                    IosToastMessage(
                        title = if (isComp) "Đã hoàn thành lịch họp" else "Chuyển sang đang chờ",
                        subtitle = meeting.title,
                        icon = if (isComp) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        iconTint = if (isComp) SuccessGreen else PriorityYellow
                    )
                )
                selectedMeetingDetails = null
            },
            onDelete = { meeting ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.deleteMeeting(meeting)
                onShowIosToast(
                    IosToastMessage(
                        title = "Đã xóa lịch họp",
                        subtitle = meeting.title,
                        icon = Icons.Default.Delete,
                        iconTint = PriorityRed
                    )
                )
                selectedMeetingDetails = null
            }
        )
    }

    // Add / Edit Note Dialog
    if (showAddNoteDialog || noteToEdit != null) {
        AddEditNoteDialog(
            existingNote = noteToEdit,
            onDismiss = {
                showAddNoteDialog = false
                noteToEdit = null
            },
            onSave = { note ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (noteToEdit != null) {
                    viewModel.updateNote(note)
                    onShowIosToast(
                        IosToastMessage(
                            title = "Đã cập nhật ghi chú",
                            subtitle = note.title,
                            icon = Icons.Default.Edit,
                            iconTint = IosSystemBlue
                        )
                    )
                } else {
                    viewModel.addNote(note)
                    onShowIosToast(
                        IosToastMessage(
                            title = "Đã lưu ghi chú thành công",
                            subtitle = note.title,
                            icon = Icons.Default.NoteAdd,
                            iconTint = SuccessGreen
                        )
                    )
                }
                showAddNoteDialog = false
                noteToEdit = null
            }
        )
    }

    // AI Transformer Dialog for Note
    if (selectedNoteForAi != null) {
        AiNoteActionDialog(
            note = selectedNoteForAi!!,
            onDismiss = { selectedNoteForAi = null },
            onSelectAction = { actionType ->
                val targetNote = selectedNoteForAi!!
                selectedNoteForAi = null
                if (actionType == "MEETING_SUGGESTION") {
                    noteForMeetingSuggestion = targetNote
                    suggestedMeetingResult = null
                    viewModel.analyzeNoteForMeetingSuggestion(targetNote) { suggested ->
                        suggestedMeetingResult = suggested
                    }
                } else {
                    viewModel.processNoteWithAi(targetNote, actionType)
                    onNavigateToAiTab()
                }
            }
        )
    }

    // Meeting Suggestion from Note Dialog (with Firestore sync)
    if (noteForMeetingSuggestion != null) {
        MeetingSuggestionFromNoteDialog(
            note = noteForMeetingSuggestion!!,
            isAnalyzing = isAnalyzingNoteForMeeting,
            suggestedMeeting = suggestedMeetingResult,
            onDismiss = {
                noteForMeetingSuggestion = null
                suggestedMeetingResult = null
            },
            onSyncFirestore = { meeting ->
                viewModel.syncMeetingSuggestionToFirestore(noteForMeetingSuggestion!!, meeting) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            },
            onSaveToMeetings = { meeting ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.addMeeting(meeting)
                onShowIosToast(
                    IosToastMessage(
                        title = "Đã tự động lên lịch họp từ ghi chú AI",
                        subtitle = "${meeting.title} • ${meeting.date}",
                        icon = Icons.Default.AutoAwesome,
                        iconTint = SuccessGreen
                    )
                )
                viewModel.syncMeetingSuggestionToFirestore(noteForMeetingSuggestion!!, meeting) { _, _ -> }
                noteForMeetingSuggestion = null
                suggestedMeetingResult = null
            }
        )
    }
}

@Composable
fun PaddingBoxHorizontal(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        content()
    }
}

@Composable
fun MeetingListItemFull(
    meeting: Meeting,
    onClick: () -> Unit,
    onCompleteToggle: () -> Unit
) {
    val context = LocalContext.current
    val statusColor = when {
        meeting.isCompleted -> PriorityGray
        meeting.priority == 1 -> PriorityRed
        meeting.priority == 3 -> PriorityYellow
        else -> PriorityBlue
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${meeting.date} (${meeting.startTime} - ${meeting.endTime.ifBlank { "..." }})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                IconButton(onClick = onCompleteToggle, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (meeting.isCompleted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textDecoration = if (meeting.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = "📍 ${meeting.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "👤 Chủ trì: ${meeting.chairperson}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (meeting.preparation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "📋 Chuẩn bị: ${meeting.preparation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun NoteCardFull(
    note: NoteItem,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onProcessAi: () -> Unit,
    onSuggestMeeting: () -> Unit
) {
    val context = LocalContext.current
    var expandedMenu by remember { mutableStateOf(false) }

    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(note.updatedAt))

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(note.colorTag)))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Ghim",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }

                    Box {
                        IconButton(onClick = { expandedMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Tùy chọn")
                        }
                        DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Gợi ý Lịch họp (Firestore)") },
                                onClick = {
                                    expandedMenu = false
                                    onSuggestMeeting()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = PriorityBlue) }
                            )
                            DropdownMenuItem(
                                text = { Text("Chuyển cho AI xử lý") },
                                onClick = {
                                    expandedMenu = false
                                    onProcessAi()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Sao chép nội dung") },
                                onClick = {
                                    expandedMenu = false
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Note", "${note.title}\n${note.content}")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Đã sao chép vào bộ nhớ tạm", Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Xóa ghi chú") },
                                onClick = {
                                    expandedMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = PriorityRed) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onProcessAi,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Biên tập AI", fontSize = 11.sp, maxLines = 1)
                }

                Button(
                    onClick = onSuggestMeeting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gợi ý Lịch họp", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun AddEditMeetingDialog(
    existingMeeting: Meeting?,
    onDismiss: () -> Unit,
    onSave: (Meeting) -> Unit
) {
    var title by remember { mutableStateOf(existingMeeting?.title ?: "") }
    var date by remember { mutableStateOf(existingMeeting?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var startTime by remember { mutableStateOf(existingMeeting?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(existingMeeting?.endTime ?: "10:00") }
    var location by remember { mutableStateOf(existingMeeting?.location ?: "Phòng họp UBND xã") }
    var chairperson by remember { mutableStateOf(existingMeeting?.chairperson ?: "Lãnh đạo UBND xã") }
    var attendees by remember { mutableStateOf(existingMeeting?.attendees ?: "Thành phần theo Mời") }
    var preparation by remember { mutableStateOf(existingMeeting?.preparation ?: "") }
    var documents by remember { mutableStateOf(existingMeeting?.documents ?: "") }
    var priority by remember { mutableIntStateOf(existingMeeting?.priority ?: 2) }
    var reminderMinutes by remember { mutableIntStateOf(existingMeeting?.reminderMinutes ?: 15) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingMeeting != null) "Chỉnh sửa lịch họp" else "Tạo lịch họp mới", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tiêu đề cuộc họp *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Ngày (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Giờ bắt đầu") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Địa điểm") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = chairperson,
                        onValueChange = { chairperson = it },
                        label = { Text("Người chủ trì") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = preparation,
                        onValueChange = { preparation = it },
                        label = { Text("Nội dung chuẩn bị") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    Text("Thời gian nhắc trước:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val reminderOpts = listOf(5 to "5 phút", 15 to "15 phút", 30 to "30 phút", 60 to "1 giờ", 1440 to "1 ngày")
                        reminderOpts.forEach { (mins, label) ->
                            val isSel = reminderMinutes == mins
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { reminderMinutes = mins }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val m = (existingMeeting ?: Meeting(title = title, date = date, startTime = startTime)).copy(
                        title = title,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        location = location,
                        chairperson = chairperson,
                        attendees = attendees,
                        preparation = preparation,
                        documents = documents,
                        priority = priority,
                        reminderMinutes = reminderMinutes
                    )
                    onSave(m)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu lịch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun AddEditNoteDialog(
    existingNote: NoteItem?,
    onDismiss: () -> Unit,
    onSave: (NoteItem) -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var selectedColor by remember { mutableStateOf(existingNote?.colorTag ?: "#2563EB") }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }

    val colorOptions = listOf("#2563EB", "#6366F1", "#16A34A", "#F59E0B", "#EF4444", "#8B5CF6")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingNote != null) "Sửa ghi chú" else "Ghi chú mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề ghi chú *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Nội dung công việc...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Màu phân loại:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(colorHex)))
                                .border(
                                    width = if (selectedColor == colorHex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val n = (existingNote ?: NoteItem(title = title, content = content)).copy(
                        title = title,
                        content = content,
                        colorTag = selectedColor,
                        isPinned = isPinned
                    )
                    onSave(n)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu ghi chú")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun AiNoteActionDialog(
    note: NoteItem,
    onDismiss: () -> Unit,
    onSelectAction: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Trợ lý AI & Phân tích Ghi chú", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("Chọn tác vụ AI cho ghi chú \"${note.title}\":", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(14.dp))

                AiOptionButton("Gợi ý Lịch họp & Đồng bộ Firestore", "MEETING_SUGGESTION") { onSelectAction("MEETING_SUGGESTION") }
                Spacer(modifier = Modifier.height(8.dp))
                AiOptionButton("Soạn thành thông báo", "NOTICE") { onSelectAction("NOTICE") }
                Spacer(modifier = Modifier.height(8.dp))
                AiOptionButton("Viết thành công văn", "DISPATCH") { onSelectAction("DISPATCH") }
                Spacer(modifier = Modifier.height(8.dp))
                AiOptionButton("Tóm tắt nội dung", "SUMMARY") { onSelectAction("SUMMARY") }
                Spacer(modifier = Modifier.height(8.dp))
                AiOptionButton("Lập danh sách nhiệm vụ", "TASKS") { onSelectAction("TASKS") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
fun MeetingSuggestionFromNoteDialog(
    note: NoteItem,
    isAnalyzing: Boolean,
    suggestedMeeting: Meeting?,
    onDismiss: () -> Unit,
    onSyncFirestore: (Meeting) -> Unit,
    onSaveToMeetings: (Meeting) -> Unit
) {
    var title by remember(suggestedMeeting) { mutableStateOf(suggestedMeeting?.title ?: "Cuộc họp từ ghi chú: ${note.title}") }
    var date by remember(suggestedMeeting) { mutableStateOf(suggestedMeeting?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var startTime by remember(suggestedMeeting) { mutableStateOf(suggestedMeeting?.startTime ?: "08:30") }
    var endTime by remember(suggestedMeeting) { mutableStateOf(suggestedMeeting?.endTime ?: "10:30") }
    var location by remember(suggestedMeeting) { mutableStateOf(suggestedMeeting?.location ?: "Hội trường tầng 2 - UBND xã Liên Minh") }
    var chairperson by remember(suggestedMeeting) { mutableStateOf(suggestedMeeting?.chairperson ?: "Lãnh đạo UBND xã phụ trách") }
    var attendees by remember(suggestedMeeting) { mutableStateOf(suggestedMeeting?.attendees ?: "Cán bộ chuyên môn liên quan") }
    var preparation by remember(suggestedMeeting) { mutableStateOf(suggestedMeeting?.preparation ?: note.content) }

    val currentMeetingState = remember(title, date, startTime, endTime, location, chairperson, attendees, preparation) {
        Meeting(
            title = title,
            date = date,
            startTime = startTime,
            endTime = endTime,
            location = location,
            chairperson = chairperson,
            attendees = attendees,
            preparation = preparation,
            priority = 2,
            reminderMinutes = 30
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gợi ý Lịch họp AI (Firebase Firestore)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            if (isAnalyzing || suggestedMeeting == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Trợ lý AI đang phân tích ghi chú và kết nối Firebase Firestore...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("💡 Ghi chú gốc:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(note.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(note.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Tiêu đề cuộc họp gợi ý *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("Ngày (YYYY-MM-DD)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = "$startTime - $endTime",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Giờ họp") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Địa điểm tổ chức") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = chairperson,
                            onValueChange = { chairperson = it },
                            label = { Text("Chủ trì cuộc họp") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = attendees,
                            onValueChange = { attendees = it },
                            label = { Text("Thành phần tham dự") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = preparation,
                            onValueChange = { preparation = it },
                            label = { Text("Nội dung & Chuẩn bị") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isAnalyzing && suggestedMeeting != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { onSaveToMeetings(currentMeetingState) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tạo Lịch họp Chính thức")
                    }

                    OutlinedButton(
                        onClick = { onSyncFirestore(currentMeetingState) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Đồng bộ Firebase Firestore", fontSize = 12.sp)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun AiOptionButton(label: String, type: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
