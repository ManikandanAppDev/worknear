package com.worknear.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worknear.app.data.model.Transaction
import com.worknear.app.data.repository.CustomerWorkspaceStore
import com.worknear.app.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WalletUiState(
    val balance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class WalletViewModel(
    private val walletRepository: WalletRepository,
    private val customerWorkspaceStore: CustomerWorkspaceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                customerWorkspaceStore.walletBalance,
                customerWorkspaceStore.transactions,
                customerWorkspaceStore.isRefreshing
            ) { balance, transactions, refreshing ->
                Triple(balance, transactions, refreshing)
            }.collect { (balance, transactions, refreshing) ->
                _uiState.update {
                    it.copy(
                        balance = balance,
                        transactions = transactions,
                        isLoading = refreshing && transactions.isEmpty()
                    )
                }
            }
        }
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            customerWorkspaceStore.refreshWallet(walletRepository)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
