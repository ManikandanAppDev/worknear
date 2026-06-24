package com.worknear.app.data.remote

import com.worknear.app.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches the Bearer access token to every request except the public auth endpoints.
 */
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val isAuthEndpoint = path.contains("/api/v1/auth/")

        val token = if (isAuthEndpoint) null else tokenStore.accessTokenBlocking()
        val newRequest = if (!isAuthEndpoint && !token.isNullOrBlank()) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}
