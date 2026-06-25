package com.worknear.app.data.repository

import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.CancelPreview
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.WorkNearApi
import com.worknear.app.data.remote.dto.BookingNoteBody
import com.worknear.app.data.remote.dto.BookingDto
import com.worknear.app.data.remote.dto.CancelBookingBody
import com.worknear.app.data.remote.dto.CancelPreviewData
import com.worknear.app.data.remote.dto.CreateBookingBody
import com.worknear.app.data.remote.dto.RescheduleBookingBody
import com.worknear.app.data.remote.dto.VerifyCompletionOtpBody
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

    suspend fun cancelPreview(bookingId: String): ApiResult<CancelPreview> {
        return when (val r = safeCall { api.cancelPreview(bookingId) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.toUi())
            is ApiResult.Error -> r
        }
    }

    suspend fun cancelBooking(
        bookingId: String,
        reasonCode: String,
        comment: String?
    ): ApiResult<Booking> {
        val body = CancelBookingBody(reasonCode = reasonCode, comment = comment)
        return when (val r = safeCall { api.cancelBooking(bookingId, body) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.toUiModel())
            is ApiResult.Error -> r
        }
    }

    suspend fun rescheduleBooking(
        bookingId: String,
        scheduledDate: String,
        slotStart: String,
        slotEnd: String
    ): ApiResult<Booking> {
        val body = RescheduleBookingBody(scheduledDate, slotStart, slotEnd)
        return when (val r = safeCall { api.rescheduleBooking(bookingId, body) }) {
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

    /** tab: REQUESTS | ACTIVE | COMPLETED */
    suspend fun professionalBookings(tab: String): ApiResult<List<Booking>> {
        return when (val r = safeCall { api.professionalBookings(tab = tab) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.content.map { it.toUiModel() })
            is ApiResult.Error -> r
        }
    }

    suspend fun markOnTheWay(bookingId: String): ApiResult<Booking> =
        mapBookingResult { api.markOnTheWay(bookingId) }

    suspend fun acceptBooking(bookingId: String): ApiResult<Booking> =
        mapBookingResult { api.acceptBooking(bookingId) }

    suspend fun rejectBooking(bookingId: String, note: String? = null): ApiResult<Booking> =
        mapBookingResult { api.rejectBooking(bookingId, BookingNoteBody(note)) }

    suspend fun markArrived(bookingId: String): ApiResult<Booking> =
        mapBookingResult { api.markArrived(bookingId) }

    suspend fun startWork(bookingId: String): ApiResult<Booking> =
        mapBookingResult { api.startWork(bookingId) }

    suspend fun markWorkCompleted(bookingId: String): ApiResult<Booking> =
        mapBookingResult { api.markWorkCompleted(bookingId) }

    suspend fun verifyCompletionOtp(bookingId: String, otp: String): ApiResult<Booking> =
        mapBookingResult { api.verifyCompletionOtp(bookingId, VerifyCompletionOtpBody(otp)) }

    private suspend fun mapBookingResult(
        call: suspend () -> retrofit2.Response<com.worknear.app.data.remote.dto.ApiEnvelope<BookingDto>>
    ): ApiResult<Booking> {
        return when (val r = safeCall { call() }) {
            is ApiResult.Success -> ApiResult.Success(r.data.toUiModel())
            is ApiResult.Error -> r
        }
    }
}

private fun CancelPreviewData.toUi() = CancelPreview(
    canCancel = canCancel,
    feeApplies = feeApplies,
    feeAmount = feeAmount.toInt(),
    message = message.orEmpty(),
    lateCancel = lateCancel,
    freeLateCancelsUsed = freeLateCancelsUsedThisMonth,
    freeLateCancelsLimit = freeLateCancelsLimit,
    freeLateCancelsRemaining = freeLateCancelsRemaining,
    walletBalance = walletBalance.toInt(),
    sufficientWalletBalance = sufficientWalletBalance
)
