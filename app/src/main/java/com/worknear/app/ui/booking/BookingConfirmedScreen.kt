package com.worknear.app.ui.booking

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

@Composable
fun BookingConfirmedScreen(
    bookingId: String,
    onChat: (String) -> Unit,
    onTrackBooking: () -> Unit,
    viewModel: BookingConfirmedViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(bookingId) { viewModel.load(bookingId) }

    val booking = uiState.booking
    if (booking == null) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = PrimaryBlue)
            } else {
                Text(
                    uiState.errorMessage ?: "Booking not found",
                    color = MediumGray,
                    textAlign = TextAlign.Center,
                    fontFamily = sansProText,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardColor)
                    .padding(20.dp)
            ) {
                WorkNearButton(
                    text = stringResource(R.string.chat),
                    buttonType = WorkNearButtonType.OUTLINED,
                    modifier = Modifier.weight(1f),
                    onClick = { onChat(booking.professionalId) }
                )
                Spacer(Modifier.width(12.dp))
                WorkNearButton(
                    text = stringResource(R.string.track_booking),
                    buttonType = WorkNearButtonType.FILLED,
                    modifier = Modifier.weight(1f),
                    onClick = onTrackBooking
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, "Confirmed", tint = Color.White, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                stringResource(R.string.booking_confirmed),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.booking_confirmed_message, booking.professionalName, booking.time),
                fontSize = 14.sp,
                color = MediumGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                fontFamily = sansProText
            )

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ConfirmedDetailRow(stringResource(R.string.booking_id), booking.id)
                    Spacer(Modifier.height(12.dp))
                    ConfirmedDetailRow(stringResource(R.string.service_label), booking.serviceName)
                    Spacer(Modifier.height(12.dp))
                    ConfirmedDetailRow(stringResource(R.string.date_label), booking.date)
                    Spacer(Modifier.height(12.dp))
                    ConfirmedDetailRow(stringResource(R.string.time_slot_label), booking.time)
                    Spacer(Modifier.height(12.dp))
                    ConfirmedDetailRow(stringResource(R.string.address_label), booking.address)
                }
            }
        }
    }
}

@Composable
private fun ConfirmedDetailRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 15.sp, color = DarkText, fontWeight = FontWeight.Medium, fontFamily = sansProText)
    }
}
