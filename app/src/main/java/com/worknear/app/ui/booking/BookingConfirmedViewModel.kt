package com.worknear.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Booking
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookingConfirmedUiState(
    val booking: Booking? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class BookingConfirmedViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingConfirmedUiState())
    val uiState: StateFlow<BookingConfirmedUiState> = _uiState.asStateFlow()

    private var loadedId: String? = null

    fun load(bookingId: String) {
        if (loadedId == bookingId) return
        loadedId = bookingId
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = bookingRepository.getBooking(bookingId)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, booking = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}
