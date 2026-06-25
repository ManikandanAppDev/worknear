package com.worknear.app.ui.bookings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.BookingStatus
import com.worknear.app.data.model.BookingTab
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearScreenTitle
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.SuccessGreenLight
import com.worknear.app.ui.theme.sansProText

@Composable
fun MyBookingsScreen(
    modifier: Modifier = Modifier,
    onBookingClick: (String) -> Unit,
    viewModel: MyBookingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var selectedTab by remember { mutableStateOf(BookingTab.UPCOMING) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedTab) { viewModel.load(selectedTab.name) }

    val bookings = uiState.bookings

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        WorkNearScreenTitle(title = stringResource(R.string.my_bookings))

        LazyRow(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BookingTab.entries.size) { index ->
                val tab = BookingTab.entries[index]
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    label = {
                        Text(
                            when (tab) {
                                BookingTab.UPCOMING -> stringResource(R.string.upcoming)
                                BookingTab.COMPLETED -> stringResource(R.string.completed)
                                BookingTab.CANCELLED -> stringResource(R.string.cancelled)
                            },
                            fontFamily = sansProText,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White,
                        containerColor = CardColor,
                        labelColor = DarkText
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (bookings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        } else {
                            Text(
                                uiState.errorMessage ?: "No bookings here yet",
                                color = MediumGray,
                                textAlign = TextAlign.Center,
                                fontFamily = sansProText
                            )
                        }
                    }
                }
            }
            items(bookings, key = { it.uuid }) { booking ->
                BookingCard(
                    booking = booking,
                    onClick = { onBookingClick(booking.uuid) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingCard(
    booking: Booking,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(booking.imageRes),
                contentDescription = booking.serviceName,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.serviceName, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                Text(booking.professionalName, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
                Text("${booking.date} • ${booking.time}", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
                if (booking.canReschedule || booking.canCancel) {
                    Spacer(Modifier.height(4.dp))
                    Text("Manage ›", fontSize = 13.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium, fontFamily = sansProText)
                }
            }
            StatusPill(booking.status)
        }
    }
}

@Composable
private fun StatusPill(status: BookingStatus) {
    val (bg, fg, label) = when (status) {
        BookingStatus.PENDING -> Triple(com.worknear.app.ui.theme.PromoBackground, PrimaryBlue, "Pending")
        BookingStatus.CONFIRMED -> Triple(SuccessGreenLight, SuccessGreen, "Confirmed")
        BookingStatus.ON_THE_WAY -> Triple(com.worknear.app.ui.theme.PromoBackground, PrimaryBlue, "On the way")
        BookingStatus.ARRIVED -> Triple(com.worknear.app.ui.theme.PromoBackground, PrimaryBlue, "Arrived")
        BookingStatus.IN_PROGRESS -> Triple(com.worknear.app.ui.theme.WarningAmberLight, PrimaryBlue, "In progress")
        BookingStatus.COMPLETED_PENDING_OTP -> Triple(com.worknear.app.ui.theme.WarningAmberLight, PrimaryBlue, "OTP pending")
        BookingStatus.COMPLETED -> Triple(SuccessGreenLight, SuccessGreen, "Completed")
        BookingStatus.CANCELLED -> Triple(Color(0xFFFEE2E2), Color(0xFFEF4444), "Cancelled")
    }
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        fontSize = 11.sp,
        color = fg,
        fontWeight = FontWeight.Medium,
        fontFamily = sansProText
    )
}
