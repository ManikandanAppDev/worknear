package com.worknear.app.ui.bookings

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.BookingStatus
import com.worknear.app.data.model.CancellationReasonCode
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearAlert
import com.worknear.app.ui.components.WorkNearAlertType
import com.worknear.app.ui.components.WorkNearTopBar
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.ErrorRedLight
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.PromoBackground
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.SuccessGreenLight
import com.worknear.app.ui.theme.WarningAmberLight
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingUuid: String,
    onNavigateBack: () -> Unit,
    onChat: (String) -> Unit,
    viewModel: BookingDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(bookingUuid) { viewModel.load(bookingUuid) }

    Scaffold(
        topBar = {
            WorkNearTopBar(title = "Job status", onBackClick = onNavigateBack)
        },
        containerColor = Background
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            uiState.booking == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage ?: "Booking not found", color = MediumGray, fontFamily = sansProText)
            }
            else -> {
                val booking = uiState.booking!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    if (uiState.successMessage != null) {
                        WorkNearAlert(message = uiState.successMessage, type = WorkNearAlertType.SUCCESS)
                        Spacer(Modifier.height(12.dp))
                    }
                    if (uiState.errorMessage != null && uiState.activeSheet == BookingDetailSheet.NONE) {
                        WorkNearAlert(message = uiState.errorMessage, type = WorkNearAlertType.ERROR)
                        Spacer(Modifier.height(12.dp))
                    }

                    BookingHeaderCard(booking)
                    Spacer(Modifier.height(16.dp))
                    BookingInfoCard(booking)
                    Spacer(Modifier.height(16.dp))
                    JobStatusCard(booking)
                    Spacer(Modifier.height(16.dp))
                    LockedAmountCard(booking)
                    if (booking.completionOtp != null) {
                        Spacer(Modifier.height(16.dp))
                        CompletionOtpCard(booking)
                    }
                    Spacer(Modifier.height(16.dp))

                    if (booking.canReschedule || booking.canCancel) {
                        Text(
                            "Reschedules used: ${booking.rescheduleCount} / ${booking.rescheduleMax}",
                            fontSize = 13.sp,
                            color = MediumGray,
                            fontFamily = sansProText
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    if (booking.canReschedule) {
                        WorkNearButton(
                            text = "Reschedule",
                            buttonType = WorkNearButtonType.OUTLINED,
                            onClick = viewModel::openRescheduleSheet
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    if (booking.canCancel) {
                        WorkNearButton(
                            text = "Cancel booking",
                            buttonType = WorkNearButtonType.DANGER,
                            onClick = viewModel::openCancelSheet,
                            loading = uiState.isSubmitting && uiState.activeSheet == BookingDetailSheet.NONE
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    if (booking.status in listOf(
                            BookingStatus.CONFIRMED,
                            BookingStatus.ON_THE_WAY,
                            BookingStatus.ARRIVED,
                            BookingStatus.IN_PROGRESS,
                            BookingStatus.COMPLETED_PENDING_OTP
                        )
                    ) {
                        WorkNearButton(
                            text = "Chat with professional",
                            buttonType = WorkNearButtonType.FILLED,
                            onClick = { onChat(booking.professionalId) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.activeSheet != BookingDetailSheet.NONE) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissSheet,
            sheetState = sheetState,
            containerColor = CardColor
        ) {
            when (uiState.activeSheet) {
                BookingDetailSheet.CANCEL_REASON -> CancelReasonSheet(uiState, viewModel)
                BookingDetailSheet.CANCEL_CONFIRM -> CancelConfirmSheet(uiState, viewModel)
                BookingDetailSheet.RESCHEDULE -> RescheduleSheet(uiState, viewModel)
                BookingDetailSheet.RESCHEDULE_CONFIRM -> RescheduleConfirmSheet(uiState, viewModel)
                BookingDetailSheet.NONE -> Unit
            }
        }
    }
}

@Composable
private fun BookingHeaderCard(booking: Booking) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PromoBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_wn_professional),
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        statusTitle(booking),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontFamily = sansProText
                    )
                    Text(
                        "${booking.serviceName} • ${booking.professionalName}",
                        fontSize = 13.sp,
                        color = MediumGray,
                        fontFamily = sansProText
                    )
                }
                StatusPill(booking.status)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Background)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_wn_wallet),
                contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Locked ₹${booking.lockedAmount}. Payment auto-transfers only after professional enters your OTP.",
                    fontSize = 12.sp,
                    color = MediumGray,
                    fontFamily = sansProText
                )
            }
        }
    }
}

