package com.worknear.app.utils

/** Indian mobile numbers are exactly 10 digits (leading digit 6–9). */
const val MOBILE_DIGIT_LENGTH = 10

/** Keeps only digits and caps at [MOBILE_DIGIT_LENGTH]. */
fun filterMobileInput(raw: String): String =
    raw.filter { it.isDigit() }.take(MOBILE_DIGIT_LENGTH)

/**
 * Validates a 10-digit Indian mobile and returns E.164 (+91…) for the API,
 * or null when the number is incomplete or invalid.
 */
fun normalizeIndianMobile(raw: String): String? {
    val digits = filterMobileInput(raw)
    if (digits.length != MOBILE_DIGIT_LENGTH || digits.first() !in '6'..'9') return null
    return "+91$digits"
}
