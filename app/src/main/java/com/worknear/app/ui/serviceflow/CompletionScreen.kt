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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.StarYellow
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.sansProText

@Composable
fun CompletionScreen(
    onDone: () -> Unit
) {
    val serviceCount = ServiceFlowState.selectedCount.coerceAtLeast(1)
    val serviceCharges = ServiceFlowState.lockAmount
    val platformFee = 35
    val totalPaid = serviceCharges + platformFee
    var rating by remember { mutableIntStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(CardColor).statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Completed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(84.dp).clip(CircleShape).background(SuccessGreen.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(SuccessGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Work Completed!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
            Text("Today, 12:45 PM", fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardColor).border(1.dp, BorderGray, RoundedCornerShape(18.dp)).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Rate Your Experience", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = if (star <= rating) StarYellow else BorderGray,
                            modifier = Modifier.size(34.dp).clip(CircleShape).clickable { rating = star }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    when (rating) { 5 -> "Amazing service!"; 4 -> "Great job!"; 3 -> "It was okay"; else -> "Tell us more" },
                    fontSize = 12.sp,
                    color = MediumGray,
                    fontFamily = sansProText
                )
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardColor).border(1.dp, BorderGray, RoundedCornerShape(18.dp)).padding(18.dp)
            ) {
                Text("Payment Summary", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
                Spacer(Modifier.height(14.dp))
                SummaryRow("Service Charges ($serviceCount)", "₹$serviceCharges")
                Spacer(Modifier.height(10.dp))
                SummaryRow("Platform Fee", "₹$platformFee")
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderGray))
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Total Paid", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText, modifier = Modifier.weight(1f))
                    Text("₹$totalPaid", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, fontFamily = sansProText)
                }
                Spacer(Modifier.height(6.dp))
                Text("Paid via wallet · Auto-released after OTP", fontSize = 11.sp, color = MediumGray, fontFamily = sansProText)
            }

            Spacer(Modifier.height(16.dp))

            ActionRow(R.drawable.ic_wn_invoice, "Download Invoice", onClick = {})
            Spacer(Modifier.height(10.dp))
            ActionRow(R.drawable.ic_wn_cart, "Book Again", onClick = {
                ServiceFlowState.reset()
                onDone()
            })
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
    }
}

@Composable
private fun ActionRow(iconRes: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(iconRes), null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MediumGray, modifier = Modifier.size(20.dp))
    }
}
