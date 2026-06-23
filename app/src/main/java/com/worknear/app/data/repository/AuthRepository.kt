package com.worknear.app.data.repository

import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.OtpRequestBody
import com.worknear.app.data.remote.dto.OtpRequestData
import com.worknear.app.data.remote.dto.OtpVerifyBody
import com.worknear.app.data.remote.safeCall

class AuthRepository(
    private val api: WorkNearApi,
    private val tokenStore: TokenStore
) {

    /** Requests an OTP. In mock mode the response carries a [OtpRequestData.devCode] for testing. */
    suspend fun requestOtp(phone: String): ApiResult<OtpRequestData> =
        safeCall { api.requestOtp(OtpRequestBody(phone = phone, role = "CUSTOMER")) }

    /** Verifies the OTP and, on success, persists the session. Returns true if a brand-new account was created. */
    suspend fun verifyOtp(phone: String, code: String): ApiResult<Boolean> {
        return when (val result = safeCall { api.verifyOtp(OtpVerifyBody(phone = phone, code = code, role = "CUSTOMER")) }) {
            is ApiResult.Success -> {
                val auth = result.data
                val access = auth.accessToken
                val refresh = auth.refreshToken
                if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
                    ApiResult.Error("Login succeeded but no tokens were returned")
                } else {
                    tokenStore.saveSession(
                        accessToken = access,
                        refreshToken = refresh,
                        userId = auth.user?.id,
                        userName = auth.user?.fullName,
                        userPhone = auth.user?.phone,
                        userRole = auth.user?.role
                    )
                    ApiResult.Success(auth.newUser)
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun logout() = tokenStore.clear()
}
