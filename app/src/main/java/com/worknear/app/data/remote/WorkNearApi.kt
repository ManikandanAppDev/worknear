package com.worknear.app.data.remote

import com.worknear.app.data.remote.dto.AddressDto
import com.worknear.app.data.remote.dto.AddressRequestBody
import com.worknear.app.data.remote.dto.ApiEnvelope
import com.worknear.app.data.remote.dto.AuthData
import com.worknear.app.data.remote.dto.BannerDto
import com.worknear.app.data.remote.dto.BookingNoteBody
import com.worknear.app.data.remote.dto.BookingDto
import com.worknear.app.data.remote.dto.CancelBookingBody
import com.worknear.app.data.remote.dto.CancelPreviewData
import com.worknear.app.data.remote.dto.CategoryDto
import com.worknear.app.data.remote.dto.ChangePhoneRequestBody
import com.worknear.app.data.remote.dto.ChangeRoleBody
import com.worknear.app.data.remote.dto.ConfirmPhoneChangeBody
import com.worknear.app.data.remote.dto.CreateBookingBody
import com.worknear.app.data.remote.dto.RescheduleBookingBody
import com.worknear.app.data.remote.dto.OfferDto
import com.worknear.app.data.remote.dto.OtpRequestBody
import com.worknear.app.data.remote.dto.OtpRequestData
import com.worknear.app.data.remote.dto.OtpVerifyBody
import com.worknear.app.data.remote.dto.PageDto
import com.worknear.app.data.remote.dto.ProDocumentDto
import com.worknear.app.data.remote.dto.ProProfileDto
import com.worknear.app.data.remote.dto.ProfessionalDetailDto
import com.worknear.app.data.remote.dto.ProfessionalSummaryDto
import com.worknear.app.data.remote.dto.RefreshBody
import com.worknear.app.data.remote.dto.SetProServicesBody
import com.worknear.app.data.remote.dto.UpdateProProfileBody
import com.worknear.app.data.remote.dto.UpdateProfileBody
import com.worknear.app.data.remote.dto.UserDto
import com.worknear.app.data.remote.dto.VerifyCompletionOtpBody
import com.worknear.app.data.remote.dto.WalletDto
import com.worknear.app.data.remote.dto.WalletTransactionDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkNearApi {

    // ----- Auth (public) -----
    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequestBody): Response<ApiEnvelope<OtpRequestData>>

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyBody): Response<ApiEnvelope<AuthData>>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshBody): Response<ApiEnvelope<AuthData>>

    // ----- Onboarding banners (public) -----
    @GET("api/v1/banners")
    suspend fun banners(
        @Query("audience") audience: String = "ALL"
    ): Response<ApiEnvelope<List<BannerDto>>>

    // ----- Catalog -----
    @GET("api/v1/categories")
    suspend fun categories(): Response<ApiEnvelope<List<CategoryDto>>>

    // ----- Offers -----
    @GET("api/v1/offers")
    suspend fun offers(): Response<ApiEnvelope<List<OfferDto>>>

    // ----- Professionals (directory) -----
    @GET("api/v1/professionals")
    suspend fun searchProfessionals(
        @Query("categorySlug") categorySlug: String? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("sort") sort: String = "RECOMMENDED",
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("limit") limit: Int = 50
    ): Response<ApiEnvelope<List<ProfessionalSummaryDto>>>

    @GET("api/v1/professionals/{userId}")
    suspend fun professionalDetail(@Path("userId") userId: String): Response<ApiEnvelope<ProfessionalDetailDto>>

    // ----- Account -----
    @GET("api/v1/me")
    suspend fun me(): Response<ApiEnvelope<UserDto>>

    @PATCH("api/v1/me")
    suspend fun updateProfile(@Body body: UpdateProfileBody): Response<ApiEnvelope<UserDto>>

    @POST("api/v1/me/role")
    suspend fun setRole(@Body body: ChangeRoleBody): Response<ApiEnvelope<AuthData>>

    @POST("api/v1/me/phone/otp/request")
    suspend fun requestPhoneChangeOtp(@Body body: ChangePhoneRequestBody): Response<ApiEnvelope<OtpRequestData>>

    @POST("api/v1/me/phone/verify")
    suspend fun confirmPhoneChange(@Body body: ConfirmPhoneChangeBody): Response<ApiEnvelope<AuthData>>

    @GET("api/v1/me/addresses")
    suspend fun addresses(): Response<ApiEnvelope<List<AddressDto>>>

    @POST("api/v1/me/addresses")
    suspend fun createAddress(@Body body: AddressRequestBody): Response<ApiEnvelope<AddressDto>>

    @PUT("api/v1/me/addresses/{addressId}")
    suspend fun updateAddress(
        @Path("addressId") addressId: String,
        @Body body: AddressRequestBody
    ): Response<ApiEnvelope<AddressDto>>

    @DELETE("api/v1/me/addresses/{addressId}")
    suspend fun deleteAddress(@Path("addressId") addressId: String): Response<ApiEnvelope<Unit>>

    // ----- Bookings -----
    @POST("api/v1/bookings")
    suspend fun createBooking(@Body body: CreateBookingBody): Response<ApiEnvelope<BookingDto>>

    @GET("api/v1/bookings/{bookingId}")
    suspend fun booking(@Path("bookingId") bookingId: String): Response<ApiEnvelope<BookingDto>>

    @GET("api/v1/bookings/{bookingId}/cancel-preview")
    suspend fun cancelPreview(@Path("bookingId") bookingId: String): Response<ApiEnvelope<CancelPreviewData>>

    @POST("api/v1/bookings/{bookingId}/cancel")
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: String,
        @Body body: CancelBookingBody
    ): Response<ApiEnvelope<BookingDto>>

    @POST("api/v1/bookings/{bookingId}/reschedule")
    suspend fun rescheduleBooking(
        @Path("bookingId") bookingId: String,
        @Body body: RescheduleBookingBody
    ): Response<ApiEnvelope<BookingDto>>

    @POST("api/v1/bookings/{bookingId}/accept")
    suspend fun acceptBooking(@Path("bookingId") bookingId: String): Response<ApiEnvelope<BookingDto>>

    @POST("api/v1/bookings/{bookingId}/reject")
    suspend fun rejectBooking(
        @Path("bookingId") bookingId: String,
        @Body body: BookingNoteBody = BookingNoteBody()
    ): Response<ApiEnvelope<BookingDto>>

    @POST("api/v1/bookings/{bookingId}/on-the-way")
    suspend fun markOnTheWay(@Path("bookingId") bookingId: String): Response<ApiEnvelope<BookingDto>>

    @POST("api/v1/bookings/{bookingId}/arrived")
    suspend fun markArrived(@Path("bookingId") bookingId: String): Response<ApiEnvelope<BookingDto>>

    @POST("api/v1/bookings/{bookingId}/start-work")
    suspend fun startWork(@Path("bookingId") bookingId: String): Response<ApiEnvelope<BookingDto>>

    @POST("api/v1/bookings/{bookingId}/mark-work-completed")
    suspend fun markWorkCompleted(@Path("bookingId") bookingId: String): Response<ApiEnvelope<BookingDto>>

    @POST("api/v1/bookings/{bookingId}/verify-completion-otp")
    suspend fun verifyCompletionOtp(
        @Path("bookingId") bookingId: String,
        @Body body: VerifyCompletionOtpBody
    ): Response<ApiEnvelope<BookingDto>>

    @GET("api/v1/bookings/customer")
    suspend fun customerBookings(
        @Query("tab") tab: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiEnvelope<PageDto<BookingDto>>>

    @GET("api/v1/bookings/professional")
    suspend fun professionalBookings(
        @Query("tab") tab: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiEnvelope<PageDto<BookingDto>>>

    // ----- Professional (self) onboarding -----
    @GET("api/v1/pro/profile")
    suspend fun proProfile(): Response<ApiEnvelope<ProProfileDto>>

    @PATCH("api/v1/pro/profile")
    suspend fun updateProProfile(@Body body: UpdateProProfileBody): Response<ApiEnvelope<ProProfileDto>>

    @PUT("api/v1/pro/services")
    suspend fun setProServices(@Body body: SetProServicesBody): Response<ApiEnvelope<ProProfileDto>>

    @Multipart
    @POST("api/v1/pro/documents")
    suspend fun uploadProDocument(
        @Query("type") type: String,
        @Part file: MultipartBody.Part
    ): Response<ApiEnvelope<ProDocumentDto>>

    @POST("api/v1/pro/submit-verification")
    suspend fun submitProVerification(): Response<ApiEnvelope<ProProfileDto>>

    // ----- Wallet -----
    @GET("api/v1/wallet")
    suspend fun wallet(): Response<ApiEnvelope<WalletDto>>

    @GET("api/v1/wallet/transactions")
    suspend fun walletTransactions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): Response<ApiEnvelope<PageDto<WalletTransactionDto>>>
}
