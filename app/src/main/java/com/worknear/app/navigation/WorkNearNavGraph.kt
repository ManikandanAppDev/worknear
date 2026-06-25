package com.worknear.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.ui.OnboardingScreen
import com.worknear.app.ui.address.AddAddressScreen
import com.worknear.app.ui.address.ManageAddressesScreen
import com.worknear.app.ui.booking.BookServiceScreen
import com.worknear.app.ui.booking.BookingConfirmedScreen
import com.worknear.app.ui.bookings.BookingDetailScreen
import com.worknear.app.ui.chat.ChatDetailScreen
import com.worknear.app.ui.login.LoginScreen
import com.worknear.app.ui.main.MainScreen
import com.worknear.app.ui.onboarding.CompleteProfileScreen
import com.worknear.app.ui.onboarding.ProfessionalOnboardingScreen
import com.worknear.app.ui.onboarding.RoleSelectScreen
import com.worknear.app.ui.profile.EditProfileScreen
import com.worknear.app.ui.professional.ProfessionalProfileScreen
import com.worknear.app.ui.search.SearchScreen
import com.worknear.app.ui.components.OfflineBanner
import com.worknear.app.ui.servicelist.ServiceListScreen
import com.worknear.app.ui.serviceflow.AllServicesScreen
import com.worknear.app.ui.serviceflow.BestMatchesScreen
import com.worknear.app.ui.serviceflow.BookingSuccessScreen
import com.worknear.app.ui.serviceflow.CompletionScreen
import com.worknear.app.ui.serviceflow.ConfigureScreen
import com.worknear.app.ui.serviceflow.ConfirmBookingScreen
import com.worknear.app.ui.serviceflow.JobStatusScreen
import com.worknear.app.ui.serviceflow.ScheduleBookingScreen
import com.worknear.app.ui.serviceflow.ServiceBuilderScreen
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.utils.rememberIsOnline
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val PROFILE_GATE_TIMEOUT_MS = 4000L

private suspend fun resolveLoggedInStartRoute(app: WorkNearApplication): String {
    val awaitingRole = app.container.tokenStore.isPendingRoleSelection() ||
        (withTimeoutOrNull(PROFILE_GATE_TIMEOUT_MS) {
            app.container.accountRepository.needsRoleSelection()
        } ?: false)
    if (awaitingRole) return Screen.RoleSelect.route

    val me = withTimeoutOrNull(PROFILE_GATE_TIMEOUT_MS) { app.container.accountRepository.getMe() }
    if (me is ApiResult.Success &&
        me.data.role.equals("PROFESSIONAL", ignoreCase = true)
    ) {
        return Screen.Main.route
    }

    val needsCustomer = withTimeoutOrNull(PROFILE_GATE_TIMEOUT_MS) {
        app.container.accountRepository.needsOnboarding()
    } ?: false
    return if (needsCustomer) Screen.CompleteProfile.route else Screen.Main.route
}

