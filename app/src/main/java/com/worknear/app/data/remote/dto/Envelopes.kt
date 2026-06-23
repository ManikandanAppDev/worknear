package com.worknear.app.data.remote.dto

/** Mirrors the backend's uniform success envelope: { success, data, message, timestamp }. */
data class ApiEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null,
    val timestamp: String? = null
)

/** Mirrors the backend's flat error envelope: { success, code, message, errors[], path, timestamp }. */
data class ApiErrorEnvelope(
    val success: Boolean = false,
    val code: String? = null,
    val message: String? = null,
    val errors: List<FieldErrorDto>? = null,
    val path: String? = null,
    val timestamp: String? = null
)

data class FieldErrorDto(
    val field: String? = null,
    val message: String? = null
)

/** Mirrors PageResponse<T>. */
data class PageDto<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)
