package com.worknear.app.data.model

import com.worknear.app.R

data class Professional(
    val id: String,
    val name: String,
    val profession: String,
    val rating: Double,
    val reviewCount: Int,
    val startingPrice: Int,
    val imageRes: Int,
    val experienceYears: Int = 5,
    val about: String = "",
    val services: List<String> = emptyList(),
    val isVerified: Boolean = true,
    val categoryId: String = ""
)

object Professionals {
    val all = listOf(
        Professional(
            id = "1",
            name = "Ramesh Kumar",
            profession = "Electrician",
            rating = 4.8,
            reviewCount = 230,
            startingPrice = 299,
            imageRes = R.drawable.avatar_one,
            experienceYears = 8,
            categoryId = "electrician",
            about = "Certified electrician with 8+ years of experience in residential and commercial electrical work. Known for quick response and quality service.",
            services = listOf(
                "House Wiring",
                "Light Installation",
                "Switch & Socket Repair",
                "MCB & Fuse Replacement",
                "Fan Installation"
            )
        ),
        Professional(
            id = "2",
            name = "Suresh Babu",
            profession = "Electrician",
            rating = 4.6,
            reviewCount = 180,
            startingPrice = 249,
            imageRes = R.drawable.avatar_two,
            experienceYears = 6,
            categoryId = "electrician",
            about = "Experienced electrician specializing in home electrical repairs and installations.",
            services = listOf("Wiring", "Light Fitting", "Appliance Repair")
        ),
        Professional(
            id = "3",
            name = "Karthik Raj",
            profession = "Electrician",
            rating = 4.7,
            reviewCount = 156,
            startingPrice = 279,
            imageRes = R.drawable.avatar_three,
            experienceYears = 5,
            categoryId = "electrician",
            about = "Reliable electrician for all your home electrical needs.",
            services = listOf("House Wiring", "Fan Repair", "Switch Repair")
        ),
        Professional(
            id = "4",
            name = "Vijay Prasad",
            profession = "Electrician",
            rating = 4.5,
            reviewCount = 98,
            startingPrice = 199,
            imageRes = R.drawable.avatar_four,
            experienceYears = 4,
            categoryId = "electrician",
            about = "Affordable and quality electrical services.",
            services = listOf("Light Installation", "Socket Repair")
        )
    )

    val default = all.take(1)

    fun byId(id: String): Professional? = all.find { it.id == id }

    fun byCategory(categoryId: String): List<Professional> =
        all.filter { it.categoryId == categoryId }
}
