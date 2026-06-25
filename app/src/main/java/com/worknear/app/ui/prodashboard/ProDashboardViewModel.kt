package com.worknear.app.ui.prodashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.BookingStatus
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.ProProfileDto
import com.worknear.app.data.remote.dto.UpdateProProfileBody
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.BookingRepository
import com.worknear.app.data.repository.ProWorkspaceStore
import com.worknear.app.data.repository.ProfessionalRepository
import com.worknear.app.utils.ResolvedLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProDashboardUiState(
    val isRefreshing: Boolean = false,
    val dashboard: ProDashboardState = ProDashboardState.Loading,
    val workLocationLabel: String = "Update work location",
    val isUpdatingLocation: Boolean = false,
    val locationUpdateMessage: String? = null,
    val errorMessage: String? = null
)

class ProDashboardViewModel(
    private val accountRepository: AccountRepository,
    private val professionalRepository: ProfessionalRepository,
    private val bookingRepository: BookingRepository,
    private val proWorkspaceStore: ProWorkspaceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProDashboardUiState())
    val uiState: StateFlow<ProDashboardUiState> = _uiState.asStateFlow()

    private var verifiedName: String = "Professional"
    private var verifiedLocationLabel: String = "Update work location"

    init {
        viewModelScope.launch {
            combine(
                proWorkspaceStore.openJobs,
                proWorkspaceStore.completedJobs,
                proWorkspaceStore.rating
            ) { openJobs, completedJobs, rating ->
                Triple(openJobs, completedJobs, rating)
            }.collect { (openJobs, completedJobs, rating) ->
                val current = _uiState.value.dashboard
                if (current is ProDashboardState.Verified) {
                    rebuildVerifiedDashboard(openJobs, completedJobs, rating)
                }
            }
        }
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            val meResult = accountRepository.getMe()
            val profileResult = professionalRepository.getMyProfile()

            if (meResult is ApiResult.Error) {
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = meResult.message, dashboard = ProDashboardState.Loading)
                }
                return@launch
            }
            if (profileResult is ApiResult.Error) {
                _uiState.update {
                    it.copy(isRefreshing = false, errorMessage = profileResult.message, dashboard = ProDashboardState.Loading)
                }
                return@launch
            }

            val me = (meResult as ApiResult.Success).data
            val profile = (profileResult as ApiResult.Success).data
            val locationLabel = formatWorkLocation(profile.city, profile.area)
            val name = me.fullName?.takeIf { it.isNotBlank() }
                ?: profile.name?.takeIf { it.isNotBlank() }
                ?: "Professional"

            if (me.status.equals("BLOCKED", ignoreCase = true)) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        workLocationLabel = locationLabel,
                        dashboard = ProDashboardState.Blocked(name)
                    )
                }
                return@launch
            }

            when (profile.verificationStatus?.uppercase()) {
                "APPROVED" -> loadVerifiedDashboard(name, profile, locationLabel)
                "PENDING" -> _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        workLocationLabel = locationLabel,
                        dashboard = ProDashboardState.VerificationPending(name)
                    )
                }
                "REJECTED", "MORE_INFO" -> {
                    val note = profile.documents
                        .firstOrNull { !it.reviewNote.isNullOrBlank() }
                        ?.reviewNote
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            workLocationLabel = locationLabel,
                            dashboard = ProDashboardState.VerificationRejected(name, note)
                        )
                    }
                }
                else -> _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        workLocationLabel = locationLabel,
                        dashboard = ProDashboardState.ProfileIncomplete(name)
                    )
                }
            }
        }
    }

    fun clearLocationUpdateMessage() {
        _uiState.update { it.copy(locationUpdateMessage = null) }
    }

    fun onLocationScanFailed(message: String = "Couldn't detect location. Check GPS and try again.") {
        _uiState.update {
            it.copy(isUpdatingLocation = false, locationUpdateMessage = message)
        }
    }

    fun updateWorkLocation(resolved: ResolvedLocation) {
        _uiState.update { it.copy(isUpdatingLocation = true, locationUpdateMessage = null, errorMessage = null) }
        viewModelScope.launch {
            when (val result = professionalRepository.updateMyProfile(
                UpdateProProfileBody(
                    city = resolved.city.trim(),
                    area = resolved.area.trim(),
                    baseLatitude = resolved.latitude,
                    baseLongitude = resolved.longitude
                )
            )) {
                is ApiResult.Success -> {
                    val label = formatWorkLocation(result.data.city, result.data.area)
                    _uiState.update {
                        it.copy(
                            isUpdatingLocation = false,
                            workLocationLabel = label,
                            locationUpdateMessage = "Work location updated"
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isUpdatingLocation = false, locationUpdateMessage = result.message)
                }
            }
        }
    }

    private suspend fun loadVerifiedDashboard(name: String, profile: ProProfileDto, locationLabel: String) {
        verifiedName = name
        verifiedLocationLabel = locationLabel

        proWorkspaceStore.refresh(bookingRepository, professionalRepository)

        rebuildVerifiedDashboard(
            openJobs = proWorkspaceStore.openJobs.value,
            completedJobs = proWorkspaceStore.completedJobs.value,
            rating = profile.rating
        )
        _uiState.update { it.copy(isRefreshing = false, workLocationLabel = locationLabel) }
    }

    private fun rebuildVerifiedDashboard(
        openJobs: List<Booking>,
        completedJobs: List<Booking>,
        rating: Double
    ) {
        val today = LocalDate.now().toString()
        val todayJobs = openJobs.filter { it.rawDate == today }
        val completedToday = completedJobs.filter {
            it.status == BookingStatus.COMPLETED && it.rawDate == today
        }
        val earningsToday = completedToday.sumOf { job ->
            if (job.proEarning > 0) job.proEarning else job.price
        }

        _uiState.update {
            it.copy(
                workLocationLabel = verifiedLocationLabel,
                dashboard = ProDashboardState.Verified(
                    name = verifiedName,
                    summary = ProTodaySummary(
                        jobsCompleted = completedToday.size,
                        rating = rating,
                        earnings = earningsToday
                    ),
                    todayJobs = todayJobs
                )
            )
        }
    }

    private fun formatWorkLocation(city: String?, area: String?): String {
        val parts = listOfNotNull(
            area?.trim()?.takeIf { it.isNotBlank() },
            city?.trim()?.takeIf { it.isNotBlank() }
        ).distinct()
        return parts.joinToString(", ").ifBlank { "Update work location" }
    }
}
