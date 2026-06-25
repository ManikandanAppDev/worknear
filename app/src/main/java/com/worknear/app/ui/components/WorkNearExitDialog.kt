package com.worknear.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

@Composable
fun WorkNearExitDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Intentional dismiss only via buttons/back handler below */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        BackHandler(onBack = onDismiss)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(24.dp),
            color = CardColor,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PrimaryBlue.copy(alpha = 0.18f), PrimaryBlue.copy(alpha = 0.08f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Leave WorkNear?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = sansProText,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Are you sure you want to exit the app?",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MediumGray,
                    fontFamily = sansProText,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                WorkNearButton(
                    text = "Exit app",
                    buttonType = WorkNearButtonType.FILLED,
                    onClick = onConfirm
                )
                Spacer(Modifier.height(10.dp))
                WorkNearButton(
                    text = "Stay",
                    buttonType = WorkNearButtonType.OUTLINED,
                    onClick = onDismiss
                )
            }
        }
    }
}
