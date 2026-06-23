package com.worknear.app.data.model

import com.worknear.app.R

data class UserProfile(
    val name: String,
    val phone: String,
    val imageRes: Int
)

data class ProfileMenuItem(
    val id: String,
    val title: String,
    val badge: String? = null
)

object UserData {
    val profile = UserProfile(
        name = "Rahul Kumar",
        phone = "+91 98765 43210",
        imageRes = R.drawable.avatar_five
    )

    val menuItems = listOf(
        ProfileMenuItem("addresses", "My Addresses"),
        ProfileMenuItem("payments", "Payment Methods"),
        ProfileMenuItem("refer", "Refer & Earn", "Get ₹100"),
        ProfileMenuItem("help", "Help & Support"),
        ProfileMenuItem("settings", "Settings")
    )
}
