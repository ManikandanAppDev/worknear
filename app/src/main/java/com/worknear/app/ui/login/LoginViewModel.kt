package com.worknear.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginStep { PHONE, OTP }

data class LoginUiState(
    val step: LoginStep = LoginStep.PHONE,
    val phone: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Populated in the backend's mock OTP mode so you can log in without an SMS gateway. */
    val devCode: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value.filter { ch -> ch.isDigit() }.take(6), errorMessage = null) }
    }

    fun backToPhone() {
        _uiState.update { it.copy(step = LoginStep.PHONE, otp = "", errorMessage = null) }
    }

    fun requestOtp() {
        val phone = normalizePhone(_uiState.value.phone)
        if (phone == null) {
            _uiState.update { it.copy(errorMessage = "Enter a valid mobile number") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.requestOtp(phone)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        step = LoginStep.OTP,
                        devCode = result.data.devCode
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        val phone = normalizePhone(_uiState.value.phone) ?: return
        val code = _uiState.value.otp
        if (code.length < 4) {
            _uiState.update { it.copy(errorMessage = "Enter the OTP sent to your phone") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.verifyOtp(phone, code)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    /** Normalizes to E.164. Accepts "+9190..." as-is, or a bare 10-digit Indian number. */
    private fun normalizePhone(raw: String): String? {
        val trimmed = raw.trim().replace(" ", "")
        return when {
            trimmed.matches(Regex("\\+[1-9]\\d{7,14}")) -> trimmed
            trimmed.matches(Regex("[6-9]\\d{9}")) -> "+91$trimmed"
            else -> null
        }
    }
}
