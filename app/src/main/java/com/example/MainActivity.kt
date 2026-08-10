package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import com.example.ui.theme.PriorityRed
import com.example.ui.components.IosToastMessage
import com.example.ui.components.IosToastOverlay
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.screens.AiAssistantTab
import com.example.ui.screens.NotificationsTab
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsGuideTab
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TodoListTab
import com.example.ui.theme.VhxhTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val useDarkTheme = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            VhxhTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainEntry(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppMainEntry(viewModel: MainViewModel) {
    val context = LocalContext.current
    var isSplashFinished by remember { mutableStateOf(false) }
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    // Runtime Permission Launcher for Android 13/14/15 standards
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results gracefully if needed
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    if (!isSplashFinished) {
        SplashScreen(onSplashFinished = { isSplashFinished = true })
    } else if (!onboardingCompleted) {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = { viewModel.setOnboardingCompleted(true) }
        )
    } else {
        MainAppScaffold(viewModel = viewModel)
    }
}

enum class NavigationTab(val label: String, val icon: ImageVector) {
    NOTIFICATIONS("Thông báo", Icons.Default.Notifications),
    TODO_LIST("Việc cần làm", Icons.Default.Checklist),
    AI_ASSISTANT("Ứng dụng AI", Icons.Default.Psychology),
    SETTINGS_GUIDE("Hướng dẫn", Icons.Default.Settings)
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.NOTIFICATIONS) }
    var openAddMeetingDialogInitial by remember { mutableStateOf(false) }
    var iosToastMessage by remember { mutableStateOf<IosToastMessage?>(null) }

    val allMeetings by viewModel.allMeetings.collectAsState()
    val upcomingCount = remember(allMeetings) { allMeetings.count { !it.isCompleted } }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                shadowElevation = 10.dp,
                tonalElevation = 2.dp
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            alwaysShowLabel = true,
                            icon = {
                                if (tab == NavigationTab.NOTIFICATIONS && upcomingCount > 0) {
                                    BadgedBox(badge = { Badge(containerColor = PriorityRed) { Text("$upcomingCount") } }) {
                                        Icon(imageVector = tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(imageVector = tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            IosToastOverlay(
                toastMessage = iosToastMessage,
                onDismiss = { iosToastMessage = null }
            )

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val targetIndex = targetState.ordinal
                    val initialIndex = initialState.ordinal
                    if (targetIndex > initialIndex) {
                        (slideInHorizontally { width -> width / 4 } + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally { width -> -width / 4 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally { width -> -width / 4 } + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally { width -> width / 4 } + fadeOut(tween(200)))
                    }
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    NavigationTab.NOTIFICATIONS -> {
                        NotificationsTab(
                            viewModel = viewModel,
                            onNavigateToTodoList = { selectedTab = NavigationTab.TODO_LIST },
                            onOpenAddMeetingDialog = {
                                openAddMeetingDialogInitial = true
                                selectedTab = NavigationTab.TODO_LIST
                            }
                        )
                    }

                    NavigationTab.TODO_LIST -> {
                        TodoListTab(
                            viewModel = viewModel,
                            onNavigateToAiTab = { selectedTab = NavigationTab.AI_ASSISTANT },
                            showAddMeetingDialogInitial = openAddMeetingDialogInitial,
                            onAddMeetingDialogHandled = { openAddMeetingDialogInitial = false },
                            onShowIosToast = { iosToastMessage = it }
                        )
                    }

                    NavigationTab.AI_ASSISTANT -> {
                        AiAssistantTab(viewModel = viewModel)
                    }

                    NavigationTab.SETTINGS_GUIDE -> {
                        SettingsGuideTab(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
