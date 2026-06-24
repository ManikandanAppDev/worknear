package com.worknear.app.ui.bookings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
            WorkNearTopBar(title = "Booking Details", onBackClick = onNavigateBack)
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
                            buttonType = WorkNearButtonType.OUTLINED,
                            onClick = viewModel::openCancelSheet,
                            loading = uiState.isSubmitting && uiState.activeSheet == BookingDetailSheet.NONE
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    if (booking.status == BookingStatus.CONFIRMED || booking.status == BookingStatus.ON_THE_WAY) {
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(booking.imageRes),
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(booking.serviceName, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                Text(booking.professionalName, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
            }
            StatusPill(booking.status)
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
            DetailLine("Amount", "₹${booking.price}")
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
        BookingStatus.PENDING -> Triple(PromoBackground, PrimaryBlue, "Pending")
        BookingStatus.CONFIRMED -> Triple(SuccessGreenLight, SuccessGreen, "Confirmed")
        BookingStatus.ON_THE_WAY -> Triple(PromoBackground, PrimaryBlue, "On the way")
        BookingStatus.IN_PROGRESS -> Triple(WarningAmberLight, PrimaryBlue, "In progress")
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
                    "Wallet balance: ₹${preview.walletBalance} → ₹${preview.walletBalance - preview.feeAmount} after fee",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RescheduleSheet(state: BookingDetailUiState, viewModel: BookingDetailViewModel) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Reschedule booking", fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))
        Text("Choose a new date", fontSize = 14.sp, color = MediumGray, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.availableDates.forEach { (iso, label) ->
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
        }
        Spacer(Modifier.height(16.dp))
        Text("Choose time slot", fontSize = 14.sp, color = MediumGray, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.availableSlots.forEach { slot ->
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
