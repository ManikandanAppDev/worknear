package com.worknear.app.ui.bookings

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

data class MyBookingsUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MyBookingsViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBookingsUiState())
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    private var currentTab: String? = null

    /** tab: UPCOMING | COMPLETED | CANCELLED */
    fun load(tab: String) {
        currentTab = tab
        _uiState.update { it.copy(isLoading = true, errorMessage = null, bookings = emptyList()) }
        viewModelScope.launch {
            when (val result = bookingRepository.customerBookings(tab)) {
                is ApiResult.Success -> {
                    // Guard against out-of-order responses when tabs are switched quickly.
                    if (currentTab == tab) {
                        _uiState.update { it.copy(isLoading = false, bookings = result.data) }
                    }
                }
                is ApiResult.Error -> if (currentTab == tab) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
