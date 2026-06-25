package com.worknear.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.AuthRepository
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
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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
                        name = me.data.fullName ?: "WorkNear User",
                        phone = me.data.phone.orEmpty(),
                        email = me.data.email.orEmpty(),
                        avatarUrl = localAvatar ?: me.data.avatarUrl
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = me.message)
                }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
