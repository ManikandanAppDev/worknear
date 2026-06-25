package com.worknear.app.ui.prodashboard

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.WorkNearApplication
import com.worknear.app.ui.components.WorkNearExitDialog
import com.worknear.app.ui.projobs.ProfessionalJobsScreen
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText
import androidx.compose.ui.platform.LocalContext

enum class ProTab {
    HOME,
    JOBS,
    EARNINGS,
    PROFILE
}

@Composable
fun ProMainScreen(
    onLogout: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(ProTab.HOME) }
    val context = LocalContext.current
    val app = context.applicationContext as WorkNearApplication
    val container = app.container
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        container.proWorkspaceStore.refresh(
            container.bookingRepository,
            container.professionalRepository
        )
    }

    BackHandler(enabled = showExitDialog) {
        showExitDialog = false
    }

    BackHandler(enabled = !showExitDialog) {
        if (selectedTab != ProTab.HOME) {
            selectedTab = ProTab.HOME
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
            ProBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Crossfade(targetState = selectedTab, animationSpec = tween(250), label = "proTab") { tab ->
            when (tab) {
                ProTab.HOME -> ProDashboardHomeTab(
                    modifier = Modifier.padding(padding),
                    onNavigateToOnboarding = onNavigateToOnboarding,
                    onOpenJobsTab = { selectedTab = ProTab.JOBS }
                )
                ProTab.JOBS -> ProfessionalJobsScreen(
                    modifier = Modifier.padding(padding),
                    embedded = true,
                    onLogout = onLogout
                )
                ProTab.EARNINGS -> ProEarningsTab(
                    modifier = Modifier.padding(padding),
                    proWorkspaceStore = container.proWorkspaceStore
                )
                ProTab.PROFILE -> ProProfileTab(
                    modifier = Modifier.padding(padding),
                    onLogout = onLogout,
                    accountRepository = container.accountRepository,
                    professionalRepository = container.professionalRepository
                )
            }
        }
    }
}

@Composable
private fun ProBottomBar(
    selectedTab: ProTab,
    onTabSelected: (ProTab) -> Unit
) {
    NavigationBar(
        containerColor = CardColor,
        tonalElevation = 4.dp
    ) {
        ProTab.entries.forEach { tab ->
            val (icon, label) = when (tab) {
                ProTab.HOME -> Icons.Default.Home to "Home"
                ProTab.JOBS -> Icons.Default.CalendarMonth to "Jobs"
                ProTab.EARNINGS -> Icons.Default.AccountBalanceWallet to "Earnings"
                ProTab.PROFILE -> Icons.Default.Person to "Profile"
            }
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { Text(text = label, fontFamily = sansProText, fontSize = 11.sp) },
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