@Composable
private fun BookingInfoCard(booking: Booking) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            DetailLine("Date", booking.date)
            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
            DetailLine("Time", booking.time)
            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
            DetailLine("Address", booking.address.ifBlank { "—" })
            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
            DetailLine("Estimated amount", "₹${booking.price}")
            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
            DetailLine("Booking ID", booking.id)
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Text(label, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
    Spacer(Modifier.height(2.dp))
    Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = DarkText, fontFamily = sansProText)
}

@Composable
private fun StatusPill(status: BookingStatus) {
    val (bg, fg, label) = when (status) {
        BookingStatus.PENDING -> Triple(PromoBackground, PrimaryBlue, "Assigned")
        BookingStatus.CONFIRMED -> Triple(SuccessGreenLight, SuccessGreen, "Confirmed")
        BookingStatus.ON_THE_WAY -> Triple(PromoBackground, PrimaryBlue, "On the way")
        BookingStatus.ARRIVED -> Triple(PromoBackground, PrimaryBlue, "Arrived")
        BookingStatus.IN_PROGRESS -> Triple(WarningAmberLight, PrimaryBlue, "Work started")
        BookingStatus.COMPLETED_PENDING_OTP -> Triple(WarningAmberLight, PrimaryBlue, "OTP pending")
        BookingStatus.COMPLETED -> Triple(SuccessGreenLight, SuccessGreen, "Completed")
        BookingStatus.CANCELLED -> Triple(ErrorRedLight, ErrorRed, "Cancelled")
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

@Composable
private fun JobStatusCard(booking: Booking) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                statusTitle(booking),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Spacer(Modifier.height(4.dp))
            Text(
                statusSubtitle(booking),
                fontSize = 13.sp,
                color = MediumGray,
                fontFamily = sansProText
            )
            Spacer(Modifier.height(16.dp))
            timelineSteps(booking).forEachIndexed { index, step ->
                TimelineRow(step = step, isLast = index == timelineSteps(booking).lastIndex)
            }
        }
    }
}

private data class TimelineStep(
    val label: String,
    val detail: String,
    val iconRes: Int,
    val done: Boolean,
    val active: Boolean
)

@Composable
private fun TimelineRow(step: TimelineStep, isLast: Boolean) {
    Row(Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        when {
                            step.done -> SuccessGreen
                            step.active -> PrimaryBlue
                            else -> BorderGray
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(step.iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(if (step.done) SuccessGreenLight else BorderGray)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                step.label,
                fontSize = 14.sp,
                fontWeight = if (step.active) FontWeight.Bold else FontWeight.Medium,
                color = if (step.active) DarkText else MediumGray,
                fontFamily = sansProText
            )
            if (step.detail.isNotBlank()) {
                Text(step.detail, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            }
        }
    }
}

@Composable
private fun LockedAmountCard(booking: Booking) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PromoBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_wn_lock),
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    DetailLine("Locked amount", "₹${booking.lockedAmount}")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (booking.status == BookingStatus.COMPLETED) {
                    "Payment was transferred automatically after professional OTP verification."
                } else {
                    "This amount is held by WorkNear. It transfers only after the professional enters your completion OTP, or returns by cancellation rules."
                },
                fontSize = 13.sp,
                color = MediumGray,
                fontFamily = sansProText
            )
        }
    }
}

@Composable
private fun CompletionOtpCard(booking: Booking) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarningAmberLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_wn_otp_shield),
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Your completion OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                booking.completionOtp.orEmpty().forEach { digit ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(digit.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, fontFamily = sansProText)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Share this OTP only after the work is fully completed. The professional enters it in their app and payment transfers automatically.",
                fontSize = 13.sp,
                color = DarkText,
                fontFamily = sansProText
            )
        }
    }
}

