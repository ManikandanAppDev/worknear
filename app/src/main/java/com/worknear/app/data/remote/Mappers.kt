package com.worknear.app.data.remote

import com.worknear.app.R
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.BookingStatus
import com.worknear.app.data.model.Professional
import com.worknear.app.data.model.ServiceCategory
import com.worknear.app.data.model.Transaction
import com.worknear.app.data.model.TransactionType
import com.worknear.app.data.remote.dto.BookingDto
import com.worknear.app.data.remote.dto.CategoryDto
import com.worknear.app.data.remote.dto.ProfessionalDetailDto
import com.worknear.app.data.remote.dto.ProfessionalSummaryDto
import com.worknear.app.data.remote.dto.WalletTransactionDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/** The app ships a small set of avatar drawables; pick one deterministically until real images are wired. */
private val AVATARS = intArrayOf(
    R.drawable.avatar_one,
    R.drawable.avatar_two,
    R.drawable.avatar_three,
    R.drawable.avatar_four,
    R.drawable.avatar_five
)

private fun avatarFor(key: String?): Int {
    if (key.isNullOrEmpty()) return AVATARS[0]
    return AVATARS[abs(key.hashCode()) % AVATARS.size]
}

private val dateOut = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

private fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return runCatching { LocalDate.parse(raw).format(dateOut) }.getOrElse { raw }
}

private fun formatInstant(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return runCatching { Instant.parse(raw).atZone(ZoneId.systemDefault()).toLocalDate().format(dateOut) }
        .getOrElse { raw }
}

private fun formatTimeRange(start: String?, end: String?): String {
    val s = start?.take(5).orEmpty()
    val e = end?.take(5).orEmpty()
    return when {
        s.isNotEmpty() && e.isNotEmpty() -> "$s - $e"
        s.isNotEmpty() -> s
        else -> ""
    }
}

fun CategoryDto.toUiModel(): ServiceCategory = ServiceCategory(
    id = slug ?: id.orEmpty(),
    title = name.orEmpty(),
    imageRes = avatarFor(slug ?: id)
)

fun ProfessionalSummaryDto.toUiModel(professionLabel: String, categorySlug: String = ""): Professional =
    Professional(
        id = userId.orEmpty(),
        name = name.orEmpty(),
        profession = professionLabel,
        rating = rating,
        reviewCount = ratingCount,
        startingPrice = price.toInt(),
        imageRes = avatarFor(userId),
        experienceYears = experienceYears,
        isVerified = verified,
        categoryId = categorySlug
    )

fun ProfessionalDetailDto.toUiModel(): Professional {
    val primary = services.firstOrNull { it.active } ?: services.firstOrNull()
    val startingPrice = services.filter { it.active }
        .minOfOrNull { it.basePrice }
        ?: primary?.basePrice
        ?: 0.0
    return Professional(
        id = userId.orEmpty(),
        name = name.orEmpty(),
        profession = primary?.categoryName ?: "Professional",
        rating = rating,
        reviewCount = ratingCount,
        startingPrice = startingPrice.toInt(),
        imageRes = avatarFor(userId),
        experienceYears = experienceYears,
        about = bio.orEmpty(),
        services = specializations,
        isVerified = verificationStatus.equals("APPROVED", ignoreCase = true),
        categoryId = primary?.categorySlug.orEmpty()
    )
}

fun BookingDto.toUiModel(): Booking = Booking(
    uuid = id.orEmpty(),
    id = code ?: id.orEmpty(),
    serviceName = categoryName ?: "Service",
    professionalName = professionalName ?: "Professional",
    professionalId = professionalId.orEmpty(),
    date = formatDate(scheduledDate),
    rawDate = scheduledDate.orEmpty(),
    time = formatTimeRange(slotStart, slotEnd),
    slotStart = slotStart.orEmpty(),
    slotEnd = slotEnd.orEmpty(),
    address = listOfNotNull(addressLine?.takeIf { it.isNotBlank() }, city?.takeIf { it.isNotBlank() })
        .joinToString(", "),
    price = amount.toInt(),
    status = mapBookingStatus(status),
    displayStatus = displayStatus.orEmpty(),
    lockedAmount = lockedAmount.toInt(),
    completionOtp = completionOtp,
    completionOtpExpiresAt = completionOtpExpiresAt,
    confirmedAt = confirmedAt,
    onTheWayAt = onTheWayAt,
    arrivedAt = arrivedAt,
    workStartedAt = workStartedAt,
    workCompletedAt = workCompletedAt,
    completedAt = completedAt,
    paymentReleasedAt = paymentReleasedAt,
    imageRes = avatarFor(professionalId),
    rescheduleCount = rescheduleCount,
    rescheduleMax = rescheduleMax,
    canReschedule = canReschedule,
    canCancel = canCancel
)

private fun mapBookingStatus(raw: String?): BookingStatus = when (raw?.uppercase(Locale.ENGLISH)) {
    "PENDING" -> BookingStatus.PENDING
    "CONFIRMED" -> BookingStatus.CONFIRMED
    "ON_THE_WAY" -> BookingStatus.ON_THE_WAY
    "ARRIVED" -> BookingStatus.ARRIVED
    "IN_PROGRESS" -> BookingStatus.IN_PROGRESS
    "COMPLETED_PENDING_OTP" -> BookingStatus.COMPLETED_PENDING_OTP
    "COMPLETED" -> BookingStatus.COMPLETED
    "CANCELLED", "CANCELED", "REJECTED" -> BookingStatus.CANCELLED
    else -> BookingStatus.CONFIRMED
}

fun WalletTransactionDto.toUiModel(): Transaction = Transaction(
    id = id.orEmpty(),
    title = description?.takeIf { it.isNotBlank() } ?: (reason ?: "Transaction"),
    date = formatInstant(createdAt),
    amount = amount,
    type = if (type.equals("CREDIT", ignoreCase = true)) TransactionType.CREDIT else TransactionType.DEBIT
)
