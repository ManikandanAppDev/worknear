package com.worknear.app.data.repository

import com.worknear.app.data.model.Booking
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.BookingDto
import com.worknear.app.data.remote.dto.CreateBookingBody
import com.worknear.app.data.remote.safeCall
import com.worknear.app.data.remote.toUiModel

class BookingRepository(private val api: WorkNearApi) {

    suspend fun createBooking(body: CreateBookingBody): ApiResult<BookingDto> =
        safeCall { api.createBooking(body) }

    suspend fun getBooking(bookingId: String): ApiResult<Booking> {
        return when (val r = safeCall { api.booking(bookingId) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.toUiModel())
            is ApiResult.Error -> r
        }
    }

    /** tab: UPCOMING | COMPLETED | CANCELLED */
    suspend fun customerBookings(tab: String): ApiResult<List<Booking>> {
        return when (val r = safeCall { api.customerBookings(tab = tab) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.content.map { it.toUiModel() })
            is ApiResult.Error -> r
        }
    }
}
