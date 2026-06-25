package com.worknear.app.navigation

object NavArgs {
    const val CATEGORY_ID = "categoryId"
    const val PROFESSIONAL_ID = "professionalId"
    const val BOOKING_ID = "bookingId"
    const val ADDRESS_ID = "addressId"
}

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object CompleteProfile : Screen("complete_profile")
    object Main : Screen("main")
    object ServiceList : Screen("service_list/{${NavArgs.CATEGORY_ID}}") {
        fun createRoute(categoryId: String) = "service_list/$categoryId"
    }
    object ServiceBuilder : Screen("service_builder/{${NavArgs.CATEGORY_ID}}") {
        fun createRoute(categoryId: String) = "service_builder/$categoryId"
    }
    object AllServices : Screen("all_services")
    object ManageAddresses : Screen("manage_addresses")
    object AddAddress : Screen("add_address?${NavArgs.ADDRESS_ID}={${NavArgs.ADDRESS_ID}}") {
        fun createRoute(addressId: String? = null) =
            if (addressId.isNullOrBlank()) "add_address?${NavArgs.ADDRESS_ID}=" else "add_address?${NavArgs.ADDRESS_ID}=$addressId"
    }
    object Configure : Screen("configure")
    object BestMatches : Screen("best_matches")
    object Schedule : Screen("schedule_booking")
    object ConfirmBooking : Screen("confirm_booking")
    object BookingSuccess : Screen("booking_success")
    object JobStatusFlow : Screen("job_status_flow")
    object Completion : Screen("completion")
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
