package com.worknear.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.AuthRepository
import com.worknear.app.data.repository.CustomerWorkspaceStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val customerWorkspaceStore: CustomerWorkspaceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            customerWorkspaceStore.profile.collect { profile ->
                _uiState.update {
                    it.copy(
                        name = profile.name,
                        phone = profile.phone,
                        email = profile.email,
                        avatarUrl = profile.avatarUrl,
                        isLoading = false
                    )
                }
            }
        }
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            customerWorkspaceStore.refreshProfile(accountRepository, tokenStore)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            customerWorkspaceStore.clear()
            onDone()
        }
    }
}
