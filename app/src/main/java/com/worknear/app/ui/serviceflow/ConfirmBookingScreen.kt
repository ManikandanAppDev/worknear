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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.R
import com.worknear.app.data.model.ServiceCatalog
import com.worknear.app.ui.components.PrimaryPillButton
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText

@Composable
fun ConfirmBookingScreen(
    onNavigateBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val pro = ServiceFlowState.selectedPro
    val amount = ServiceFlowState.lockAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        ScheduleTopBar(title = "Schedule Booking", onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Booking Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
            Spacer(Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardColor)
                    .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
                    .padding(4.dp)
            ) {
                SummaryRow(R.drawable.ic_wn_cart, "Service", ServiceCatalog.titleFor(ServiceFlowState.categoryId))
                Divider()
                SummaryRow(R.drawable.ic_wn_professional, "Professional", pro?.name ?: "Your pro")
                Divider()
                SummaryRow(R.drawable.ic_wn_calendar, "Date", ServiceFlowState.scheduledDate ?: "-")
                Divider()
                SummaryRow(R.drawable.ic_wn_clock, "Time", ServiceFlowState.scheduledTime ?: "-")
                Divider()
                SummaryRow(R.drawable.ic_wn_location, "Address", ServiceFlowState.address)
                if (ServiceFlowState.notes.isNotBlank()) {
                    Divider()
                    SummaryRow(R.drawable.ic_wn_note, "Notes", ServiceFlowState.notes)
                }
                Divider()
                SummaryRow(R.drawable.ic_wn_wallet, "Estimated Amount", "₹$amount", highlight = true)
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_wn_lock), null, tint = MediumGray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Amount is locked now and paid after OTP confirmation.",
                    fontSize = 12.sp,
                    color = MediumGray,
                    fontFamily = sansProText
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().background(CardColor).padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .border(1.5.dp, PrimaryBlue, RoundedCornerShape(50))
                        .clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Back", color = PrimaryBlue, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = sansProText)
                }
                PrimaryPillButton(
                    text = "Confirm Booking",
                    modifier = Modifier.weight(1f),
                    onClick = onConfirm
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "You can reschedule before the pro arrives.",
                fontSize = 12.sp,
                color = MediumGray,
                fontFamily = sansProText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SummaryRow(iconRes: Int, label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryBlue.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(iconRes), null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            fontSize = if (highlight) 15.sp else 13.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight) PrimaryBlue else DarkText,
            fontFamily = sansProText,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

@Composable
private fun Divider() {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(1.dp).background(BorderGray.copy(alpha = 0.6f)))
}
