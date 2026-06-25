package com.worknear.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.UpdateProfileBody
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.utils.filterMobileInput
import com.worknear.app.utils.normalizeIndianMobile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PhoneChangeStep { ENTER_PHONE, ENTER_OTP }

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUri: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedMessage: String? = null,
    val errorMessage: String? = null,
    // Phone-change sub-flow
    val showPhoneSheet: Boolean = false,
    val phoneStep: PhoneChangeStep = PhoneChangeStep.ENTER_PHONE,
    val newPhone: String = "",
    val otpCode: String = "",
    val devCode: String? = null,
    val phoneSubmitting: Boolean = false,
    val phoneError: String? = null
)

class EditProfileViewModel(
    private val accountRepository: AccountRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val localAvatar = tokenStore.cachedAvatar()
            when (val me = accountRepository.getMe()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = me.data.fullName.orEmpty(),
                        phone = me.data.phone.orEmpty(),
                        email = me.data.email.orEmpty(),
                        avatarUri = localAvatar ?: me.data.avatarUrl
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = me.message, avatarUri = localAvatar)
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, savedMessage = null, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, savedMessage = null, errorMessage = null) }
    }

    fun saveProfile() {
        val name = _uiState.value.name.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your name") }
            return
        }
        _uiState.update { it.copy(isSaving = true, errorMessage = null, savedMessage = null) }
        viewModelScope.launch {
            val body = UpdateProfileBody(
                fullName = name,
                email = _uiState.value.email.trim().ifBlank { null }
            )
            when (val result = accountRepository.updateProfile(body)) {
                is ApiResult.Success -> {
                    tokenStore.updateProfileLocal(name = name)
                    _uiState.update {
                        it.copy(isSaving = false, savedMessage = "Profile updated")
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    /** Persists the picked image (a file:// URI) locally and shows it immediately. */
    fun onAvatarPicked(uri: String) {
        viewModelScope.launch {
            tokenStore.saveAvatar(uri)
            _uiState.update { it.copy(avatarUri = uri, savedMessage = "Photo updated") }
        }
    }

    // ----- Phone change -----

    fun openPhoneSheet() {
        _uiState.update {
            it.copy(
                showPhoneSheet = true,
                phoneStep = PhoneChangeStep.ENTER_PHONE,
                newPhone = "",
                otpCode = "",
                devCode = null,
                phoneError = null
            )
        }
    }

    fun closePhoneSheet() {
        _uiState.update { it.copy(showPhoneSheet = false, phoneError = null) }
    }

    fun onNewPhoneChange(value: String) {
        _uiState.update { it.copy(newPhone = filterMobileInput(value), phoneError = null) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otpCode = value.filter { ch -> ch.isDigit() }.take(6), phoneError = null) }
    }

    fun requestPhoneOtp() {
        val phone = normalizeIndianMobile(_uiState.value.newPhone)
        if (phone == null) {
            _uiState.update { it.copy(phoneError = "Enter a valid 10-digit mobile number") }
            return
        }
        _uiState.update { it.copy(phoneSubmitting = true, phoneError = null) }
        viewModelScope.launch {
            when (val result = accountRepository.requestPhoneChangeOtp(phone)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        phoneSubmitting = false,
                        phoneStep = PhoneChangeStep.ENTER_OTP,
                        devCode = result.data.devCode
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(phoneSubmitting = false, phoneError = result.message)
                }
            }
        }
    }

    fun confirmPhoneChange() {
        val phone = normalizeIndianMobile(_uiState.value.newPhone) ?: return
        val code = _uiState.value.otpCode
        if (code.length < 4) {
            _uiState.update { it.copy(phoneError = "Enter the OTP sent to your phone") }
            return
        }
        _uiState.update { it.copy(phoneSubmitting = true, phoneError = null) }
        viewModelScope.launch {
            when (val result = accountRepository.confirmPhoneChange(phone, code)) {
                is ApiResult.Success -> {
                    val auth = result.data
                    val access = auth.accessToken
                    val refresh = auth.refreshToken
                    if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                        tokenStore.updateTokens(access, refresh)
                    }
                    tokenStore.updateProfileLocal(phone = phone)
                    _uiState.update {
                        it.copy(
                            phoneSubmitting = false,
                            showPhoneSheet = false,
                            phone = phone,
                            savedMessage = "Phone number updated"
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(phoneSubmitting = false, phoneError = result.message)
                }
            }
        }
    }
}