private fun statusTitle(booking: Booking): String = when {
    booking.status == BookingStatus.PENDING || booking.status == BookingStatus.CONFIRMED -> {
        if (isScheduledToday(booking)) "Arriving today" else "Booking confirmed"
    }
    booking.status == BookingStatus.ON_THE_WAY -> "Professional on the way"
    booking.status == BookingStatus.ARRIVED -> "Professional arrived"
    booking.status == BookingStatus.IN_PROGRESS -> "Work in progress"
    booking.status == BookingStatus.COMPLETED_PENDING_OTP -> "Confirm completion"
    booking.status == BookingStatus.COMPLETED -> "Service completed"
    booking.status == BookingStatus.CANCELLED -> "Booking cancelled"
    else -> booking.displayStatus.ifBlank { "Booking status" }
}

private fun statusSubtitle(booking: Booking): String = when (booking.status) {
    BookingStatus.COMPLETED_PENDING_OTP -> "Share the OTP only after checking the completed work."
    BookingStatus.COMPLETED -> "Payment was released after OTP verification."
    BookingStatus.CANCELLED -> "The locked amount is released based on cancellation rules."
    else -> "${booking.professionalName} • ${booking.date} • ${booking.time}"
}

private fun timelineSteps(booking: Booking): List<TimelineStep> {
    val status = booking.status
    val progress = statusProgress(status)
    return listOf(
        TimelineStep("Assigned", booking.confirmedAt.orEmpty(), R.drawable.ic_wn_professional, progress >= 1, status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED),
        TimelineStep("On the way", booking.onTheWayAt.orEmpty(), R.drawable.ic_wn_route, progress >= 2, status == BookingStatus.ON_THE_WAY),
        TimelineStep("Arrived", booking.arrivedAt.orEmpty(), R.drawable.ic_wn_arrived, progress >= 3, status == BookingStatus.ARRIVED),
        TimelineStep("Work started", booking.workStartedAt.orEmpty(), R.drawable.ic_wn_tools, progress >= 4, status == BookingStatus.IN_PROGRESS),
        TimelineStep("Completion OTP", booking.workCompletedAt.orEmpty(), R.drawable.ic_wn_otp_shield, progress >= 5, status == BookingStatus.COMPLETED_PENDING_OTP),
        TimelineStep("Completed", booking.completedAt.orEmpty(), R.drawable.ic_wn_receipt, status == BookingStatus.COMPLETED, status == BookingStatus.COMPLETED)
    )
}

private fun statusProgress(status: BookingStatus): Int = when (status) {
    BookingStatus.PENDING, BookingStatus.CONFIRMED -> 1
    BookingStatus.ON_THE_WAY -> 2
    BookingStatus.ARRIVED -> 3
    BookingStatus.IN_PROGRESS -> 4
    BookingStatus.COMPLETED_PENDING_OTP -> 5
    BookingStatus.COMPLETED -> 6
    BookingStatus.CANCELLED -> 0
}

private fun isScheduledToday(booking: Booking): Boolean {
    return runCatching { LocalDate.parse(booking.rawDate) == LocalDate.now() }.getOrDefault(false)
}

