package com.worknear.app.data.repository

import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.BookingStatus
import com.worknear.app.data.remote.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Shared in-memory cache for professional tab data (jobs, earnings, rating).
 * Updated after job actions and refreshed when switching pro tabs.
 */
class ProWorkspaceStore {

    private val refreshMutex = Mutex()

    private val _openJobs = MutableStateFlow<List<Booking>>(emptyList())
    val openJobs: StateFlow<List<Booking>> = _openJobs.asStateFlow()

    private val _completedJobs = MutableStateFlow<List<Booking>>(emptyList())
    val completedJobs: StateFlow<List<Booking>> = _completedJobs.asStateFlow()

    private val _rating = MutableStateFlow(0.0)
    val rating: StateFlow<Double> = _rating.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    suspend fun refresh(
        bookingRepository: BookingRepository,
        professionalRepository: ProfessionalRepository
    ) {
        refreshMutex.withLock {
            _isRefreshing.value = true
            val requests = bookingRepository.professionalBookings("REQUESTS")
            val active = bookingRepository.professionalBookings("ACTIVE")
            val completed = bookingRepository.professionalBookings("COMPLETED")
            val profile = professionalRepository.getMyProfile()

            if (requests is ApiResult.Success && active is ApiResult.Success) {
                _openJobs.value = requests.data + active.data
            }
            if (completed is ApiResult.Success) {
                _completedJobs.value = completed.data.filter { it.status == BookingStatus.COMPLETED }
            }
            if (profile is ApiResult.Success) {
                _rating.value = profile.data.rating
            }
            _isRefreshing.value = false
        }
    }

    fun applyBookingUpdate(booking: Booking) {
        if (booking.status == BookingStatus.COMPLETED) {
            _openJobs.update { jobs -> jobs.filter { it.uuid != booking.uuid } }
            _completedJobs.update { jobs ->
                listOf(booking) + jobs.filter { it.uuid != booking.uuid }
            }
        } else {
            _openJobs.update { jobs ->
                if (jobs.any { it.uuid == booking.uuid }) {
                    jobs.map { if (it.uuid == booking.uuid) booking else it }
                } else {
                    jobs + booking
                }
            }
        }
    }

    fun removeOpenJob(uuid: String) {
        _openJobs.update { jobs -> jobs.filter { it.uuid != uuid } }
    }
}
