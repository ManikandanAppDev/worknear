package com.worknear.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoadingRole: Boolean = true,
    val isProfessional: Boolean = false
)

class MainViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadRole()
    }

    private fun loadRole() {
        viewModelScope.launch {
            when (val result = accountRepository.getMe()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isLoadingRole = false,
                        isProfessional = result.data.role.equals("PROFESSIONAL", ignoreCase = true)
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoadingRole = false) }
            }
        }
    }
}
