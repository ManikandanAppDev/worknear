package com.worknear.app.data.model

data class BookServiceData(
    val serviceName: String = "Electrician",
    val professionalName: String = "Ramesh Kumar",
    val professionalId: String = "1",
    val date: String = "24 Jun 2026",
    val timeSlot: String = "10:00 AM - 12:00 PM",
    val problemDescription: String = "Kitchen light not working, need urgent repair.",
    val price: Int = 299
)
