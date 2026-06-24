package com.worknear.app.data.remote

import com.worknear.app.data.local.TokenStore
import com.worknear.app.data.remote.dto.RefreshBody
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Transparently refreshes an expired access token when the server replies 401.
 *
 * Flow: a request fails with 401 -> exchange the stored refresh token for a new token pair ->
 * persist it and retry the original request with the new access token. If refresh fails (e.g. the
 * 30-day refresh token has also expired), the session is cleared, which flips
 * [TokenStore.isLoggedIn] to false and the nav graph routes the user back to onboarding.
 *
 * This is what prevents the "authentication required" message after the app sits idle past the
 * 60-minute access-token lifetime.
 */
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val refreshApi: AuthRefreshApi
) : Authenticator {

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        // Never try to refresh the auth endpoints themselves.
        if (response.request.url.encodedPath.contains("/api/v1/auth/")) return null

        // Give up if we've already retried this request once.
        if (priorResponseCount(response) >= 2) return null

        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        // Another request may have already refreshed the token while this one was in flight.
        val current = tokenStore.accessTokenSnapshot
        if (!current.isNullOrBlank() && current != failedToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $current")
                .build()
        }

        val refreshToken = tokenStore.refreshTokenSnapshot
        if (refreshToken.isNullOrBlank()) return null

        val newTokens = try {
            val resp = refreshApi.refresh(RefreshBody(refreshToken)).execute()
            if (resp.isSuccessful) resp.body()?.data else null
        } catch (e: Exception) {
            null
        }

        val newAccess = newTokens?.accessToken
        val newRefresh = newTokens?.refreshToken
        if (newAccess.isNullOrBlank() || newRefresh.isNullOrBlank()) {
            // Refresh failed -> force logout.
            runBlocking { tokenStore.clear() }
            return null
        }

        runBlocking { tokenStore.updateTokens(newAccess, newRefresh) }
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }

    private fun priorResponseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
