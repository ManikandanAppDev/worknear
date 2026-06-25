package com.worknear.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.AuthRepository
import com.worknear.app.utils.filterMobileInput
import com.worknear.app.utils.normalizeIndianMobile
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
        _uiState.update { it.copy(phone = filterMobileInput(value), errorMessage = null) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value.filter { ch -> ch.isDigit() }.take(6), errorMessage = null) }
    }

    fun backToPhone() {
        _uiState.update { it.copy(step = LoginStep.PHONE, otp = "", errorMessage = null) }
    }

    fun requestOtp() {
        val phone = normalizeIndianMobile(_uiState.value.phone)
        if (phone == null) {
            _uiState.update { it.copy(errorMessage = "Enter a valid 10-digit mobile number") }
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

    /**
     * [onSuccess] receives `newUser = true` when a brand-new account was just created (so the caller
     * can show the "Who are you?" role step), along with the account's current role.
     */
    fun verifyOtp(onSuccess: (newUser: Boolean, role: String?) -> Unit) {
        val phone = normalizeIndianMobile(_uiState.value.phone) ?: return
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
                    onSuccess(result.data.newUser, result.data.role)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
