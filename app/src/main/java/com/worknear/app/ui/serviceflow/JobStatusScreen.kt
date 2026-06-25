package com.worknear.app.ui.serviceflow

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.R
import com.worknear.app.ui.components.IconTile
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.WarningAmber
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

private data class TimelineStep(
    val iconRes: Int,
    val title: String,
    val time: String,
    val state: StepState
)

private enum class StepState { DONE, ACTIVE, PENDING }

@Composable
fun JobStatusScreen(
    onNavigateBack: () -> Unit,
    onConfirmCompletion: () -> Unit
) {
    val pro = ServiceFlowState.selectedPro
    val lockAmount = ServiceFlowState.lockAmount

    val steps = listOf(
        TimelineStep(R.drawable.ic_wn_professional, "Assigned", "10:15 AM", StepState.DONE),
        TimelineStep(R.drawable.ic_wn_route, "On the way", "12:05 PM", StepState.ACTIVE),
        TimelineStep(R.drawable.ic_wn_arrived, "Arrived", "Pending", StepState.PENDING),
        TimelineStep(R.drawable.ic_wn_tools, "Work started", "Pending", StepState.PENDING),
        TimelineStep(R.drawable.ic_wn_otp_shield, "Completion OTP", "Pending", StepState.PENDING),
        TimelineStep(R.drawable.ic_wn_receipt, "Completed", "Pending", StepState.PENDING)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(CardColor).statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkText)
            }
            Text("Job status", modifier = Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
            Text("Help", color = PrimaryBlue, fontSize = 14.sp, fontFamily = sansProText)
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            HeroCard(proName = pro?.name ?: "Your pro", rating = pro?.rating ?: 4.8, lockAmount = lockAmount)
            Spacer(Modifier.height(20.dp))
            steps.forEachIndexed { index, step ->
                TimelineRow(step = step, isLast = index == steps.lastIndex)
            }
        }

        Column(modifier = Modifier.fillMaxWidth().background(CardColor).padding(20.dp)) {
            WorkNearButton(text = "Mark service completed", onClick = onConfirmCompletion)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WorkNearButton(
                    text = "Reschedule",
                    buttonType = WorkNearButtonType.OUTLINED,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, ErrorRed.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .clickable {},
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = ErrorRed, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, fontFamily = sansProText)
                }
            }
        }
    }
}

@Composable
private fun HeroCard(proName: String, rating: Double, lockAmount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(PrimaryBlue, PrimaryBlue.copy(alpha = 0.75f))
                )
            )
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(proName.first().toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = sansProText)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Arriving today", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = sansProText)
                Text("$proName · ★ $rating", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, fontFamily = sansProText)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill(text = "12:20 PM - 12:40 PM")
            Spacer(Modifier.width(8.dp))
            Pill(text = "Locked ₹$lockAmount")
        }
    }
}

@Composable
private fun Pill(text: String) {
    Text(
        text,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = sansProText,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun TimelineRow(step: TimelineStep, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val nodeColor = when (step.state) {
                StepState.DONE -> SuccessGreen
                StepState.ACTIVE -> PrimaryBlue
                StepState.PENDING -> BorderGray
            }
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(nodeColor.copy(alpha = if (step.state == StepState.PENDING) 0.4f else 1f)),
                contentAlignment = Alignment.Center
            ) {
                if (step.state == StepState.DONE) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Icon(painterResource(step.iconRes), null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier.width(2.dp).weight(1f).background(BorderGray)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f).padding(top = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    step.title,
                    fontSize = 15.sp,
                    fontWeight = if (step.state == StepState.PENDING) FontWeight.Medium else FontWeight.SemiBold,
                    color = if (step.state == StepState.PENDING) MediumGray else DarkText,
                    fontFamily = sansProText
                )
                if (step.state == StepState.ACTIVE) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Live",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = sansProText,
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(WarningAmber).padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(step.time, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
        }
    }
}
