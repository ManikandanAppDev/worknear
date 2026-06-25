package com.worknear.app.ui.serviceflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.sansProText
import kotlinx.coroutines.delay

@Composable
fun BookingSuccessScreen(
    onBackToHome: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1600)
        loading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (loading) ConfirmingState() else SuccessState(onBackToHome)
    }
}

@Composable
private fun ConfirmingState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(PrimaryBlue.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_wn_calendar), null, tint = PrimaryBlue, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text("Confirming your booking...", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))
        Text(
            "Please wait while we confirm the professional and time.",
            fontSize = 13.sp,
            color = MediumGray,
            fontFamily = sansProText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun SuccessState(onBackToHome: () -> Unit) {
    val pro = ServiceFlowState.selectedPro
    val amount = ServiceFlowState.lockAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(SuccessGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Booking Confirmed!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))
        Text(
            "We've sent the details to your email and SMS.",
            fontSize = 13.sp,
            color = MediumGray,
            fontFamily = sansProText,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardColor)
                .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
                .padding(4.dp)
        ) {
            DetailRow(R.drawable.ic_wn_professional, "Professional", pro?.name ?: "Your pro")
            RowDivider()
            DetailRow(R.drawable.ic_wn_cart, "Service", ServiceCatalog.titleFor(ServiceFlowState.categoryId))
            RowDivider()
            DetailRow(R.drawable.ic_wn_calendar, "Date", ServiceFlowState.scheduledDate ?: "-")
            RowDivider()
            DetailRow(R.drawable.ic_wn_clock, "Time", ServiceFlowState.scheduledTime ?: "-")
            RowDivider()
            DetailRow(R.drawable.ic_wn_location, "Address", ServiceFlowState.address)
            RowDivider()
            DetailRow(R.drawable.ic_wn_wallet, "Amount Locked", "₹$amount", highlight = true)
        }

        Spacer(Modifier.height(24.dp))
        PrimaryPillButton(
            text = "Back to Home",
            modifier = Modifier.fillMaxWidth(),
            onClick = onBackToHome
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(iconRes: Int, label: String, value: String, highlight: Boolean = false) {
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
private fun RowDivider() {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(1.dp).background(BorderGray.copy(alpha = 0.6f)))
}
