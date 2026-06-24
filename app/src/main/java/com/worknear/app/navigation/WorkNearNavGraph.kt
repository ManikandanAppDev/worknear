package com.worknear.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.worknear.app.WorkNearApplication
import com.worknear.app.ui.OnboardingScreen
import com.worknear.app.ui.booking.BookServiceScreen
import com.worknear.app.ui.booking.BookingConfirmedScreen
import com.worknear.app.ui.bookings.BookingDetailScreen
import com.worknear.app.ui.chat.ChatDetailScreen
import com.worknear.app.ui.login.LoginScreen
import com.worknear.app.ui.main.MainScreen
import com.worknear.app.ui.professional.ProfessionalProfileScreen
import com.worknear.app.ui.components.OfflineBanner
import com.worknear.app.ui.servicelist.ServiceListScreen
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.utils.rememberIsOnline

@Composable
fun WorkNearNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as WorkNearApplication
    val loggedIn by app.container.tokenStore.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)

    // Wait until we know whether a session exists, then pick the entry point.
    val sessionState = loggedIn
    if (sessionState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    // If the session is cleared while in-app (e.g. token refresh failed), route back to onboarding.
    LaunchedEffect(sessionState) {
        if (!sessionState) {
            val current = navController.currentDestination?.route
            if (current != null && current != Screen.Onboarding.route && current != Screen.Login.route) {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val isOnline by rememberIsOnline()

    Column(modifier = Modifier.fillMaxSize()) {
        OfflineBanner(visible = !isOnline)

        NavHost(
            modifier = Modifier.weight(1f),
            navController = navController,
            startDestination = if (sessionState) Screen.Main.route else Screen.Onboarding.route
        ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToServiceList = { categoryId ->
                    navController.navigate(Screen.ServiceList.createRoute(categoryId))
                },
                onNavigateToProfessional = { professionalId ->
                    navController.navigate(Screen.ProfessionalProfile.createRoute(professionalId))
                },
                onNavigateToBookService = { professionalId ->
                    navController.navigate(Screen.BookService.createRoute(professionalId))
                },
                onNavigateToChat = { professionalId ->
                    navController.navigate(Screen.ChatDetail.createRoute(professionalId))
                },
                onNavigateToBookingDetail = { bookingUuid ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingUuid))
                },
                onLogout = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ServiceList.route,
            arguments = listOf(navArgument(NavArgs.CATEGORY_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString(NavArgs.CATEGORY_ID) ?: "electrician"
            ServiceListScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.popBackStack() },
                onProfessionalClick = { id ->
                    navController.navigate(Screen.ProfessionalProfile.createRoute(id))
                },
                onBookNow = { id ->
                    navController.navigate(Screen.BookService.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.ProfessionalProfile.route,
            arguments = listOf(navArgument(NavArgs.PROFESSIONAL_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString(NavArgs.PROFESSIONAL_ID) ?: "1"
            ProfessionalProfileScreen(
                professionalId = professionalId,
                onNavigateBack = { navController.popBackStack() },
                onBookNow = { id ->
                    navController.navigate(Screen.BookService.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.BookService.route,
            arguments = listOf(navArgument(NavArgs.PROFESSIONAL_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString(NavArgs.PROFESSIONAL_ID) ?: "1"
            BookServiceScreen(
                professionalId = professionalId,
                onNavigateBack = { navController.popBackStack() },
                onContinue = { bookingId ->
                    navController.navigate(Screen.BookingConfirmed.createRoute(bookingId)) {
                        popUpTo(Screen.Main.route)
                    }
                }
            )
        }

        composable(
            route = Screen.BookingConfirmed.route,
            arguments = listOf(navArgument(NavArgs.BOOKING_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString(NavArgs.BOOKING_ID) ?: "BK12345"
            BookingConfirmedScreen(
                bookingId = bookingId,
                onChat = { professionalId ->
                    navController.navigate(Screen.ChatDetail.createRoute(professionalId))
                },
                onTrackBooking = {
                    navController.popBackStack(Screen.Main.route, inclusive = false)
                }
            )
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(navArgument(NavArgs.PROFESSIONAL_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val professionalId = backStackEntry.arguments?.getString(NavArgs.PROFESSIONAL_ID) ?: "1"
            ChatDetailScreen(
                professionalId = professionalId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BookingDetail.route,
            arguments = listOf(navArgument(NavArgs.BOOKING_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingUuid = backStackEntry.arguments?.getString(NavArgs.BOOKING_ID).orEmpty()
            BookingDetailScreen(
                bookingUuid = bookingUuid,
                onNavigateBack = { navController.popBackStack() },
                onChat = { professionalId ->
                    navController.navigate(Screen.ChatDetail.createRoute(professionalId))
                }
            )
        }
        }
    }
}
