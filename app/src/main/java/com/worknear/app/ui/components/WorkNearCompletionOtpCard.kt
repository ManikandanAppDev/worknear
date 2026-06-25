package com.worknear.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.WarningAmber
import com.worknear.app.ui.theme.WarningAmberLight
import com.worknear.app.ui.theme.sansProText

@Composable
fun WorkNearCompletionOtpCard(
    otp: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    subtitle: String = "Share this code with your professional only after the work is fully done."
) {
    val digits = otp.filter { it.isDigit() }
    if (digits.isEmpty()) return

    val corner = if (compact) 18.dp else 22.dp
    val digitSize = if (compact) 36.dp else 44.dp
    val digitFont = if (compact) 18.sp else 22.sp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            WarningAmberLight,
                            PrimaryBlue.copy(alpha = 0.08f),
                            CardColor
                        )
                    )
                )
                .border(1.dp, WarningAmber.copy(alpha = 0.35f), RoundedCornerShape(corner))
                .padding(if (compact) 14.dp else 18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(if (compact) 34.dp else 40.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_wn_otp_shield),
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(if (compact) 18.dp else 22.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Your completion OTP",
                            fontSize = if (compact) 14.sp else 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            fontFamily = sansProText
                        )
                        Text(
                            text = "Work marked complete — verify to release payment",
                            fontSize = if (compact) 11.sp else 12.sp,
                            color = MediumGray,
                            fontFamily = sansProText
                        )
                    }
                }
                Spacer(Modifier.height(if (compact) 12.dp else 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
                ) {
                    digits.forEach { digit ->
                        Box(
                            modifier = Modifier
                                .size(digitSize)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.95f))
                                .border(1.dp, PrimaryBlue.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = digit.toString(),
                                fontSize = digitFont,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                fontFamily = sansProText
                            )
                        }
                    }
                }
                if (!compact) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = DarkText,
                        fontFamily = sansProText
                    )
                }
            }
        }
    }
}
