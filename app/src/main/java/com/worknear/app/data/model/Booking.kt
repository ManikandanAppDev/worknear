package com.worknear.app.data.model

enum class BookingStatus {
    CONFIRMED,
    COMPLETED,
    CANCELLED
}

enum class BookingTab {
    UPCOMING,
    COMPLETED,
    CANCELLED
}

data class Booking(
    val id: String,
    val serviceName: String,
    val professionalName: String,
    val professionalId: String,
    val date: String,
    val time: String,
    val address: String,
    val price: Int,
    val status: BookingStatus,
    val imageRes: Int
)

object Bookings {
    val all = listOf(
        Booking(
            id = "BK12345",
            serviceName = "Electrician",
            professionalName = "Ramesh Kumar",
            professionalId = "1",
            date = "24 Jun 2026",
            time = "10:00 AM - 12:00 PM",
            address = "42, 2nd Street, Anna Nagar, Chennai - 600040",
            price = 299,
            status = BookingStatus.CONFIRMED,
            imageRes = com.worknear.app.R.drawable.avatar_one
        ),
        Booking(
            id = "BK12346",
            serviceName = "Plumber",
            professionalName = "Suresh Babu",
            professionalId = "2",
            date = "28 Jun 2026",
            time = "2:00 PM - 4:00 PM",
            address = "42, 2nd Street, Anna Nagar, Chennai - 600040",
            price = 349,
            status = BookingStatus.CONFIRMED,
            imageRes = com.worknear.app.R.drawable.avatar_two
        ),
        Booking(
            id = "BK12300",
            serviceName = "AC Repair",
            professionalName = "Karthik Raj",
            professionalId = "3",
            date = "10 Jun 2026",
            time = "11:00 AM - 1:00 PM",
            address = "42, 2nd Street, Anna Nagar, Chennai - 600040",
            price = 499,
            status = BookingStatus.COMPLETED,
            imageRes = com.worknear.app.R.drawable.avatar_three
        ),
        Booking(
            id = "BK12290",
            serviceName = "Painter",
            professionalName = "Vijay Prasad",
            professionalId = "4",
            date = "05 Jun 2026",
            time = "9:00 AM - 11:00 AM",
            address = "42, 2nd Street, Anna Nagar, Chennai - 600040",
            price = 599,
            status = BookingStatus.CANCELLED,
            imageRes = com.worknear.app.R.drawable.avatar_four
        )
    )

    fun byId(id: String): Booking? = all.find { it.id == id }

    fun byStatus(status: BookingStatus): List<Booking> = all.filter { it.status == status }
}
