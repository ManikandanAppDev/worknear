package com.worknear.app.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.CancelPreview
import com.worknear.app.data.model.CancellationReasonCode
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.BookingRepository
import com.worknear.app.data.repository.CustomerWorkspaceStore
import com.worknear.app.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class BookingDetailSheet {
    NONE, CANCEL_REASON, CANCEL_CONFIRM, RESCHEDULE, RESCHEDULE_CONFIRM
}

data class RescheduleSlot(val start: String, val end: String, val label: String)

data class BookingDetailUiState(
    val isLoading: Boolean = true,
    val booking: Booking? = null,
    val errorMessage: String? = null,
    val activeSheet: BookingDetailSheet = BookingDetailSheet.NONE,
    val cancelPreview: CancelPreview? = null,
    val selectedReason: CancellationReasonCode? = null,
    val cancelComment: String = "",
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val selectedDate: String? = null,
    val selectedSlot: RescheduleSlot? = null,
    val availableDates: List<Pair<String, String>> = emptyList(),
    val availableSlots: List<RescheduleSlot> = defaultSlots
)

private val defaultSlots = listOf(
    RescheduleSlot("09:00", "11:00", "9 AM – 11 AM"),
    RescheduleSlot("11:00", "13:00", "11 AM – 1 PM"),
    RescheduleSlot("14:00", "16:00", "2 PM – 4 PM"),
    RescheduleSlot("16:00", "18:00", "4 PM – 6 PM")
)

class BookingDetailViewModel(
    private val bookingRepository: BookingRepository,
    private val walletRepository: WalletRepository,
    private val customerWorkspaceStore: CustomerWorkspaceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingDetailUiState())
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private var bookingUuid: String? = null
    private val displayDateFormatter = DateTimeFormatter.ofPattern("d MMM")

    fun load(uuid: String) {
        if (bookingUuid == uuid && _uiState.value.booking != null) return
        bookingUuid = uuid
        _uiState.update {
            it.copy(isLoading = true, errorMessage = null, availableDates = buildDates())
        }
        viewModelScope.launch {
            when (val result = bookingRepository.getBooking(uuid)) {
                is ApiResult.Success -> {
                    customerWorkspaceStore.applyBookingUpdate(result.data)
                    _uiState.update {
                        it.copy(isLoading = false, booking = result.data)
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun openCancelSheet() {
        val uuid = bookingUuid ?: return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val preview = bookingRepository.cancelPreview(uuid)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        cancelPreview = preview.data,
                        activeSheet = BookingDetailSheet.CANCEL_REASON
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = preview.message)
                }
            }
        }
    }

    fun selectCancelReason(reason: CancellationReasonCode) {
        _uiState.update { it.copy(selectedReason = reason) }
    }

    fun onCancelCommentChange(value: String) {
        _uiState.update { it.copy(cancelComment = value.take(500)) }
    }

    fun proceedToCancelConfirm() {
        if (_uiState.value.selectedReason == null) {
            _uiState.update { it.copy(errorMessage = "Please select a cancellation reason") }
            return
        }
        _uiState.update { it.copy(activeSheet = BookingDetailSheet.CANCEL_CONFIRM, errorMessage = null) }
    }

    fun confirmCancel() {
        val uuid = bookingUuid ?: return
        val reason = _uiState.value.selectedReason ?: return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = bookingRepository.cancelBooking(
                uuid, reason.name, _uiState.value.cancelComment.ifBlank { null }
            )) {
                is ApiResult.Success -> {
                    customerWorkspaceStore.onBookingMutated(result.data, walletRepository)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            booking = result.data,
                            activeSheet = BookingDetailSheet.NONE,
                            successMessage = "Booking cancelled successfully"
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }

    fun openRescheduleSheet() {
        val booking = _uiState.value.booking ?: return
        if (!booking.canReschedule) {
            _uiState.update { it.copy(errorMessage = "Reschedule limit reached for this booking") }
            return
        }
        _uiState.update {
            it.copy(
                activeSheet = BookingDetailSheet.RESCHEDULE,
                selectedDate = booking.rawDate.takeIf { d -> d.isNotBlank() },
                selectedSlot = null,
                errorMessage = null
            )
        }
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun selectSlot(slot: RescheduleSlot) {
        _uiState.update { it.copy(selectedSlot = slot) }
    }

    fun proceedToRescheduleConfirm() {
        val state = _uiState.value
        if (state.selectedDate.isNullOrBlank() || state.selectedSlot == null) {
            _uiState.update { it.copy(errorMessage = "Please select a date and time slot") }
            return
        }
        _uiState.update { it.copy(activeSheet = BookingDetailSheet.RESCHEDULE_CONFIRM, errorMessage = null) }
    }

    fun confirmReschedule() {
        val uuid = bookingUuid ?: return
        val date = _uiState.value.selectedDate ?: return
        val slot = _uiState.value.selectedSlot ?: return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = bookingRepository.rescheduleBooking(uuid, date, slot.start, slot.end)) {
                is ApiResult.Success -> {
                    customerWorkspaceStore.applyBookingUpdate(result.data)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            booking = result.data,
                            activeSheet = BookingDetailSheet.NONE,
                            successMessage = "Booking rescheduled successfully"
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(activeSheet = BookingDetailSheet.NONE, errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun buildDates(): List<Pair<String, String>> {
        val today = LocalDate.now()
        return (0..7).map { offset ->
            val date = today.plusDays(offset.toLong())
            date.toString() to date.format(displayDateFormatter)
        }
    }
}
