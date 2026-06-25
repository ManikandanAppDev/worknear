package com.worknear.app.ui.serviceflow

/** A professional shown on the Best Matches screen (sample data for the booking flow). */
data class MatchProfessional(
    val id: String,
    val name: String,
    val skills: String,
    val experience: String,
    val rating: Double,
    val onTimePercent: Int,
    val startsAt: Int
)

object MatchData {
    val professionals = listOf(
        MatchProfessional(
            id = "ravi",
            name = "Ravi Kumar",
            skills = "Electrical · Plumbing",
            experience = "8+ years experience",
            rating = 4.8,
            onTimePercent = 98,
            startsAt = 299
        ),
        MatchProfessional(
            id = "suresh",
            name = "Suresh R.",
            skills = "Electrical",
            experience = "5+ years experience",
            rating = 4.6,
            onTimePercent = 95,
            startsAt = 349
        ),
        MatchProfessional(
            id = "manoj",
            name = "Manoj Electricals",
            skills = "Electrical",
            experience = "10+ years experience",
            rating = 4.5,
            onTimePercent = 93,
            startsAt = 279
        ),
        MatchProfessional(
            id = "vijay",
            name = "Vijay Services",
            skills = "Electrical · Plumbing",
            experience = "6+ years experience",
            rating = 4.4,
            onTimePercent = 91,
            startsAt = 329
        )
    )
}
