package com.worknear.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.BookingStatus
import com.worknear.app.data.model.HomeServiceTile
import com.worknear.app.data.model.ServiceCatalog
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.address.AddressStore
import com.worknear.app.ui.address.SelectAddressSheetContent
import com.worknear.app.ui.components.IconTile
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.LightGray
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.WarningAmber
import com.worknear.app.ui.theme.sansProText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabContent(
    modifier: Modifier = Modifier,
    onServiceTileClick: (String) -> Unit,
    onSeeAllServices: () -> Unit,
    onRecentBookingClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onNavigateToAddAddress: () -> Unit = {},
    onNavigateToManageAddresses: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddressSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { AddressStore.refresh() }

    if (showAddressSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddressSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CardColor
        ) {
            SelectAddressSheetContent(
                onSelect = {
                    AddressStore.select(it)
                    showAddressSheet = false
                },
                onAddNew = {
                    showAddressSheet = false
                    onNavigateToAddAddress()
                },
                onManage = {
                    showAddressSheet = false
                    onNavigateToManageAddresses()
                }
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        item {
            HomeTopBar(
                address = AddressStore.selected?.display ?: "Add your address",
                onAddressClick = { showAddressSheet = true }
            )
        }

        item {
            Text(
                "Hi, find a pro",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
        }

        item { HomeSearchBar(onClick = onSearchClick, modifier = Modifier.padding(horizontal = 20.dp)) }

        item {
            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Popular Services", onSeeAll = onSeeAllServices)
            Spacer(Modifier.height(16.dp))
            val categoryTiles = if (uiState.categories.isNotEmpty()) {
                uiState.categories.take(7)
                    .map { HomeServiceTile(it.id, it.title, ServiceCatalog.iconFor(it.id)) } +
                    HomeServiceTile("more", "More", R.drawable.ic_wn_more)
            } else {
                ServiceCatalog.homeTiles
            }
            PopularServicesGrid(
                tiles = categoryTiles,
                onTileClick = { id -> if (id == "more") onSeeAllServices() else onServiceTileClick(id) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item {
            Spacer(Modifier.height(24.dp))
            ThreeStepsPromo(modifier = Modifier.padding(horizontal = 20.dp))
        }

        item {
            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Recent Bookings", onSeeAll = onRecentBookingClick)
            Spacer(Modifier.height(12.dp))
            when {
                uiState.bookingsLoading -> RecentBookingLoading(modifier = Modifier.padding(horizontal = 20.dp))
                uiState.recentBookings.isEmpty() -> RecentBookingEmptyState(
                    onBook = onSeeAllServices,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                else -> RecentBookingCard(
                    booking = uiState.recentBookings.first(),
                    onClick = onRecentBookingClick,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            TrustStrip(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeTopBar(address: String, onAddressClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .widthIn(max = 240.dp)
                .clip(RoundedCornerShape(50))
                .background(CardColor)
                .border(1.dp, BorderGray, RoundedCornerShape(50))
                .clickable(onClick = onAddressClick)
                .padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp)
        ) {
            Icon(Icons.Default.LocationOn, null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                address,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                fontFamily = sansProText,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(Icons.Default.ArrowDropDown, null, tint = MediumGray, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CardColor)
                .border(1.dp, BorderGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, "Notifications", tint = DarkText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HomeSearchBar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, null, tint = MediumGray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "Search for a service...",
            color = LightGray,
            fontSize = 14.sp,
            fontFamily = sansProText,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        Text(
            "See all",
            modifier = Modifier.clickable(onClick = onSeeAll),
            color = PrimaryBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = sansProText
        )
    }
}

@Composable
private fun PopularServicesGrid(
    tiles: List<HomeServiceTile>,
    onTileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        tiles.chunked(4).forEach { rowTiles ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowTiles.forEach { tile ->
                    ServiceTileItem(
                        tile = tile,
                        onClick = { onTileClick(tile.categoryId) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowTiles.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ServiceTileItem(
    tile: HomeServiceTile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconTile(iconRes = tile.iconRes, tileSize = 58.dp, iconSize = 28.dp, cornerRadius = 18.dp)
        Spacer(Modifier.height(8.dp))
        Text(
            tile.label,
            fontSize = 12.sp,
            color = DarkText,
            fontFamily = sansProText,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun ThreeStepsPromo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PrimaryBlue.copy(alpha = 0.08f))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Book a trusted pro\nin 3 easy steps",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(12.dp))
            listOf("Choose a service", "Select best pro", "Relax & book").forEach { step ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    Box(
                        modifier = Modifier.size(18.dp).clip(CircleShape).background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(step, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        IconTile(
            iconRes = R.drawable.ic_wn_professional,
            tileSize = 76.dp,
            iconSize = 40.dp,
            cornerRadius = 22.dp,
            background = PrimaryBlue.copy(alpha = 0.16f)
        )
    }
}

@Composable
private fun RecentBookingCard(booking: Booking, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (badgeText, badgeColor) = bookingStatusBadge(booking)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(iconRes = R.drawable.ic_wn_receipt, tileSize = 48.dp, iconSize = 24.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(booking.serviceName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
            Spacer(Modifier.height(2.dp))
            Text(
                "${booking.professionalName} · ${booking.date}, ${booking.time}",
                fontSize = 12.sp,
                color = MediumGray,
                fontFamily = sansProText
            )
        }
        StatusBadge(text = badgeText, color = badgeColor)
    }
}

@Composable
private fun bookingStatusBadge(booking: Booking): Pair<String, Color> = when (booking.status) {
    BookingStatus.PENDING -> "Pending" to PrimaryBlue
    BookingStatus.CONFIRMED -> "Confirmed" to SuccessGreen
    BookingStatus.ON_THE_WAY -> "On the way" to WarningAmber
    BookingStatus.ARRIVED -> "Arrived" to WarningAmber
    BookingStatus.IN_PROGRESS -> "In progress" to PrimaryBlue
    BookingStatus.COMPLETED_PENDING_OTP -> "Confirm OTP" to WarningAmber
    BookingStatus.COMPLETED -> "Completed" to SuccessGreen
    BookingStatus.CANCELLED -> "Cancelled" to MediumGray
}

@Composable
private fun RecentBookingEmptyState(onBook: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(vertical = 28.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconTile(
            iconRes = R.drawable.ic_wn_calendar,
            tileSize = 64.dp,
            iconSize = 32.dp,
            cornerRadius = 20.dp,
            background = PrimaryBlue.copy(alpha = 0.10f)
        )
        Spacer(Modifier.height(14.dp))
        Text("No bookings yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        Spacer(Modifier.height(4.dp))
        Text(
            "Book a service and track it here.",
            fontSize = 13.sp,
            color = MediumGray,
            fontFamily = sansProText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(PrimaryBlue)
                .clickable(onClick = onBook)
                .padding(horizontal = 28.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Book a Service", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = sansProText)
        }
    }
}

@Composable
private fun RecentBookingLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        fontFamily = sansProText,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun TrustStrip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TrustItem(R.drawable.ic_wn_verified, "Verified\nPros")
        TrustItem(R.drawable.ic_wn_pricing, "Transparent\nPricing")
        TrustItem(R.drawable.ic_wn_lock, "Secure\nPayments")
        TrustItem(R.drawable.ic_wn_support, "Support\n24/7")
    }
}

@Composable
private fun TrustItem(iconRes: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconTile(iconRes = iconRes, tileSize = 44.dp, iconSize = 22.dp, cornerRadius = 14.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = MediumGray,
            fontFamily = sansProText,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}
