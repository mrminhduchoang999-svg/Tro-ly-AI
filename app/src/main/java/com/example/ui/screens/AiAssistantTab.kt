package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.MainViewModel
import com.example.util.TextToSpeechHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val listState = rememberLazyListState()

    // Initialize Text to Speech Helper
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    var inputText by remember { mutableStateOf("") }
    var attachmentName by remember { mutableStateOf<String?>(null) }
    var attachmentContent by remember { mutableStateOf<String?>(null) }
    var showAttachmentConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Trợ lý AI Hành chính",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "UBND xã Liên Minh",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAiChat() }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Xóa trò chuyện")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Official ChatGPT Link Banner
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Sử dụng tài khoản ChatGPT của bạn",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Mở chatgpt.com trong trình duyệt ứng dụng",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Mở ChatGPT", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Prompts Horizontal List
            Text(
                text = "Câu lệnh nhanh:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.aiRepository.getQuickPrompts()) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                inputText = prompt
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = prompt, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Conversation Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatMessages, key = { it.id }) { msg ->
                    ChatMessageBubble(
                        msg = msg,
                        onSpeakText = { textToSpeak ->
                            ttsHelper.speak(textToSpeak)
                            Toast.makeText(context, "Đang đọc câu trả lời AI...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                if (isAiThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Trợ lý AI đang soạn câu trả lời...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Data Warning Notice Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Không nhập tài liệu mật, thông tin cá nhân nhạy cảm hoặc bí mật nhà nước vào hệ thống AI công cộng.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.sp
                    )
                }
            }

            // Attachment badge if present
            if (attachmentName != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Đính kèm: $attachmentName", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = {
                            attachmentName = null
                            attachmentContent = null
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Xóa đính kèm")
                    }
                }
            }

            // Input Bar
            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            attachmentName = "Du_thao_Cong_van_UBND.txt"
                            attachmentContent = "Dự thảo công văn về việc triển khai công tác vệ sinh môi trường và trang trí cờ hoa nhân dịp Quốc khánh 2/9."
                            Toast.makeText(context, "Đã chọn tài liệu đính kèm mẫu", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Đính kèm tài liệu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Soạn nội dung nhờ AI hỗ trợ...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank() || attachmentContent != null) {
                                if (attachmentContent != null) {
                                    showAttachmentConfirmationDialog = true
                                } else {
                                    viewModel.sendAiPrompt(inputText)
                                    inputText = ""
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi")
                    }
                }
            }
        }
    }

    // Attachment Send Confirmation Dialog
    if (showAttachmentConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentConfirmationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xác nhận gửi tài liệu", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Tài liệu \"$attachmentName\" sẽ được gửi tới AI để xử lý. Vui lòng xác nhận rằng tài liệu KHÔNG chứa bí mật nhà nước hoặc dữ liệu cá nhân nhạy cảm.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAttachmentConfirmationDialog = false
                        viewModel.sendAiPrompt(
                            promptText = inputText.ifBlank { "Hãy hỗ trợ xử lý tài liệu đính kèm này." },
                            attachmentText = attachmentContent,
                            attachmentName = attachmentName
                        )
                        inputText = ""
                        attachmentName = null
                        attachmentContent = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đồng ý & Gửi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAttachmentConfirmationDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun ChatMessageBubble(
    msg: ChatMessage,
    onSpeakText: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val isUser = msg.sender == "USER"
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Trợ lý AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onSpeakText(msg.text) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Phát âm thanh",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("AI Text", msg.text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Đã sao chép văn bản", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Sao chép", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (msg.attachmentName != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isUser) Color.White.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📎 ${msg.attachmentName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

