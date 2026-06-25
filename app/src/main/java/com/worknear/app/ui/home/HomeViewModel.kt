package com.worknear.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.Professional
import com.worknear.app.data.model.ServiceCategory
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.BookingRepository
import com.worknear.app.data.repository.CatalogRepository
import com.worknear.app.data.repository.CustomerWorkspaceStore
import com.worknear.app.data.repository.ProfessionalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "there",
    val location: String = "Chennai",
    val searchQuery: String = "",
    val categories: List<ServiceCategory> = emptyList(),
    val professionals: List<Professional> = emptyList(),
    val recentBookings: List<Booking> = emptyList(),
    val bookingsLoading: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val catalogRepository: CatalogRepository,
    private val professionalRepository: ProfessionalRepository,
    private val bookingRepository: BookingRepository,
    private val customerWorkspaceStore: CustomerWorkspaceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            customerWorkspaceStore.profile.collect { profile ->
                val firstName = profile.name
                    .substringBefore(" ")
                    .takeIf { it.isNotBlank() }
                    ?: "there"
                _uiState.update { it.copy(userName = firstName) }
            }
        }
        viewModelScope.launch {
            combine(
                customerWorkspaceStore.upcomingBookings,
                customerWorkspaceStore.isRefreshing
            ) { bookings, refreshing ->
                bookings to refreshing
            }.collect { (bookings, refreshing) ->
                _uiState.update {
                    it.copy(
                        recentBookings = bookings,
                        bookingsLoading = refreshing && bookings.isEmpty()
                    )
                }
            }
        }
        loadHomeData()
        loadRecentBookings()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadRecentBookings() {
        _uiState.update { it.copy(bookingsLoading = true) }
        viewModelScope.launch {
            customerWorkspaceStore.refreshBookings(bookingRepository)
            _uiState.update { it.copy(bookingsLoading = false) }
        }
    }

    fun loadHomeData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val categoriesResult = catalogRepository.getCategories()) {
                is ApiResult.Success -> {
                    val categories = categoriesResult.data
                    _uiState.update { it.copy(categories = categories) }
                    loadPopular(categories.firstOrNull())
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = categoriesResult.message)
                }
            }
        }
    }

    private suspend fun loadPopular(firstCategory: ServiceCategory?) {
        if (firstCategory == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        when (val pros = professionalRepository.searchByCategory(
            categorySlug = firstCategory.id,
            professionLabel = firstCategory.title,
            sort = "TOP_RATED"
        )) {
            is ApiResult.Success -> _uiState.update {
                it.copy(isLoading = false, professionals = pros.data)
            }
            is ApiResult.Error -> _uiState.update {
                it.copy(isLoading = false, errorMessage = pros.message)
            }
        }
    }
}
