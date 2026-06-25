package com.worknear.app.ui.prodashboard

import com.worknear.app.data.model.Booking

data class ProTodaySummary(
    val jobsCompleted: Int = 0,
    val rating: Double = 0.0,
    val earnings: Int = 0
)

sealed interface ProDashboardState {
    data object Loading : ProDashboardState

    data class Blocked(
        val name: String
    ) : ProDashboardState

    data class ProfileIncomplete(
        val name: String
    ) : ProDashboardState

    data class VerificationPending(
        val name: String
    ) : ProDashboardState

    data class VerificationRejected(
        val name: String,
        val reviewNote: String? = null
    ) : ProDashboardState

    data class Verified(
        val name: String,
        val summary: ProTodaySummary,
        val todayJobs: List<Booking>
    ) : ProDashboardState
}
