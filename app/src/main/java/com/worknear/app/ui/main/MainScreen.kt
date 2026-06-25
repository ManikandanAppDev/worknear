package com.worknear.app.ui.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.worknear.app.ui.components.WorkNearExitDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.WorkNearApplication
import com.worknear.app.data.model.ServiceCategory
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.bookings.MyBookingsScreen
import com.worknear.app.ui.home.HomeTabContent
import com.worknear.app.ui.prodashboard.ProMainScreen
import com.worknear.app.ui.profile.UserProfileScreen
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.ui.wallet.WalletScreen

enum class MainTab {
    HOME,
    BOOKINGS,
    WALLET,
    PROFILE
}

@Composable
fun MainScreen(
    onNavigateToServiceList: (String) -> Unit,
    onNavigateToServiceBuilder: (String) -> Unit,
    onNavigateToAllServices: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToAddAddress: () -> Unit = {},
    onNavigateToManageAddresses: () -> Unit = {},
    onNavigateToProfessional: (String) -> Unit,
    onNavigateToBookService: (String) -> Unit,
    onNavigateToBookingDetail: (String) -> Unit = {},
    onNavigateToProfessionalOnboarding: () -> Unit = {},
    onLogout: () -> Unit = {},
    onCategoryClick: (ServiceCategory) -> Unit = { onNavigateToServiceList(it.id) },
    viewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    if (uiState.isLoadingRole) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    if (uiState.isProfessional) {
        ProMainScreen(
            onLogout = onLogout,
            onNavigateToOnboarding = onNavigateToProfessionalOnboarding
        )
        return
    }

    val context = LocalContext.current
    val app = context.applicationContext as WorkNearApplication
    val container = app.container
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        container.customerWorkspaceStore.refresh(
            container.accountRepository,
            container.tokenStore,
            container.walletRepository,
            container.bookingRepository
        )
    }

    BackHandler(enabled = showExitDialog) {
        showExitDialog = false
    }

    // Back press: from any other tab return to Home; from Home, confirm before exiting.
    BackHandler(enabled = !showExitDialog) {
        if (selectedTab != MainTab.HOME) {
            selectedTab = MainTab.HOME
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        WorkNearExitDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                (context as? Activity)?.finish()
            }
        )
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Crossfade(targetState = selectedTab, animationSpec = tween(250), label = "tab") { tab ->
        when (tab) {
            MainTab.HOME -> HomeTabContent(
                modifier = Modifier.padding(padding),
                onServiceTileClick = { categoryId ->
                    if (categoryId == "more") onNavigateToAllServices()
                    else onNavigateToServiceBuilder(categoryId)
                },
                onSeeAllServices = onNavigateToAllServices,
                onRecentBookingClick = { selectedTab = MainTab.BOOKINGS },
                onSearchClick = onNavigateToSearch,
                onNavigateToAddAddress = onNavigateToAddAddress,
                onNavigateToManageAddresses = onNavigateToManageAddresses
            )
            MainTab.BOOKINGS -> MyBookingsScreen(
                modifier = Modifier.padding(padding),
                onBookingClick = onNavigateToBookingDetail
            )
            MainTab.WALLET -> WalletScreen(modifier = Modifier.padding(padding))
            MainTab.PROFILE -> UserProfileScreen(
                modifier = Modifier.padding(padding),
                onLogout = onLogout,
                onEditProfile = onNavigateToEditProfile,
                onManageAddresses = onNavigateToManageAddresses
            )
        }
        }
    }
}

@Composable
private fun MainBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = CardColor,
        tonalElevation = 4.dp
    ) {
        MainTab.entries.forEach { tab ->
            val (icon, label) = when (tab) {
                MainTab.HOME -> Icons.Default.Home to stringResource(R.string.nav_home)
                MainTab.BOOKINGS -> Icons.Default.CalendarMonth to stringResource(R.string.nav_bookings)
                MainTab.WALLET -> Icons.Default.AccountBalanceWallet to stringResource(R.string.nav_wallet)
                MainTab.PROFILE -> Icons.Default.Person to stringResource(R.string.nav_profile)
            }

            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(imageVector = icon, contentDescription = label)
                },
                label = {
                    Text(text = label, fontFamily = sansProText, fontSize = 11.sp)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = MediumGray,
                    unselectedTextColor = MediumGray,
                    indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                )
            )
        }
    }
}
