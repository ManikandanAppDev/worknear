package com.worknear.app.ui.serviceflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Professional
import com.worknear.app.data.model.ServiceCatalog
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.CreateBookingBody
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.BookingRepository
import com.worknear.app.data.repository.ProfessionalRepository
import com.worknear.app.ui.address.AddressStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class BookingFlowUiState(
    val loadingMatches: Boolean = false,
    val matches: List<Professional> = emptyList(),
    val matchError: String? = null,
    val submitting: Boolean = false,
    val submitError: String? = null
)

/**
 * Drives the multi-service booking flow's network calls: loading real professionals for the
 * chosen category on the Best Matches screen and persisting the booking on confirmation.
 */
class BookingFlowViewModel(
    private val professionalRepository: ProfessionalRepository,
    private val accountRepository: AccountRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingFlowUiState())
    val uiState: StateFlow<BookingFlowUiState> = _uiState.asStateFlow()

    private var loadedCategory: String? = null

    /** Loads the real professionals that serve the active flow category. */
    fun loadMatches(force: Boolean = false) {
        val slug = ServiceFlowState.categoryId
        if (!force && loadedCategory == slug && _uiState.value.matches.isNotEmpty()) return
        loadedCategory = slug
        val label = ServiceCatalog.titleFor(slug)
        _uiState.update { it.copy(loadingMatches = true, matchError = null) }
        viewModelScope.launch {
            when (val result = professionalRepository.searchByCategory(slug, label)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(loadingMatches = false, matches = result.data, matchError = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(loadingMatches = false, matchError = result.message)
                }
            }
        }
    }

    fun clearSubmitError() = _uiState.update { it.copy(submitError = null) }

    /**
     * Resolves the category UUID for the selected professional and creates the booking.
     * Invokes [onSuccess] with the new booking id when the API confirms it.
     */
    fun confirmBooking(onSuccess: (String) -> Unit) {
        val proId = ServiceFlowState.selectedProId
        val isoDate = ServiceFlowState.scheduledIsoDate
        val slot = parseSlot(ServiceFlowState.scheduledTime)
        val addressId = AddressStore.selected?.id

        if (proId.isNullOrBlank()) {
            _uiState.update { it.copy(submitError = "Please select a professional") }
            return
        }
        if (isoDate.isNullOrBlank() || slot == null) {
            _uiState.update { it.copy(submitError = "Please choose a date and time") }
            return
        }
        if (addressId.isNullOrBlank()) {
            _uiState.update { it.copy(submitError = "Please add a service address") }
            return
        }

        _uiState.update { it.copy(submitting = true, submitError = null) }
        viewModelScope.launch {
            val categoryId = resolveCategoryId(proId)
            if (categoryId == null) {
                _uiState.update {
                    it.copy(submitting = false, submitError = "This professional has no active service")
                }
                return@launch
            }
            val body = CreateBookingBody(
                professionalId = proId,
                categoryId = categoryId,
                scheduledDate = isoDate,
                slotStart = slot.first,
                slotEnd = slot.second,
                addressId = addressId,
                problemDescription = ServiceFlowState.notes.ifBlank { null },
                paymentMethod = null
            )
            when (val result = bookingRepository.createBooking(body)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(submitting = false) }
                    val id = result.data.id
                    if (id != null) onSuccess(id) else _uiState.update {
                        it.copy(submitError = "Booking failed. Please try again.")
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(submitting = false, submitError = result.message)
                }
            }
        }
    }

    /** Looks up the backend category UUID from the professional's services, matching the flow slug. */
    private suspend fun resolveCategoryId(proId: String): String? {
        val detail = professionalRepository.getDetailRaw(proId)
        if (detail !is ApiResult.Success) return null
        val services = detail.data.services
        val slug = ServiceFlowState.categoryId
        val match = services.firstOrNull { it.categorySlug == slug && it.active }
            ?: services.firstOrNull { it.categorySlug == slug }
            ?: services.firstOrNull { it.active }
            ?: services.firstOrNull()
        return match?.categoryId
    }

    private companion object {
        val INPUT = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        val OUTPUT = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    }

    /** Converts a display slot like "9:00 AM" into 24h start/end (HH:mm), end = start + 1h. */
    private fun parseSlot(label: String?): Pair<String, String>? {
        if (label.isNullOrBlank()) return null
        return try {
            val start = LocalTime.parse(label.trim().uppercase(Locale.US), INPUT)
            val end = start.plusHours(1)
            OUTPUT.format(start) to OUTPUT.format(end)
        } catch (e: Exception) {
            null
        }
    }
}
