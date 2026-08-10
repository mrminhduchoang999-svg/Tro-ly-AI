package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

data class IosToastMessage(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector = Icons.Default.CheckCircle,
    val iconTint: Color = SuccessGreen,
    val id: Long = System.currentTimeMillis()
)

@Composable
fun IosToastOverlay(
    toastMessage: IosToastMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = toastMessage != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(250)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            .zIndex(1000f)
    ) {
        if (toastMessage != null) {
            LaunchedEffect(toastMessage.id) {
                delay(2800)
                onDismiss()
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { onDismiss() },
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                shadowElevation = 16.dp,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(toastMessage.iconTint.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = toastMessage.icon,
                            contentDescription = null,
                            tint = toastMessage.iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = toastMessage.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!toastMessage.subtitle.isNullOrBlank()) {
                            Text(
                                text = toastMessage.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
