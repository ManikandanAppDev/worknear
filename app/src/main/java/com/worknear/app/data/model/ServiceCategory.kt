package com.worknear.app.data.model

import com.worknear.app.R

data class ServiceCategory(
    val id: String,
    val title: String,
    val imageRes: Int
)

object ServiceCategories {
    val default = listOf(
        ServiceCategory("electrician", "Electrician", R.drawable.avatar_one),
        ServiceCategory("plumber", "Plumber", R.drawable.avatar_two),
        ServiceCategory("ac_repair", "AC Repair", R.drawable.avatar_three),
        ServiceCategory("painter", "Painter", R.drawable.avatar_four)
    )
}
