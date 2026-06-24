package com.worknear.app.data.remote

import com.worknear.app.data.remote.dto.ApiEnvelope
import com.worknear.app.data.remote.dto.AuthData
import com.worknear.app.data.remote.dto.RefreshBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Synchronous refresh endpoint used by [TokenAuthenticator]. Kept on a separate Retrofit
 * instance (without the auth interceptor/authenticator) to avoid recursive 401 handling.
 */
interface AuthRefreshApi {
    @POST("api/v1/auth/refresh")
    fun refresh(@Body body: RefreshBody): Call<ApiEnvelope<AuthData>>
}
