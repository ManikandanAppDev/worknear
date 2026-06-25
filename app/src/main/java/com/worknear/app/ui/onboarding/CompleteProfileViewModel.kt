package com.worknear.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.AddressRequestBody
import com.worknear.app.data.remote.dto.UpdateProfileBody
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.ui.address.AddressStore
import com.worknear.app.ui.address.AddressType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompleteProfileUiState(
    val name: String = "",
    val addressType: AddressType = AddressType.HOME,
    val fullAddress: String = "",
    val landmark: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canSave: Boolean get() = name.isNotBlank() && fullAddress.isNotBlank()
}

/**
 * Drives the one-time "Complete your profile" step shown after OTP verification (and again
 * on every relaunch until completed). Persists the name via PATCH /me and the first address
 * via POST /me/addresses.
 */
class CompleteProfileViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()

    init {
        // Prefill the name if the account already has one (e.g. returning user with no address).
        viewModelScope.launch {
            when (val me = accountRepository.getMe()) {
                is ApiResult.Success -> me.data.fullName?.let { existing ->
                    if (existing.isNotBlank()) _uiState.update { it.copy(name = existing) }
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onTypeChange(type: AddressType) = _uiState.update { it.copy(addressType = type) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(fullAddress = value, errorMessage = null) }
    fun onLandmarkChange(value: String) = _uiState.update { it.copy(landmark = value) }
    fun useCurrentLocation() = _uiState.update { it.copy(fullAddress = "Current location, Chennai - 600001") }

    fun save(onDone: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) {
            _uiState.update { it.copy(errorMessage = "Please enter your name and address") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val nameResult = accountRepository.updateProfile(UpdateProfileBody(fullName = state.name.trim()))
            if (nameResult is ApiResult.Error) {
                _uiState.update { it.copy(isLoading = false, errorMessage = nameResult.message) }
                return@launch
            }

            val (line1, cityState) = splitAddress(state.fullAddress)
            val parts = cityState.split(" - ", limit = 2)
            val body = AddressRequestBody(
                label = state.addressType.label,
                line1 = line1,
                line2 = state.landmark.trim().takeIf { it.isNotBlank() },
                city = parts.getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() },
                pincode = parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() },
                makeDefault = true
            )
            when (val addressResult = accountRepository.createAddress(body)) {
                is ApiResult.Success -> {
                    AddressStore.refresh()
                    _uiState.update { it.copy(isLoading = false) }
                    onDone()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = addressResult.message)
                }
            }
        }
    }

    private fun splitAddress(input: String): Pair<String, String> {
        val trimmed = input.trim()
        val lastComma = trimmed.lastIndexOf(',')
        return if (lastComma in 1 until trimmed.length - 1) {
            trimmed.substring(0, lastComma).trim() to trimmed.substring(lastComma + 1).trim()
        } else {
            trimmed to ""
        }
    }
}