@Composable
fun WorkNearNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as WorkNearApplication
    val scope = rememberCoroutineScope()
    val loggedIn by app.container.tokenStore.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val pendingRole by app.container.tokenStore.pendingRoleSelection.collectAsStateWithLifecycle(initialValue = false)

    val sessionState = loggedIn

    // Onboarding gate: a logged-in customer must have a name + at least one saved address.
    // Re-checked on every (re)launch, so closing the app mid-onboarding re-prompts on reopen.
    // Default false so the NavHost is never torn down mid-session (e.g. the login transition).
    var needsProfile by remember { mutableStateOf(false) }
    // Becomes true once we've resolved the initial entry point (used only to hold the splash).
    var startResolved by remember { mutableStateOf(false) }
    // Guards against a stale needsOnboarding() result overwriting a just-completed profile.
    var profileJustCompleted by remember { mutableStateOf(false) }

    // Resolved once per login session; must not change mid-navigation or NavHost resets to home.
    var initialStartRoute by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionState) {
        when (sessionState) {
            null -> return@LaunchedEffect
            false -> {
                profileJustCompleted = false
                needsProfile = false
                initialStartRoute = Screen.Onboarding.route
                startResolved = true
            }
            true -> {
                if (!profileJustCompleted) {
                    val route = resolveLoggedInStartRoute(app)
                    initialStartRoute = route
                    needsProfile = route == Screen.CompleteProfile.route
                } else {
                    initialStartRoute = Screen.Main.route
                }
                startResolved = true
            }
        }
    }

    // Hold a splash until session + the correct entry screen are resolved.
    if (sessionState == null || !startResolved || initialStartRoute == null) {
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

    // Catch logged-in-but-incomplete customers who land on Main — never interrupt role selection.
    LaunchedEffect(needsProfile, sessionState, pendingRole) {
        val current = navController.currentDestination?.route
        val onPostLoginFlow = current == Screen.RoleSelect.route ||
            current == Screen.ProfessionalOnboarding.route ||
            current == Screen.CompleteProfile.route ||
            current == Screen.Login.route
        if (sessionState == true && needsProfile && !profileJustCompleted && !pendingRole && !onPostLoginFlow &&
            current == Screen.Main.route
        ) {
            navController.navigate(Screen.CompleteProfile.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
    }

    val isOnline by rememberIsOnline()

    Column(modifier = Modifier.fillMaxSize()) {
        OfflineBanner(visible = !isOnline)

        NavHost(
            modifier = Modifier.weight(1f),
            navController = navController,
            startDestination = initialStartRoute ?: Screen.Onboarding.route,
            // App-wide push/pop transitions: new screens slide in from the right with a fade,
            // the previous screen parallaxes out to the left (and vice-versa on back).
            enterTransition = {
                slideInHorizontally(animationSpec = tween(320)) { it } + fadeIn(animationSpec = tween(320))
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(320)) { -it / 3 } + fadeOut(animationSpec = tween(320))
            },
            popEnterTransition = {
                slideInHorizontally(animationSpec = tween(320)) { -it / 3 } + fadeIn(animationSpec = tween(320))
            },
            popExitTransition = {
                slideOutHorizontally(animationSpec = tween(320)) { it } + fadeOut(animationSpec = tween(320))
            }
        ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = { _, _ ->
                    scope.launch {
                        val destination = resolveLoggedInStartRoute(app)
                        needsProfile = destination == Screen.CompleteProfile.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.RoleSelect.route) {
            RoleSelectScreen(
                onCustomer = {
                    scope.launch {
                        navController.navigate(Screen.CompleteProfile.route)
                    }
                },
                onProfessional = {
                    scope.launch {
                        app.container.tokenStore.setPendingRoleSelection(true)
                        navController.navigate(Screen.ProfessionalOnboarding.route)
                    }
                }
            )
        }

        composable(Screen.ProfessionalOnboarding.route) {
            ProfessionalOnboardingScreen(
                onNavigateBack = { navController.popBackStack() },
                onCompleted = {
                    profileJustCompleted = true
                    needsProfile = false
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CompleteProfile.route) {
            CompleteProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onCompleted = {
                    profileJustCompleted = true
                    needsProfile = false
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToServiceList = { categoryId ->
                    navController.navigate(Screen.ServiceList.createRoute(categoryId))
                },
                onNavigateToServiceBuilder = { categoryId ->
                    navController.navigate(Screen.ServiceBuilder.createRoute(categoryId))
                },
                onNavigateToAllServices = {
                    navController.navigate(Screen.AllServices.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToAddAddress = {
                    navController.navigate(Screen.AddAddress.createRoute())
                },
                onNavigateToManageAddresses = {
                    navController.navigate(Screen.ManageAddresses.route)
                },
                onNavigateToProfessional = { professionalId ->
                    navController.navigate(Screen.ProfessionalProfile.createRoute(professionalId))
                },
                onNavigateToBookService = { professionalId ->
                    navController.navigate(Screen.BookService.createRoute(professionalId))
                },
                onNavigateToBookingDetail = { bookingUuid ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingUuid))
                },
                onLogout = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProfessionalOnboarding = {
                    navController.navigate(Screen.ProfessionalOnboarding.route)
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
            route = Screen.ServiceBuilder.route,
            arguments = listOf(navArgument(NavArgs.CATEGORY_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString(NavArgs.CATEGORY_ID) ?: "electrician"
            ServiceBuilderScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.popBackStack() },
                onConfigure = { navController.navigate(Screen.Configure.route) }
            )
        }

        composable(Screen.AllServices.route) {
            AllServicesScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenService = { categoryId ->
                    navController.navigate(Screen.ServiceBuilder.createRoute(categoryId))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenService = { categoryId ->
                    navController.navigate(Screen.ServiceBuilder.createRoute(categoryId))
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageAddresses.route) {
            ManageAddressesScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditAddress = { addressId ->
                    navController.navigate(Screen.AddAddress.createRoute(addressId))
                },
                onAddNew = { navController.navigate(Screen.AddAddress.createRoute()) }
            )
        }

        composable(
            route = Screen.AddAddress.route,
            arguments = listOf(navArgument(NavArgs.ADDRESS_ID) {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val addressId = backStackEntry.arguments?.getString(NavArgs.ADDRESS_ID)?.takeIf { it.isNotBlank() }
            AddAddressScreen(
                addressId = addressId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Configure.route) {
            ConfigureScreen(
                onNavigateBack = { navController.popBackStack() },
                onSeeMatches = { navController.navigate(Screen.BestMatches.route) }
            )
        }

        composable(Screen.BestMatches.route) {
            BestMatchesScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinue = { navController.navigate(Screen.Schedule.route) }
            )
        }

        composable(Screen.Schedule.route) {
            ScheduleBookingScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinue = { navController.navigate(Screen.ConfirmBooking.route) },
                onAddAddress = { navController.navigate(Screen.AddAddress.createRoute()) }
            )
        }

        composable(Screen.ConfirmBooking.route) {
            ConfirmBookingScreen(
                onNavigateBack = { navController.popBackStack() },
                onConfirm = { navController.navigate(Screen.BookingSuccess.route) }
            )
        }

        composable(Screen.BookingSuccess.route) {
            BookingSuccessScreen(
                onBackToHome = {
                    com.worknear.app.ui.serviceflow.ServiceFlowState.reset()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.JobStatusFlow.route) {
            JobStatusScreen(
                onNavigateBack = { navController.popBackStack() },
                onConfirmCompletion = { navController.navigate(Screen.Completion.route) }
            )
        }

        composable(Screen.Completion.route) {
            CompletionScreen(
                onDone = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
        }
    }
}
