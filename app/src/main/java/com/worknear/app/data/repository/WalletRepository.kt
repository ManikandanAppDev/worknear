package com.worknear.app.data.repository

import com.worknear.app.data.model.Transaction
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.safeCall
import com.worknear.app.data.remote.toUiModel

class WalletRepository(private val api: WorkNearApi) {

    suspend fun getBalance(): ApiResult<Double> {
        return when (val r = safeCall { api.wallet() }) {
            is ApiResult.Success -> ApiResult.Success(r.data.balance)
            is ApiResult.Error -> r
        }
    }

    suspend fun getTransactions(): ApiResult<List<Transaction>> {
        return when (val r = safeCall { api.walletTransactions() }) {
            is ApiResult.Success -> ApiResult.Success(r.data.content.map { it.toUiModel() })
            is ApiResult.Error -> r
        }
    }
}
