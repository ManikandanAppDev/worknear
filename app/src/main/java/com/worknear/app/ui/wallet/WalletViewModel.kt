package com.worknear.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Transaction
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WalletUiState(
    val balance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class WalletViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val balance = walletRepository.getBalance()) {
                is ApiResult.Success -> _uiState.update { it.copy(balance = balance.data) }
                is ApiResult.Error -> _uiState.update { it.copy(errorMessage = balance.message) }
            }
            when (val txns = walletRepository.getTransactions()) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, transactions = txns.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = txns.message) }
            }
        }
    }
}
