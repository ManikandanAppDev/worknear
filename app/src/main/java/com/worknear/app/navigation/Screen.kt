package com.worknear.app.navigation

object NavArgs {
    const val CATEGORY_ID = "categoryId"
    const val PROFESSIONAL_ID = "professionalId"
    const val BOOKING_ID = "bookingId"
}

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Main : Screen("main")
    object ServiceList : Screen("service_list/{${NavArgs.CATEGORY_ID}}") {
        fun createRoute(categoryId: String) = "service_list/$categoryId"
    }
    object ProfessionalProfile : Screen("professional/{${NavArgs.PROFESSIONAL_ID}}") {
        fun createRoute(professionalId: String) = "professional/$professionalId"
    }
    object BookService : Screen("book_service/{${NavArgs.PROFESSIONAL_ID}}") {
        fun createRoute(professionalId: String) = "book_service/$professionalId"
    }
    object BookingConfirmed : Screen("booking_confirmed/{${NavArgs.BOOKING_ID}}") {
        fun createRoute(bookingId: String) = "booking_confirmed/$bookingId"
    }
    object ChatDetail : Screen("chat/{${NavArgs.PROFESSIONAL_ID}}") {
        fun createRoute(professionalId: String) = "chat/$professionalId"
    }
    object BookingDetail : Screen("booking_detail/{${NavArgs.BOOKING_ID}}") {
        fun createRoute(bookingId: String) = "booking_detail/$bookingId"
    }
}
