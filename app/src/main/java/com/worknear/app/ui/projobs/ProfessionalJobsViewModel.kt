package com.worknear.app.ui.projobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Booking
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.BookingRepository
import com.worknear.app.data.repository.ProfessionalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfessionalJobsUiState(
    val isLoading: Boolean = true,
    val jobs: List<Booking> = emptyList(),
    val selectedJob: Booking? = null,
    val otp: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    /** UNSUBMITTED / PENDING / MORE_INFO / APPROVED / REJECTED — gates whether the pro can get jobs. */
    val verificationStatus: String? = null
)

class ProfessionalJobsViewModel(
    private val bookingRepository: BookingRepository,
    private val professionalRepository: ProfessionalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfessionalJobsUiState())
    val uiState: StateFlow<ProfessionalJobsUiState> = _uiState.asStateFlow()

    init {
        loadVerificationStatus()
        loadJobs()
    }

    private fun loadVerificationStatus() {
        viewModelScope.launch {
            val status = (professionalRepository.getMyProfile() as? ApiResult.Success)?.data?.verificationStatus
            if (status != null) _uiState.update { it.copy(verificationStatus = status) }
        }
    }

    fun loadJobs() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val requests = bookingRepository.professionalBookings("REQUESTS")
            val active = bookingRepository.professionalBookings("ACTIVE")
            if (requests is ApiResult.Success && active is ApiResult.Success) {
                val jobs = requests.data + active.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        jobs = jobs,
                        selectedJob = it.selectedJob ?: jobs.firstOrNull()
                    )
                }
            } else {
                val message = (requests as? ApiResult.Error)?.message
                    ?: (active as? ApiResult.Error)?.message
                    ?: "Unable to load jobs"
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            }
        }
    }

    fun selectJob(job: Booking) {
        _uiState.update { it.copy(selectedJob = job, otp = "", errorMessage = null, successMessage = null) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value.filter(Char::isDigit).take(6)) }
    }

    fun markOnTheWay() = updateSelected("Marked on the way") { bookingRepository.markOnTheWay(it) }

    fun acceptJob() = updateSelected("Job accepted") { bookingRepository.acceptBooking(it) }

    fun markArrived() = updateSelected("Marked arrived") { bookingRepository.markArrived(it) }

    fun startWork() = updateSelected("Work started") { bookingRepository.startWork(it) }

    fun markWorkCompleted() = updateSelected("Waiting for customer OTP") { bookingRepository.markWorkCompleted(it) }

    fun verifyOtp() {
        val otp = _uiState.value.otp
        if (otp.length != 6) {
            _uiState.update { it.copy(errorMessage = "Enter the 6-digit customer OTP") }
            return
        }
        updateSelected("Booking completed") { bookingRepository.verifyCompletionOtp(it, otp) }
    }

    private fun updateSelected(
        success: String,
        action: suspend (String) -> ApiResult<Booking>
    ) {
        val job = _uiState.value.selectedJob ?: return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            when (val result = action(job.uuid)) {
                is ApiResult.Success -> _uiState.update { state ->
                    val updatedJobs = state.jobs.map { if (it.uuid == result.data.uuid) result.data else it }
                    state.copy(
                        isSubmitting = false,
                        jobs = updatedJobs,
                        selectedJob = result.data,
                        otp = "",
                        successMessage = success
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}
