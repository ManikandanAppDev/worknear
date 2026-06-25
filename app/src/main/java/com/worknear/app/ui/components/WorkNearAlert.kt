package com.worknear.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.ErrorRedLight
import com.worknear.app.ui.theme.InfoBlueLight
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.SuccessGreenLight
import com.worknear.app.ui.theme.WarningAmber
import com.worknear.app.ui.theme.WarningAmberLight
import com.worknear.app.ui.theme.sansProText

enum class WorkNearAlertType { ERROR, WARNING, SUCCESS, INFO }

private data class AlertStyle(
    val accent: Color,
    val container: Color,
    val icon: ImageVector
)

private fun styleFor(type: WorkNearAlertType): AlertStyle = when (type) {
    WorkNearAlertType.ERROR -> AlertStyle(ErrorRed, ErrorRedLight, Icons.Default.Error)
    WorkNearAlertType.WARNING -> AlertStyle(WarningAmber, WarningAmberLight, Icons.Default.WarningAmber)
    WorkNearAlertType.SUCCESS -> AlertStyle(SuccessGreen, SuccessGreenLight, Icons.Default.CheckCircle)
    WorkNearAlertType.INFO -> AlertStyle(PrimaryBlue, InfoBlueLight, Icons.Default.Info)
}

/**
 * On-brand inline alert. Pass a null [message] to hide (with animation when [animated] is true).
 */
@Composable
fun WorkNearAlert(
    message: String?,
    modifier: Modifier = Modifier,
    type: WorkNearAlertType = WorkNearAlertType.ERROR,
    title: String? = null,
    animated: Boolean = true
) {
    val content: @Composable () -> Unit = {
        WorkNearAlertContent(
            message = message.orEmpty(),
            modifier = modifier,
            type = type,
            title = title
        )
    }

    if (animated) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            content()
        }
    } else if (message != null) {
        content()
    }
}

@Composable
private fun WorkNearAlertContent(
    message: String,
    modifier: Modifier,
    type: WorkNearAlertType,
    title: String?
) {
    val style = styleFor(type)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(style.container)
            .border(1.dp, style.accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    color = DarkText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = sansProText
                )
            }
            Text(
                text = message,
                color = if (title != null) DarkText.copy(alpha = 0.75f) else DarkText,
                fontSize = 13.5.sp,
                fontFamily = sansProText
            )
        }
    }
}
