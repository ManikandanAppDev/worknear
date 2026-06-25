package com.worknear.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.CreateBookingBody
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.BookingRepository
import com.worknear.app.data.repository.CustomerWorkspaceStore
import com.worknear.app.data.repository.WalletRepository
import com.worknear.app.data.repository.ProfessionalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class BookServiceUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val serviceName: String = "",
    val professionalName: String = "",
    val price: Int = 0,
    val date: String = "",
    val timeSlot: String = "",
    val problemDescription: String = "",
    val addressLabel: String = ""
)

class BookServiceViewModel(
    private val professionalRepository: ProfessionalRepository,
    private val accountRepository: AccountRepository,
    private val bookingRepository: BookingRepository,
    private val walletRepository: WalletRepository,
    private val customerWorkspaceStore: CustomerWorkspaceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookServiceUiState())
    val uiState: StateFlow<BookServiceUiState> = _uiState.asStateFlow()

    private var professionalUserId: String? = null
    private var categoryId: String? = null
    private var addressId: String? = null

    // Sensible defaults for the demo flow; a date/slot picker can replace these later.
    private val scheduledDate: LocalDate = LocalDate.now().plusDays(1)
    private val slotStart = "10:00"
    private val slotEnd = "12:00"

    fun load(userId: String) {
        if (professionalUserId == userId && _uiState.value.serviceName.isNotEmpty()) return
        professionalUserId = userId
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val detail = professionalRepository.getDetailRaw(userId)) {
                is ApiResult.Success -> {
                    val service = detail.data.services.firstOrNull { it.active }
                        ?: detail.data.services.firstOrNull()
                    categoryId = service?.categoryId
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            serviceName = service?.categoryName ?: "Service",
                            professionalName = detail.data.name ?: "Professional",
                            price = (service?.basePrice ?: 0.0).toInt(),
                            date = scheduledDate.toString(),
                            timeSlot = "$slotStart - $slotEnd"
                        )
                    }
                    loadDefaultAddress()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = detail.message)
                }
            }
        }
    }

    private suspend fun loadDefaultAddress() {
        when (val addr = accountRepository.getAddresses()) {
            is ApiResult.Success -> {
                val chosen = addr.data.firstOrNull { it.isDefault } ?: addr.data.firstOrNull()
                addressId = chosen?.id
                _uiState.update {
                    it.copy(addressLabel = chosen?.let { a -> listOfNotNull(a.label, a.city).joinToString(" • ") } ?: "")
                }
            }
            is ApiResult.Error -> Unit
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(problemDescription = value) }
    }

    fun submit(onBooked: (String) -> Unit) {
        val proId = professionalUserId
        val catId = categoryId
        if (proId == null || catId == null) {
            _uiState.update { it.copy(errorMessage = "Service details are still loading") }
            return
        }
        if (addressId == null) {
            _uiState.update { it.copy(errorMessage = "Please add a service address in your profile first") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val body = CreateBookingBody(
                professionalId = proId,
                categoryId = catId,
                scheduledDate = scheduledDate.toString(),
                slotStart = slotStart,
                slotEnd = slotEnd,
                addressId = addressId,
                problemDescription = _uiState.value.problemDescription.ifBlank { null },
                paymentMethod = null
            )
            when (val result = bookingRepository.createBooking(body)) {
                is ApiResult.Success -> {
                    customerWorkspaceStore.onBookingCreated(walletRepository, bookingRepository)
                    _uiState.update { it.copy(isSubmitting = false) }
                    result.data.id?.let { onBooked(it) }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}
