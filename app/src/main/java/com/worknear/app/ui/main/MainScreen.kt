package com.worknear.app.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.R
import com.worknear.app.data.model.ServiceCategory
import com.worknear.app.ui.bookings.MyBookingsScreen
import com.worknear.app.ui.chat.ChatListScreen
import com.worknear.app.ui.home.HomeTabContent
import com.worknear.app.ui.profile.UserProfileScreen
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.ui.wallet.WalletScreen

enum class MainTab {
    HOME,
    BOOKINGS,
    CHAT,
    WALLET,
    PROFILE
}

@Composable
fun MainScreen(
    onNavigateToServiceList: (String) -> Unit,
    onNavigateToProfessional: (String) -> Unit,
    onNavigateToBookService: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onLogout: () -> Unit = {},
    onCategoryClick: (ServiceCategory) -> Unit = { onNavigateToServiceList(it.id) }
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        when (selectedTab) {
            MainTab.HOME -> HomeTabContent(
                modifier = Modifier.padding(padding),
                onSeeAllCategories = { onNavigateToServiceList("electrician") },
                onCategoryClick = onCategoryClick,
                onBookNow = { onNavigateToServiceList("electrician") },
                onProfessionalClick = { onNavigateToProfessional(it.id) }
            )
            MainTab.BOOKINGS -> MyBookingsScreen(
                modifier = Modifier.padding(padding),
                onChatClick = onNavigateToChat
            )
            MainTab.CHAT -> ChatListScreen(
                modifier = Modifier.padding(padding),
                onChatClick = onNavigateToChat
            )
            MainTab.WALLET -> WalletScreen(modifier = Modifier.padding(padding))
            MainTab.PROFILE -> UserProfileScreen(
                modifier = Modifier.padding(padding),
                onLogout = onLogout
            )
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
                MainTab.CHAT -> Icons.AutoMirrored.Filled.Chat to stringResource(R.string.nav_chat)
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