@Composable
private fun CancelReasonSheet(state: BookingDetailUiState, viewModel: BookingDetailViewModel) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Cancel booking", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))
        Text("Why are you cancelling?", fontSize = 14.sp, color = MediumGray, fontFamily = sansProText)
        Spacer(Modifier.height(12.dp))
        CancellationReasonCode.entries.forEach { reason ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectCancelReason(reason) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = state.selectedReason == reason,
                    onClick = { viewModel.selectCancelReason(reason) },
                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                )
                Text(reason.label, fontFamily = sansProText, color = DarkText)
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.cancelComment,
            onValueChange = viewModel::onCancelCommentChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Additional comments (optional)", fontFamily = sansProText) },
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderGray)
        )
        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            WorkNearAlert(message = state.errorMessage, type = WorkNearAlertType.ERROR)
        }
        Spacer(Modifier.height(16.dp))
        WorkNearButton(text = "Continue", buttonType = WorkNearButtonType.FILLED, onClick = viewModel::proceedToCancelConfirm)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CancelConfirmSheet(state: BookingDetailUiState, viewModel: BookingDetailViewModel) {
    val preview = state.cancelPreview
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Confirm cancellation", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = sansProText)
        Spacer(Modifier.height(12.dp))
        if (preview != null) {
            WorkNearAlert(
                message = preview.message,
                type = if (preview.feeApplies) WorkNearAlertType.WARNING else WorkNearAlertType.INFO
            )
            if (preview.feeApplies) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cancellation fee: ₹${preview.feeAmount}. The remaining locked amount returns to your wallet.",
                    fontSize = 13.sp,
                    color = MediumGray,
                    fontFamily = sansProText
                )
            }
            if (preview.lateCancel && !preview.feeApplies) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Late cancels used: ${preview.freeLateCancelsUsed} / ${preview.freeLateCancelsLimit} this month",
                    fontSize = 13.sp,
                    color = MediumGray,
                    fontFamily = sansProText
                )
            }
        }
        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            WorkNearAlert(message = state.errorMessage, type = WorkNearAlertType.ERROR)
        }
        Spacer(Modifier.height(16.dp))
        val feeText = if (preview?.feeApplies == true) "Pay ₹${preview.feeAmount} & cancel" else "Cancel booking"
        WorkNearButton(
            text = feeText,
            buttonType = WorkNearButtonType.FILLED,
            loading = state.isSubmitting,
            onClick = viewModel::confirmCancel
        )
        Spacer(Modifier.height(10.dp))
        WorkNearButton(text = "Keep booking", buttonType = WorkNearButtonType.OUTLINED, onClick = viewModel::dismissSheet)
        Spacer(Modifier.height(24.dp))
    }
}

/** Lays out items in fixed-size rows (a stable replacement for FlowRow). */
@Composable
private fun <T> ChipWrap(items: List<T>, perRow: Int, content: @Composable (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(perRow).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { content(it) }
            }
        }
    }
}

@Composable
private fun RescheduleSheet(state: BookingDetailUiState, viewModel: BookingDetailViewModel) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Reschedule booking", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))
        Text("Choose a new date", fontSize = 14.sp, color = MediumGray, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))
        ChipWrap(items = state.availableDates, perRow = 3) { (iso, label) ->
            FilterChip(
                selected = state.selectedDate == iso,
                onClick = { viewModel.selectDate(iso) },
                label = { Text(label, fontFamily = sansProText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("Choose time slot", fontSize = 14.sp, color = MediumGray, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))
        ChipWrap(items = state.availableSlots, perRow = 3) { slot ->
            FilterChip(
                selected = state.selectedSlot == slot,
                onClick = { viewModel.selectSlot(slot) },
                label = { Text(slot.label, fontFamily = sansProText) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            WorkNearAlert(message = state.errorMessage, type = WorkNearAlertType.ERROR)
        }
        Spacer(Modifier.height(16.dp))
        WorkNearButton(text = "Continue", buttonType = WorkNearButtonType.FILLED, onClick = viewModel::proceedToRescheduleConfirm)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RescheduleConfirmSheet(state: BookingDetailUiState, viewModel: BookingDetailViewModel) {
    val booking = state.booking
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Confirm reschedule", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = sansProText)
        Spacer(Modifier.height(12.dp))
        if (booking != null) {
            DetailLine("Current", "${booking.date} • ${booking.time}")
            Spacer(Modifier.height(8.dp))
            val newDateLabel = state.availableDates.find { it.first == state.selectedDate }?.second ?: state.selectedDate.orEmpty()
            DetailLine("New", "$newDateLabel • ${state.selectedSlot?.label.orEmpty()}")
        }
        WorkNearAlert(
            message = "If already confirmed, the professional will need to accept the new slot.",
            type = WorkNearAlertType.INFO
        )
        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            WorkNearAlert(message = state.errorMessage, type = WorkNearAlertType.ERROR)
        }
        Spacer(Modifier.height(16.dp))
        WorkNearButton(
            text = "Confirm reschedule",
            buttonType = WorkNearButtonType.FILLED,
            loading = state.isSubmitting,
            onClick = viewModel::confirmReschedule
        )
        Spacer(Modifier.height(24.dp))
    }
}
