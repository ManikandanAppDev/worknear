package com.worknear.app.ui.serviceflow

/** A professional shown on the Best Matches screen (mapped from the backend search results). */
data class MatchProfessional(
    val id: String,
    val name: String,
    val skills: String,
    val experience: String,
    val rating: Double,
    val onTimePercent: Int,
    val startsAt: Int
)
