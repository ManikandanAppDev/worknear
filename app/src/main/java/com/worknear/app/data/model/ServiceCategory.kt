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
        ServiceCategory("ac", "AC", R.drawable.avatar_three),
        ServiceCategory("painting-water-proofing", "Painting & Water-proofing", R.drawable.avatar_four),
        ServiceCategory("carpenter", "Carpenter", R.drawable.avatar_five),
        ServiceCategory("bathroom-cleaning", "Bathroom Cleaning", R.drawable.avatar_one),
        ServiceCategory("kitchen-cleaning", "Kitchen Cleaning", R.drawable.avatar_two),
        ServiceCategory("living-bedroom-cleaning", "Living & Bedroom Cleaning", R.drawable.avatar_three),
        ServiceCategory("full-home-cleaning", "Full Home/By Room Cleaning", R.drawable.avatar_four),
        ServiceCategory("washing-machine", "Washing Machine", R.drawable.avatar_five),
        ServiceCategory("refrigerator", "Refrigerator", R.drawable.avatar_one),
        ServiceCategory("television", "Television", R.drawable.avatar_two),
        ServiceCategory("chimney", "Chimney", R.drawable.avatar_three),
        ServiceCategory("microwave", "Microwave", R.drawable.avatar_four),
        ServiceCategory("stove", "Stove", R.drawable.avatar_five),
        ServiceCategory("laptop", "Laptop", R.drawable.avatar_one),
        ServiceCategory("ro-water-purifier", "RO/Water Purifier", R.drawable.avatar_two),
        ServiceCategory("geyser", "Geyser", R.drawable.avatar_three),
        ServiceCategory("festival-lights-installation", "Festival Lights Installation", R.drawable.avatar_four),
        ServiceCategory("fan-installation", "Fan Installation", R.drawable.avatar_five),
        ServiceCategory("furniture-assembly", "Furniture Assembly", R.drawable.avatar_one),
        ServiceCategory("geyser-service-repair", "Geyser Service & Repair", R.drawable.avatar_two)
    )
}
