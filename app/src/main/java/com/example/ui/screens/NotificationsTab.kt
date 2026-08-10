package com.example.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.WeatherInfo
import com.example.data.model.Meeting
import com.example.data.model.NoteItem
import com.example.ui.MainViewModel
import com.example.ui.theme.PriorityBlue
import com.example.ui.theme.PriorityGray
import com.example.ui.theme.PriorityRed
import com.example.ui.theme.PriorityYellow
import com.example.ui.theme.SuccessGreen
import com.example.util.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsTab(
    viewModel: MainViewModel,
    onNavigateToTodoList: () -> Unit,
    onOpenAddMeetingDialog: () -> Unit
) {
    val context = LocalContext.current
    val allMeetings by viewModel.allMeetings.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsState()

    var selectedMeetingForDetails by remember { mutableStateOf<Meeting?>(null) }
    var showWeatherLocationDialog by remember { mutableStateOf(false) }
    var showWeatherApiInfoDialog by remember { mutableStateOf(false) }

    // Date & Greeting
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 5..11 -> "Chào buổi sáng"
        hour in 12..17 -> "Chào buổi chiều"
        else -> "Chào buổi tối"
    }

    val vietnameseDateFormat = SimpleDateFormat("EEEE, 'ngày' dd 'tháng' MM 'năm' yyyy", Locale("vi", "VN"))
    val todayDateStr = vietnameseDateFormat.format(Date()).replaceFirstChar { it.uppercase() }

    // Categorize meetings into 3 sub-frames
    val isoDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayIsoStr = remember { isoDateFormat.format(Date()) }

    fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        if (parts.size >= 2) {
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }
        return 0
    }

    val currentCal = Calendar.getInstance()
    val nowTotalMin = currentCal.get(Calendar.HOUR_OF_DAY) * 60 + currentCal.get(Calendar.MINUTE)

    val ongoingMeetings = remember(allMeetings, todayIsoStr, nowTotalMin) {
        allMeetings.filter { meeting ->
            if (meeting.isCompleted) false
            else {
                val startM = parseTimeToMinutes(meeting.startTime)
                if (meeting.date < todayIsoStr) false
                else if (meeting.date == todayIsoStr) {
                    nowTotalMin >= startM
                } else false
            }
        }
    }

    val upcomingMeetingsCategorized = remember(allMeetings, todayIsoStr, nowTotalMin) {
        allMeetings.filter { meeting ->
            if (meeting.isCompleted) false
            else {
                val startM = parseTimeToMinutes(meeting.startTime)
                if (meeting.date > todayIsoStr) true
                else if (meeting.date == todayIsoStr) {
                    nowTotalMin < startM
                } else false
            }
        }
    }

    val completedMeetingsCategorized = remember(allMeetings, todayIsoStr) {
        allMeetings.filter { meeting ->
            if (meeting.isCompleted) true
            else meeting.date < todayIsoStr
        }
    }

    // Pinned or recent notes
    val pinnedNotes = remember(allNotes) {
        val pinned = allNotes.filter { it.isPinned }
        if (pinned.isNotEmpty()) pinned.take(3) else allNotes.take(3)
    }

    // Permission launcher for notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Đã cấp quyền thông báo nhắc nhở lịch họp!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Automatically schedule reminders (15 minutes prior) for all upcoming meetings
    LaunchedEffect(upcomingMeetingsCategorized) {
        upcomingMeetingsCategorized.forEach { meeting ->
            ReminderScheduler.scheduleMeetingReminder(context, meeting)
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = todayDateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "UBND xã Liên Minh",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Frame
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = todayDateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "UBND xã Liên Minh",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Cán bộ chuyên môn",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Hệ thống thông báo đang hoạt động bình thường", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            BadgedBox(badge = { Badge { Text("${ongoingMeetings.size + upcomingMeetingsCategorized.size}") } }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Thông báo",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đức",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Lịch họp Frame with 3 Sub-Frames
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
                            Column {
                                Text(
                                    text = "Lịch họp gần nhất",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Phân loại chi tiết theo tiến độ cuộc họp",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onOpenAddMeetingDialog) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Thêm lịch họp",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            TextButton(onClick = onNavigateToTodoList) {
                                Text("Xem tất cả", fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. CUỘC HỌP ĐANG TRIỂN KHAI
                    MeetingSubFrame(
                        frameTitle = "Cuộc họp đang triển khai",
                        frameSubtitle = "Cuộc họp đang diễn ra hoặc trong ca làm việc",
                        statusType = FrameStatusType.ONGOING,
                        meetings = ongoingMeetings,
                        emptyMessage = "Hiện tại không có cuộc họp nào đang triển khai",
                        onMeetingClick = { selectedMeetingForDetails = it },
                        onMeetingToggleComplete = { viewModel.toggleMeetingComplete(it) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. CUỘC HỌP CHUẨN BỊ TRIỂN KHAI
                    MeetingSubFrame(
                        frameTitle = "Cuộc họp chuẩn bị triển khai",
                        frameSubtitle = "Các cuộc họp dự kiến tiếp theo",
                        statusType = FrameStatusType.UPCOMING,
                        meetings = upcomingMeetingsCategorized,
                        emptyMessage = "Không có lịch họp nào chuẩn bị triển khai",
                        onMeetingClick = { selectedMeetingForDetails = it },
                        onMeetingToggleComplete = { viewModel.toggleMeetingComplete(it) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. PHIÊN HỌP ĐÃ KẾT THÚC
                    MeetingSubFrame(
                        frameTitle = "Phiên họp đã kết thúc",
                        frameSubtitle = "Các cuộc họp đã hoàn thành nhiệm vụ",
                        statusType = FrameStatusType.COMPLETED,
                        meetings = completedMeetingsCategorized,
                        emptyMessage = "Chưa có cuộc họp nào trong danh sách đã kết thúc",
                        onMeetingClick = { selectedMeetingForDetails = it },
                        onMeetingToggleComplete = { viewModel.toggleMeetingComplete(it) }
                    )
                }
            }
        }

        // Ghi chú Frame
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
                        }

                        TextButton(onClick = onNavigateToTodoList) {
                            Text("Xem tất cả", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (pinnedNotes.isEmpty()) {
                        Text(
                            text = "Chưa có ghi chú nào.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pinnedNotes) { note ->
                                NoteMiniCardItem(note = note, onClick = onNavigateToTodoList)
                            }
                        }
                    }
                }
            }
        }

        // Khung Thời tiết địa phương (đặt ở cuối các khung theo yêu cầu)
        item {
            LocalWeatherCard(
                weatherState = weatherState,
                isLoading = isWeatherLoading,
                onRefresh = { viewModel.fetchWeather(weatherState.locationName) },
                onChangeLocationClick = { showWeatherLocationDialog = true },
                onApiInfoClick = { showWeatherApiInfoDialog = true }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Weather Location Dialog
    if (showWeatherLocationDialog) {
        WeatherLocationDialog(
            currentLocation = weatherState.locationName,
            presetLocations = viewModel.weatherRepository.getPresetLocations(),
            onDismiss = { showWeatherLocationDialog = false },
            onSelectLocation = { loc ->
                viewModel.updateWeatherLocation(loc)
                showWeatherLocationDialog = false
            }
        )
    }

    // Weather API & Library Info Dialog
    if (showWeatherApiInfoDialog) {
        WeatherApiInfoDialog(onDismiss = { showWeatherApiInfoDialog = false })
    }

    // Meeting Details Sheet
    if (selectedMeetingForDetails != null) {
        MeetingDetailsBottomSheet(
            meeting = selectedMeetingForDetails!!,
            onDismiss = { selectedMeetingForDetails = null },
            onCompleteToggle = {
                viewModel.toggleMeetingComplete(it)
                selectedMeetingForDetails = null
            },
            onDelete = {
                viewModel.deleteMeeting(it)
                selectedMeetingForDetails = null
            }
        )
    }
}
}

enum class FrameStatusType {
    ONGOING,
    UPCOMING,
    COMPLETED
}

@Composable
fun MeetingSubFrame(
    frameTitle: String,
    frameSubtitle: String,
    statusType: FrameStatusType,
    meetings: List<Meeting>,
    emptyMessage: String,
    onMeetingClick: (Meeting) -> Unit,
    onMeetingToggleComplete: (Meeting) -> Unit
) {
    val (headerColor, containerBg, borderColor) = when (statusType) {
        FrameStatusType.ONGOING -> Triple(
            PriorityRed,
            PriorityRed.copy(alpha = 0.05f),
            PriorityRed.copy(alpha = 0.35f)
        )
        FrameStatusType.UPCOMING -> Triple(
            PriorityBlue,
            PriorityBlue.copy(alpha = 0.04f),
            PriorityBlue.copy(alpha = 0.3f)
        )
        FrameStatusType.COMPLETED -> Triple(
            SuccessGreen,
            SuccessGreen.copy(alpha = 0.04f),
            SuccessGreen.copy(alpha = 0.3f)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Sub-Frame Header
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
                            .background(headerColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = frameTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = frameSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(headerColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${meetings.size} cuộc họp",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = headerColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (statusType == FrameStatusType.UPCOMING && meetings.isNotEmpty()) {
                val context = LocalContext.current
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PriorityBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, PriorityBlue.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = PriorityBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "🔔 Tự động nhắc nhở trước 15 phút",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Đã lên lịch báo thức & thông báo nhắc cho ${meetings.size} cuộc họp chuẩn bị triển khai",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                meetings.firstOrNull()?.let { firstMeeting ->
                                    ReminderScheduler.scheduleMeetingReminder(context, firstMeeting)
                                    showImmediateTestNotification(context, firstMeeting)
                                    Toast.makeText(context, "🔔 Đã phát thông báo nhắc nhở 15 phút thử nghiệm!", Toast.LENGTH_LONG).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Thử thông báo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (meetings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    meetings.forEach { meeting ->
                        DetailedMeetingCardItem(
                            meeting = meeting,
                            statusType = statusType,
                            accentColor = headerColor,
                            onClick = { onMeetingClick(meeting) },
                            onToggleComplete = { onMeetingToggleComplete(meeting) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedMeetingCardItem(
    meeting: Meeting,
    statusType: FrameStatusType,
    accentColor: Color,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val priorityText = when (meeting.priority) {
        1 -> "🔥 Quan trọng"
        3 -> "⚡ Cần chuẩn bị"
        else -> "📌 Thường"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row of Meeting Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (statusType) {
                                FrameStatusType.ONGOING -> "🔴 Đang triển khai"
                                FrameStatusType.UPCOMING -> "🔵 Chuẩn bị triển khai"
                                FrameStatusType.COMPLETED -> "🟢 Đã kết thúc"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = priorityText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    text = "🔔 Nhắc trước ${meeting.reminderMinutes}p",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Meeting Title
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            // Details List
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Time & Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Thời gian: ${meeting.startTime}${if (meeting.endTime.isNotBlank()) " - ${meeting.endTime}" else ""} • ${meeting.date}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Địa điểm: ${meeting.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Chairperson
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Chủ trì: ${meeting.chairperson}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Attendees
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👥", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Thành phần: ${meeting.attendees}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Preparation if present
                if (meeting.preparation.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "📋", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nội dung: ${meeting.preparation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Xem chi tiết >", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onToggleComplete,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (meeting.isCompleted) MaterialTheme.colorScheme.primary else SuccessGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (meeting.isCompleted) "Khôi phục" else "Hoàn thành",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MeetingCardItem(
    meeting: Meeting,
    onClick: () -> Unit,
    onComplete: () -> Unit
) {
    val statusColor = when {
        meeting.isCompleted -> PriorityGray
        meeting.priority == 1 -> PriorityRed
        meeting.priority == 3 -> PriorityYellow
        else -> PriorityBlue
    }

    val statusText = when {
        meeting.isCompleted -> "Đã hoàn thành"
        meeting.priority == 1 -> "Quan trọng"
        meeting.priority == 3 -> "Cần chuẩn bị"
        else -> "Sắp diễn ra"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (meeting.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = meeting.startTime + (if (meeting.endTime.isNotBlank()) " - ${meeting.endTime}" else ""),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "📍 ${meeting.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Hoàn thành",
                    tint = if (meeting.isCompleted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun NoteMiniCardItem(
    note: NoteItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(note.colorTag)))
                )
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Ghim",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MonthCalendarCompactView(
    allMeetings: List<Meeting>,
    selectedDate: String,
    onDateSelect: (String) -> Unit
) {
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }

    val monthYearFormat = SimpleDateFormat("'Tháng' MM 'năm' yyyy", Locale("vi", "VN"))
    val currentMonthStr = monthYearFormat.format(calendarMonth.time)

    val daysInMonth = remember(calendarMonth) {
        val cal = calendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1: Sun, 2: Mon...
        val leadingEmpty = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

        val list = mutableListOf<String?>()
        repeat(leadingEmpty) { list.add(null) }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (d in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            list.add(dateFormat.format(cal.time))
        }
        list
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newCal = calendarMonth.clone() as Calendar
                newCal.add(Calendar.MONTH, -1)
                calendarMonth = newCal
            }) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
            }

            Text(
                text = currentMonthStr,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                val newCal = calendarMonth.clone() as Calendar
                newCal.add(Calendar.MONTH, 1)
                calendarMonth = newCal
            }) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Tháng sau")
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Weekday Headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val weekDays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
            weekDays.forEach { day ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Days Grid
        val rows = daysInMonth.chunked(7)
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { dateStr ->
                    if (dateStr == null) {
                        Spacer(modifier = Modifier.width(36.dp))
                    } else {
                        val dayNum = dateStr.takeLast(2)
                        val isSelected = dateStr == selectedDate
                        val meetingsOnDay = allMeetings.filter { it.date == dateStr }

                        val dotColor = when {
                            meetingsOnDay.isEmpty() -> null
                            meetingsOnDay.all { it.isCompleted } -> PriorityGray
                            meetingsOnDay.any { it.priority == 1 } -> PriorityRed
                            meetingsOnDay.any { it.priority == 3 } -> PriorityYellow
                            else -> PriorityBlue
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onDateSelect(dateStr) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNum,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                if (dotColor != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else dotColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherLocationDialog(
    currentLocation: String,
    presetLocations: List<String>,
    onDismiss: () -> Unit,
    onSelectLocation: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn địa phương xem thời tiết", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm tỉnh, huyện, xã...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clickable { onSelectLocation("Xã Liên Minh, TP. Hà Nội") }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sử dụng vị trí hiện tại (Xã Liên Minh)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Địa phương phổ biến:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = presetLocations.filter { it.contains(searchQuery, ignoreCase = true) }
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(filtered) { loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLocation(loc) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = loc, fontWeight = if (loc == currentLocation) FontWeight.Bold else FontWeight.Normal)
                            if (loc == currentLocation) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailsBottomSheet(
    meeting: Meeting,
    onDismiss: () -> Unit,
    onCompleteToggle: (Meeting) -> Unit,
    onDelete: (Meeting) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chi tiết cuộc họp",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (meeting.isCompleted) SuccessGreen.copy(alpha = 0.2f) else PriorityBlue.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (meeting.isCompleted) "Đã hoàn thành" else "Chưa diễn ra",
                        fontWeight = FontWeight.Bold,
                        color = if (meeting.isCompleted) SuccessGreen else PriorityBlue,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = meeting.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(label = "🕒 Thời gian", value = "${meeting.date} (${meeting.startTime} - ${meeting.endTime.ifBlank { "kết thúc" }})")
            DetailInfoRow(label = "📍 Địa điểm", value = meeting.location)
            DetailInfoRow(label = "👤 Chủ trì", value = meeting.chairperson)
            DetailInfoRow(label = "👥 Thành phần", value = meeting.attendees)

            if (meeting.preparation.isNotBlank()) {
                DetailInfoRow(label = "📋 Chuẩn bị", value = meeting.preparation)
            }
            if (meeting.documents.isNotBlank()) {
                DetailInfoRow(label = "📁 Tài liệu", value = meeting.documents)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onCompleteToggle(meeting) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (meeting.isCompleted) PriorityGray else SuccessGreen
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (meeting.isCompleted) "Mở lại" else "Đánh dấu hoàn thành")
                }

                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Lịch họp: ${meeting.title}")
                            putExtra(Intent.EXTRA_TEXT, "LỊCH HỌP UBND XÃ LIÊN MINH\n- Cuộc họp: ${meeting.title}\n- Thời gian: ${meeting.startTime} ngày ${meeting.date}\n- Địa điểm: ${meeting.location}\n- Chủ trì: ${meeting.chairperson}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ lịch họp"))
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Chia sẻ")
                }

                OutlinedButton(
                    onClick = { onDelete(meeting) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PriorityRed)
                ) {
                    Text("Xóa")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WeatherApiInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = PriorityYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "API & Thư viện thời tiết",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ứng dụng hiện tích hợp kết nối dữ liệu thời tiết trực tiếp qua Open-Meteo REST API.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Text(
                    text = "🌐 Nguồn API thời tiết đề xuất:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• Open-Meteo REST API: Miễn phí, không cần API Key, cập nhật theo tọa độ GPS.\n• OpenWeatherMap / WeatherAPI: Cung cấp chỉ số UV, chất lượng không khí AQI và cảnh báo dông lốc.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "🛠️ Thư viện Android khuyên dùng:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• Retrofit 2 + OkHttp / Ktor Client: Gọi REST API bất đồng bộ hiệu năng cao.\n• Google Fused Location Services: Tự động lấy vị trí hiện tại của thiết bị.\n• kotlinx.serialization / Moshi: Giải mã JSON siêu tốc độ.\n• Coroutines & StateFlow: Tự động đồng bộ giao diện người dùng Compose.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun LocalWeatherCard(
    weatherState: WeatherInfo,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onChangeLocationClick: () -> Unit,
    onApiInfoClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PriorityYellow.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Thời tiết",
                            tint = PriorityYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Thời tiết tại địa phương",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tích hợp Open-Meteo REST API thời gian thực",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Cập nhật thời tiết",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Weather Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    .padding(16.dp)
            ) {
                Column {
                    // Location Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onChangeLocationClick() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = weatherState.locationName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Thay đổi vị trí",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${weatherState.currentTemp}°",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "C",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            Text(
                                text = weatherState.condition,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🌡️ ${weatherState.maxTemp}°C / ${weatherState.minTemp}°C",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "💧 Độ ẩm: ${weatherState.humidity}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🌧️ Khả năng mưa: ${weatherState.rainProbability}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 3-Day Forecast Section
            if (weatherState.forecast3Days.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Dự báo thời tiết 3 ngày tới:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(weatherState.forecast3Days) { forecast ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.width(120.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = forecast.dayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${forecast.maxTemp}° / ${forecast.minTemp}°C",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = forecast.condition,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Navigation / Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onChangeLocationClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thay đổi xã/thành phố", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onApiInfoClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("Thông tin API & Thư viện", fontSize = 12.sp)
                }
            }
        }
    }
}

fun showImmediateTestNotification(context: Context, meeting: Meeting) {
    val channelId = "vhxh_meeting_reminders"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Lịch họp UBND xã Liên Minh",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thông báo nhắc nhở 15 phút trước giờ diễn ra cuộc họp"
            enableVibration(true)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    val contentIntent = PendingIntent.getActivity(
        context,
        meeting.id,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("⏰ Sắp diễn ra cuộc họp (Nhắc trước 15 phút)")
        .setContentText("${meeting.title} - Bắt đầu: ${meeting.startTime} tại ${meeting.location}")
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText("⚠️ LỊCH HỌP CHUẨN BỊ TRIỂN KHAI (NHẮC NHỞ TRƯỚC 15 PHÚT)\n\n📌 NỘI DUNG HỌP:\n${meeting.title}\n\n⏰ Thời gian: ${meeting.startTime} • Ngày: ${meeting.date}\n📍 Địa điểm: ${meeting.location}\n👤 Chủ trì: ${meeting.chairperson}\n👥 Thành phần: ${meeting.attendees}")
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(longArrayOf(0, 300, 200, 300))
        .setContentIntent(contentIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify((meeting.id + 1000) % 10000 + 1, notification)
}


