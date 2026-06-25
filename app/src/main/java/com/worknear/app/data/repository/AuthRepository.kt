package com.worknear.app.data.repository

import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.AuthData
import com.worknear.app.data.remote.dto.ChangeRoleBody
import com.worknear.app.data.remote.dto.OtpRequestBody
import com.worknear.app.data.remote.dto.OtpRequestData
import com.worknear.app.data.remote.dto.OtpVerifyBody
import com.worknear.app.data.remote.safeCall

/** Result of a successful OTP verification, used to drive post-login routing. */
data class AuthOutcome(
    val newUser: Boolean,
    val role: String?
)

class AuthRepository(
    private val api: WorkNearApi,
    private val tokenStore: TokenStore
) {

    /** Requests an OTP. In mock mode the response carries a [OtpRequestData.devCode] for testing. */
    suspend fun requestOtp(phone: String): ApiResult<OtpRequestData> =
        safeCall { api.requestOtp(OtpRequestBody(phone = phone)) }

    /**
     * Verifies the OTP and, on success, persists the session. New accounts are created as CUSTOMER
     * by default; the role can be upgraded straight after via [setRole] (the "Who are you?" step).
     */
    suspend fun verifyOtp(phone: String, code: String): ApiResult<AuthOutcome> {
        return when (val result = safeCall { api.verifyOtp(OtpVerifyBody(phone = phone, code = code)) }) {
            is ApiResult.Success -> {
                if (persist(result.data)) {
                    syncPendingRoleSelection(result.data.user?.roleConfirmed)
                    ApiResult.Success(
                        AuthOutcome(newUser = result.data.newUser, role = result.data.user?.role)
                    )
                } else {
                    ApiResult.Error("Login succeeded but no tokens were returned")
                }
            }
            is ApiResult.Error -> result
        }
    }

    /**
     * Sets the signed-in user's role during onboarding (CUSTOMER or PROFESSIONAL). The backend
     * re-issues tokens (the JWT embeds the role), which we persist so subsequent calls are authorized
     * for the new role.
     */
    suspend fun setRole(role: String): ApiResult<Unit> {
        return when (val result = safeCall { api.setRole(ChangeRoleBody(role)) }) {
            is ApiResult.Success -> {
                if (persist(result.data)) {
                    syncPendingRoleSelection(result.data.user?.roleConfirmed)
                    ApiResult.Success(Unit)
                }
                else ApiResult.Error("Could not update role")
            }
            is ApiResult.Error -> result
        }
    }

    /** Persists tokens + lightweight user info. Returns false when the response is missing tokens. */
    private suspend fun persist(auth: AuthData): Boolean {
        val access = auth.accessToken
        val refresh = auth.refreshToken
        if (access.isNullOrBlank() || refresh.isNullOrBlank()) return false
        tokenStore.saveSession(
            accessToken = access,
            refreshToken = refresh,
            userId = auth.user?.id,
            userName = auth.user?.fullName,
            userPhone = auth.user?.phone,
            userRole = auth.user?.role
        )
        return true
    }

    suspend fun logout() = tokenStore.clear()

    /** Keeps the local pending-role flag aligned with the backend source of truth. */
    private suspend fun syncPendingRoleSelection(roleConfirmed: Boolean?) {
        if (roleConfirmed == true) tokenStore.clearPendingRoleSelection()
        else tokenStore.setPendingRoleSelection(true)
    }
}
