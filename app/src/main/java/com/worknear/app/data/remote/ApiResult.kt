package com.worknear.app.data.remote

import com.google.gson.Gson
import com.worknear.app.data.remote.dto.ApiEnvelope
import com.worknear.app.data.remote.dto.ApiErrorEnvelope
import retrofit2.Response
import java.io.IOException

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val message: String, val code: String? = null, val httpStatus: Int? = null) : ApiResult<Nothing>
}

inline fun <T> ApiResult<T>.onSuccess(block: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) block(data)
    return this
}

inline fun <T> ApiResult<T>.onError(block: (ApiResult.Error) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) block(this)
    return this
}

private val errorGson = Gson()

/**
 * Wraps a Retrofit call that returns an [ApiEnvelope], surfacing transport/HTTP/parse failures
 * as a uniform [ApiResult.Error] with a human-readable message.
 */
suspend fun <T> safeCall(call: suspend () -> Response<ApiEnvelope<T>>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            val data = body?.data
            if (data != null) {
                ApiResult.Success(data)
            } else {
                ApiResult.Error(
                    message = body?.message ?: "Empty response from server",
                    httpStatus = response.code()
                )
            }
        } else {
            val raw = response.errorBody()?.string()
            val parsed = raw?.let {
                runCatching { errorGson.fromJson(it, ApiErrorEnvelope::class.java) }.getOrNull()
            }
            ApiResult.Error(
                message = parsed?.message ?: defaultMessageFor(response.code()),
                code = parsed?.code,
                httpStatus = response.code()
            )
        }
    } catch (e: IOException) {
        ApiResult.Error("Can't reach the server. Check your connection and that the API is running.")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Something went wrong")
    }
}

private fun defaultMessageFor(status: Int): String = when (status) {
    401 -> "Session expired. Please log in again."
    403 -> "You don't have permission to do that."
    404 -> "Not found."
    in 500..599 -> "Server error. Please try again."
    else -> "Request failed ($status)."
}
