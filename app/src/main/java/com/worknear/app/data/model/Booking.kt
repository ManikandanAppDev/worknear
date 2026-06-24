package com.worknear.app.data.model

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    ON_THE_WAY,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class BookingTab {
    UPCOMING,
    COMPLETED,
    CANCELLED
}

enum class CancellationReasonCode(val label: String) {
    PRO_UNAVAILABLE("Professional unavailable"),
    CHANGE_OF_PLANS("Change of plans"),
    BOOKING_MISTAKE("Booked by mistake"),
    SERVICE_NOT_NEEDED("Service no longer needed"),
    OTHER("Other")
}

data class Booking(
    val uuid: String,
    val id: String,
    val serviceName: String,
    val professionalName: String,
    val professionalId: String,
    val date: String,
    val rawDate: String,
    val time: String,
    val slotStart: String,
    val slotEnd: String,
    val address: String,
    val price: Int,
    val status: BookingStatus,
    val imageRes: Int,
    val rescheduleCount: Int = 0,
    val rescheduleMax: Int = 2,
    val canReschedule: Boolean = false,
    val canCancel: Boolean = false
)

data class CancelPreview(
    val canCancel: Boolean,
    val feeApplies: Boolean,
    val feeAmount: Int,
    val message: String,
    val lateCancel: Boolean,
    val freeLateCancelsUsed: Int,
    val freeLateCancelsLimit: Int,
    val freeLateCancelsRemaining: Int,
    val walletBalance: Int,
    val sufficientWalletBalance: Boolean
)
