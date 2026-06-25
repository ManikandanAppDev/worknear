package com.worknear.app.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Booking
import com.worknear.app.data.repository.BookingRepository
import com.worknear.app.data.repository.CustomerWorkspaceStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyBookingsUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MyBookingsViewModel(
    private val bookingRepository: BookingRepository,
    private val customerWorkspaceStore: CustomerWorkspaceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBookingsUiState())
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    private var currentTab: String = "UPCOMING"

    init {
        viewModelScope.launch {
            combine(
                customerWorkspaceStore.upcomingBookings,
                customerWorkspaceStore.completedBookings,
                customerWorkspaceStore.cancelledBookings,
                customerWorkspaceStore.isRefreshing
            ) { upcoming, completed, cancelled, refreshing ->
                val bookings = bookingsForTab(currentTab, upcoming, completed, cancelled)
                bookings to refreshing
            }.collect { (bookings, refreshing) ->
                _uiState.update {
                    it.copy(
                        bookings = bookings,
                        isLoading = refreshing && bookings.isEmpty()
                    )
                }
            }
        }
    }

    /** tab: UPCOMING | COMPLETED | CANCELLED */
    fun load(tab: String) {
        currentTab = tab
        val bookings = bookingsForTab(
            tab,
            customerWorkspaceStore.upcomingBookings.value,
            customerWorkspaceStore.completedBookings.value,
            customerWorkspaceStore.cancelledBookings.value
        )
        _uiState.update {
            it.copy(
                isLoading = bookings.isEmpty(),
                errorMessage = null,
                bookings = bookings
            )
        }
        viewModelScope.launch {
            customerWorkspaceStore.refreshBookings(bookingRepository)
        }
    }

    private fun bookingsForTab(
        tab: String,
        upcoming: List<Booking>,
        completed: List<Booking>,
        cancelled: List<Booking>
    ): List<Booking> = when (tab) {
        "COMPLETED" -> completed
        "CANCELLED" -> cancelled
        else -> upcoming
    }
}
