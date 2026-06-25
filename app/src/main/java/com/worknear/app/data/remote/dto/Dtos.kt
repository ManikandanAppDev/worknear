package com.worknear.app.data.remote.dto

// ---------- Auth ----------

data class OtpRequestBody(
    val phone: String,
    val role: String? = null
)

data class OtpRequestData(
    val phone: String? = null,
    val otpSent: Boolean = false,
    val devCode: String? = null
)

data class OtpVerifyBody(
    val phone: String,
    val code: String,
    val role: String? = null
)

data class RefreshBody(
    val refreshToken: String
)

data class AuthData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenType: String? = null,
    val expiresIn: Long = 0,
    val newUser: Boolean = false,
    val user: UserDto? = null
)

data class UserDto(
    val id: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val status: String? = null,
    val roleConfirmed: Boolean = false
)

data class UpdateProfileBody(
    val fullName: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null
)

data class ChangePhoneRequestBody(
    val phone: String
)

data class ConfirmPhoneChangeBody(
    val phone: String,
    val code: String
)

data class ChangeRoleBody(
    val role: String
)

// ---------- Onboarding banners ----------

data class BannerDto(
    val id: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val ctaLabel: String? = null,
    val audience: String? = null,
    val sortOrder: Int = 0,
    val active: Boolean = true
)

// ---------- Catalog ----------

data class CategoryDto(
    val id: String? = null,
    val slug: String? = null,
    val name: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val basePrice: Double = 0.0
)

// ---------- Professionals ----------

data class ProfessionalSummaryDto(
    val userId: String? = null,
    val profileId: String? = null,
    val name: String? = null,
    val avatarUrl: String? = null,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val experienceYears: Int = 0,
    val verified: Boolean = false,
    val online: Boolean = false,
    val price: Double = 0.0,
    val distanceKm: Double? = null
)

data class ProfessionalServiceDto(
    val categoryId: String? = null,
    val categorySlug: String? = null,
    val categoryName: String? = null,
    val basePrice: Double = 0.0,
    val active: Boolean = true
)

data class AvailabilityDto(
    val dayOfWeek: Int = 0,
    val startTime: String? = null,
    val endTime: String? = null,
    val available: Boolean = true
)

data class ProfessionalDetailDto(
    val userId: String? = null,
    val profileId: String? = null,
    val name: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val experienceYears: Int = 0,
    val serviceRadiusKm: Int = 0,
    val city: String? = null,
    val area: String? = null,
    val languages: String? = null,
    val online: Boolean = false,
    val verificationStatus: String? = null,
    val jobsCompleted: Int = 0,
    val specializations: List<String> = emptyList(),
    val services: List<ProfessionalServiceDto> = emptyList(),
    val availability: List<AvailabilityDto> = emptyList()
)

// ---------- Offers ----------

data class OfferDto(
    val id: String? = null,
    val code: String? = null,
    val title: String? = null,
    val description: String? = null,
    val discountType: String? = null,
    val discountValue: Double = 0.0,
    val maxDiscount: Double? = null,
    val minOrder: Double? = null
)

// ---------- Bookings ----------

data class CreateBookingBody(
    val professionalId: String,
    val categoryId: String,
    val scheduledDate: String,      // yyyy-MM-dd
    val slotStart: String,          // HH:mm
    val slotEnd: String,            // HH:mm
    val addressId: String? = null,
    val addressLine: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val problemDescription: String? = null,
    val paymentMethod: String? = null
)

data class CancelBookingBody(
    val reasonCode: String,
    val comment: String? = null
)

data class CancelPreviewData(
    val canCancel: Boolean = false,
    val feeApplies: Boolean = false,
    val feeAmount: Double = 0.0,
    val feeReason: String? = null,
    val lateCancel: Boolean = false,
    val hoursUntilSlot: Double = 0.0,
    val freeLateCancelsUsedThisMonth: Int = 0,
    val freeLateCancelsLimit: Int = 3,
    val freeLateCancelsRemaining: Int = 0,
    val walletBalance: Double = 0.0,
    val sufficientWalletBalance: Boolean = true,
    val message: String? = null
)

data class RescheduleBookingBody(
    val scheduledDate: String,      // yyyy-MM-dd
    val slotStart: String,          // HH:mm
    val slotEnd: String             // HH:mm
)

data class VerifyCompletionOtpBody(
    val otp: String
)

data class BookingPhotoDto(
    val id: String? = null,
    val type: String? = null,
    val fileUrl: String? = null
)

data class BookingDto(
    val id: String? = null,
    val code: String? = null,
    val customerId: String? = null,
    val professionalId: String? = null,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val professionalName: String? = null,
    val professionalPhone: String? = null,
    val scheduledDate: String? = null,
    val slotStart: String? = null,
    val slotEnd: String? = null,
    val addressLine: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val problemDescription: String? = null,
    val status: String? = null,
    val displayStatus: String? = null,
    val amount: Double = 0.0,
    val commission: Double = 0.0,
    val proEarning: Double = 0.0,
    val lockedAmount: Double = 0.0,
    val paymentMethod: String? = null,
    val confirmedAt: String? = null,
    val onTheWayAt: String? = null,
    val arrivedAt: String? = null,
    val workStartedAt: String? = null,
    val workCompletedAt: String? = null,
    val completedAt: String? = null,
    val paymentReleasedAt: String? = null,
    val completionOtp: String? = null,
    val completionOtpExpiresAt: String? = null,
    val createdAt: String? = null,
    val photos: List<BookingPhotoDto> = emptyList(),
    val rescheduleCount: Int = 0,
    val rescheduleMax: Int = 2,
    val canReschedule: Boolean = false,
    val canCancel: Boolean = false
)

// ---------- Wallet ----------

data class WalletDto(
    val balance: Double = 0.0,
    val currency: String? = null
)

data class WalletTransactionDto(
    val id: String? = null,
    val type: String? = null,
    val reason: String? = null,
    val amount: Double = 0.0,
    val balanceAfter: Double = 0.0,
    val description: String? = null,
    val createdAt: String? = null
)

// ---------- Addresses ----------

data class AddressDto(
    val id: String? = null,
    val label: String? = null,
    val line1: String? = null,
    val line2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isDefault: Boolean = false
)

data class AddressRequestBody(
    val label: String? = null,
    val line1: String,
    val line2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val makeDefault: Boolean = false
)

// ---------- Professional (self) onboarding ----------

data class UpdateProProfileBody(
    val bio: String? = null,
    val experienceYears: Int? = null,
    val serviceRadiusKm: Int? = null,
    val city: String? = null,
    val area: String? = null,
    val baseLatitude: Double? = null,
    val baseLongitude: Double? = null,
    val languages: String? = null
)

data class SetProServicesBody(
    val services: List<ProServiceItemBody>
)

data class ProServiceItemBody(
    val categoryId: String,
    val basePrice: Double
)

data class ProDocumentDto(
    val id: String? = null,
    val type: String? = null,
    val fileUrl: String? = null,
    val originalName: String? = null,
    val status: String? = null,
    val reviewNote: String? = null
)

data class ProProfileDto(
    val userId: String? = null,
    val profileId: String? = null,
    val name: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val experienceYears: Int = 0,
    val serviceRadiusKm: Int = 0,
    val city: String? = null,
    val area: String? = null,
    val languages: String? = null,
    val verificationStatus: String? = null,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val jobsCompleted: Int = 0,
    val services: List<ProfessionalServiceDto> = emptyList(),
    val documents: List<ProDocumentDto> = emptyList()
)
