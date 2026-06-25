package com.worknear.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoleSelectUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Drives the post-OTP "Who are you?" step. New accounts are CUSTOMER by default; choosing
 * "I provide a service" upgrades the role to PROFESSIONAL on the backend (re-issuing tokens).
 */
class RoleSelectViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoleSelectUiState())
    val uiState: StateFlow<RoleSelectUiState> = _uiState.asStateFlow()

    /** Customer keeps CUSTOMER; confirms the choice on the backend before continuing. */
    fun chooseCustomer(onDone: () -> Unit) {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.setRole("CUSTOMER")) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onDone()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }

    fun chooseProfessional(onDone: () -> Unit) {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.setRole("PROFESSIONAL")) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onDone()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}
