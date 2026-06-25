package com.worknear.app.ui.prodashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.worknear.app.utils.LocationHelper
import kotlinx.coroutines.launch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.BookingStatus
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearAlert
import com.worknear.app.ui.components.WorkNearAlertType
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.ErrorRedLight
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.PromoBackground
import com.worknear.app.ui.theme.StarYellow
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.SuccessGreenLight
import com.worknear.app.ui.theme.WarningAmber
import com.worknear.app.ui.theme.WarningAmberLight
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProDashboardHomeTab(
    modifier: Modifier = Modifier,
    onNavigateToOnboarding: () -> Unit,
    onOpenJobsTab: () -> Unit,
    viewModel: ProDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showLocationSheet by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }

    suspend fun scanAndUpdateLocation() {
        if (!LocationHelper.hasLocationPermission(context)) return
        if (!LocationHelper.isLocationEnabled(context)) {
            showGpsDialog = true
            return
        }
        val resolved = LocationHelper.resolveCurrentLocation(context)
        if (resolved != null) {
            viewModel.updateWorkLocation(resolved)
            showLocationSheet = false
        } else {
            viewModel.onLocationScanFailed()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (!LocationHelper.isLocationEnabled(context)) {
                showGpsDialog = true
            } else {
                scope.launch { scanAndUpdateLocation() }
            }
        } else {
            viewModel.onLocationScanFailed("Location permission is required to update your work area")
        }
    }

    fun requestLocationScan() {
        if (!LocationHelper.hasLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        if (!LocationHelper.isLocationEnabled(context)) {
            showGpsDialog = true
            return
        }
        scope.launch { scanAndUpdateLocation() }
    }

    if (showGpsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            containerColor = CardColor,
            title = {
                Text("Turn on location", fontFamily = sansProText, fontWeight = FontWeight.Bold, color = DarkText)
            },
            text = {
                Text(
                    "GPS is turned off. Enable location services so we can scan and update your work area.",
                    fontFamily = sansProText,
                    color = MediumGray
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showGpsDialog = false
                    LocationHelper.openLocationSettings(context)
                }) {
                    Text("Enable GPS", fontFamily = sansProText, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showGpsDialog = false }) {
                    Text("Cancel", fontFamily = sansProText, color = MediumGray)
                }
            }
        )
    }

    if (showLocationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLocationSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CardColor
        ) {
            ProLocationUpdateSheet(
                currentLocation = uiState.workLocationLabel,
                isUpdating = uiState.isUpdatingLocation,
                onScanLocation = ::requestLocationScan,
                onOpenSettings = { LocationHelper.openAppSettings(context) }
            )
        }
    }

    LaunchedEffect(uiState.locationUpdateMessage) {
        if (uiState.locationUpdateMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearLocationUpdateMessage()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        item {
            ProHomeTopBar(
                locationLabel = uiState.workLocationLabel,
                onLocationClick = { showLocationSheet = true }
            )
        }

        when (val dashboard = uiState.dashboard) {
            ProDashboardState.Loading -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            is ProDashboardState.Blocked -> {
                item { ProGreetingHeader(dashboard.name, "Your account access is restricted") }
                item {
                    ProStatusBanner(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        containerColor = ErrorRedLight,
                        borderColor = ErrorRed.copy(alpha = 0.25f),
                        icon = Icons.Default.Lock,
                        iconTint = ErrorRed,
                        title = "Account Blocked",
                        message = "You cannot access jobs with this account. Contact support if you believe this is a mistake.",
                        actionLabel = "Contact Support",
                        onAction = { /* future: support deeplink */ }
                    )
                }
            }

            is ProDashboardState.ProfileIncomplete -> {
                item { ProGreetingHeader(dashboard.name, "Complete your profile to start getting jobs") }
                item {
                    ProStatusBanner(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        containerColor = PromoBackground,
                        borderColor = PrimaryBlue.copy(alpha = 0.2f),
                        icon = Icons.Default.Person,
                        iconTint = PrimaryBlue,
                        title = "Profile Incomplete",
                        message = "Add your details and documents so we can verify you and send nearby jobs.",
                        actionLabel = "Complete Profile",
                        onAction = onNavigateToOnboarding
                    )
                }
            }

            is ProDashboardState.VerificationPending -> {
                item { ProGreetingHeader(dashboard.name, "We're reviewing your profile") }
                item {
                    ProStatusBanner(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        containerColor = WarningAmberLight,
                        borderColor = WarningAmber.copy(alpha = 0.35f),
                        icon = Icons.Default.Schedule,
                        iconTint = WarningAmber,
                        title = "Under Verification",
                        message = "You will be able to receive jobs once your account is verified. This usually takes 24–48 hours.",
                        actionLabel = "Need Help? Contact Support",
                        onAction = { /* future: support deeplink */ },
                        actionTextOnly = true
                    )
                }
            }

            is ProDashboardState.VerificationRejected -> {
                item { ProGreetingHeader(dashboard.name, "Action needed on your documents") }
                item {
                    ProStatusBanner(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        containerColor = ErrorRedLight,
                        borderColor = ErrorRed.copy(alpha = 0.25f),
                        icon = Icons.Default.Cancel,
                        iconTint = ErrorRed,
                        title = "Verification Rejected",
                        message = dashboard.reviewNote?.takeIf { it.isNotBlank() }
                            ?: "Please update your documents to get verified.",
                        actionLabel = "Update Now",
                        onAction = onNavigateToOnboarding
                    )
                }
            }

            is ProDashboardState.Verified -> {
                item {
                    ProGreetingHeader(
                        name = dashboard.name,
                        subtitle = "You are verified and ready to work"
                    )
                }

                item {
                    TodaySummaryCard(
                        summary = dashboard.summary,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                }

                item {
                    SectionTitle(
                        title = "Today's Jobs",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (dashboard.todayJobs.isEmpty()) {
                    item {
                        EmptyTodayJobsCard(modifier = Modifier.padding(horizontal = 20.dp))
                        Spacer(Modifier.height(20.dp))
                    }
                } else {
                    items(dashboard.todayJobs, key = { it.uuid }) { job ->
                        ProTodayJobCard(
                            job = job,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
                            onClick = onOpenJobsTab
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }

        uiState.locationUpdateMessage?.let { message ->
            item {
                WorkNearAlert(
                    message = message,
                    type = if (message.contains("updated", ignoreCase = true)) {
                        WorkNearAlertType.SUCCESS
                    } else {
                        WorkNearAlertType.ERROR
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }

        uiState.errorMessage?.let { message ->
            item {
                WorkNearAlert(
                    message = message,
                    type = WorkNearAlertType.ERROR,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ProHomeTopBar(locationLabel: String, onLocationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .widthIn(max = 240.dp)
                .clip(RoundedCornerShape(50))
                .background(CardColor)
                .border(1.dp, BorderGray, RoundedCornerShape(50))
                .clickable(onClick = onLocationClick)
                .padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp)
        ) {
            Icon(Icons.Default.LocationOn, null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                locationLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                fontFamily = sansProText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
private fun ProLocationUpdateSheet(
    currentLocation: String,
    isUpdating: Boolean,
    onScanLocation: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Text(
            text = "Work location",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Scan your current GPS location to update where you receive jobs. You have one work location — no saved addresses.",
            fontSize = 14.sp,
            color = MediumGray,
            lineHeight = 20.sp,
            fontFamily = sansProText
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = PromoBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Current location", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
                    Text(
                        currentLocation,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText,
                        fontFamily = sansProText
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        WorkNearButton(
            text = if (isUpdating) "Scanning location…" else "Scan & update location",
            buttonType = WorkNearButtonType.FILLED,
            loading = isUpdating,
            icon = Icons.Default.MyLocation,
            onClick = onScanLocation
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Open app settings",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onOpenSettings),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryBlue,
            fontFamily = sansProText
        )
    }
}

@Composable
private fun ProGreetingHeader(name: String, subtitle: String) {
    val displayName = name.trim().ifBlank { "Professional" }
    val firstName = displayName.split("\\s+".toRegex()).firstOrNull() ?: displayName
    val nameFontSize = when {
        firstName.length > 18 -> 22.sp
        firstName.length > 12 -> 24.sp
        else -> 28.sp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = timeGreeting(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MediumGray,
                fontFamily = sansProText
            )
            Spacer(Modifier.width(6.dp))
            Text(text = "👋", fontSize = 18.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = firstName,
            fontSize = nameFontSize,
            lineHeight = when {
                firstName.length > 18 -> 26.sp
                firstName.length > 12 -> 28.sp
                else -> 32.sp
            },
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (displayName.contains(" ") && displayName != firstName) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = displayName,
                fontSize = 13.sp,
                color = MediumGray,
                fontFamily = sansProText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = MediumGray,
            fontFamily = sansProText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProStatusBanner(
    modifier: Modifier = Modifier,
    containerColor: Color,
    borderColor: Color,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    actionTextOnly: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontFamily = sansProText
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = MediumGray,
                        lineHeight = 18.sp,
                        fontFamily = sansProText
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            if (actionTextOnly) {
                Text(
                    text = actionLabel,
                    modifier = Modifier.clickable(onClick = onAction),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    fontFamily = sansProText
                )
            } else {
                WorkNearButton(
                    text = actionLabel,
                    buttonType = WorkNearButtonType.FILLED,
                    onClick = onAction
                )
            }
        }
    }
}

@Composable
private fun TodaySummaryCard(summary: ProTodaySummary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.14f),
                            PrimaryBlue.copy(alpha = 0.05f),
                            CardColor
                        )
                    )
                )
                .border(1.dp, PrimaryBlue.copy(alpha = 0.14f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Today's Summary",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            fontFamily = sansProText
                        )
                        Text(
                            text = "Your performance snapshot",
                            fontSize = 12.sp,
                            color = MediumGray,
                            fontFamily = sansProText
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PrimaryBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Today",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            fontFamily = sansProText
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrendySummaryTile(
                        icon = Icons.Default.Work,
                        iconTint = PrimaryBlue,
                        iconBackground = PrimaryBlue.copy(alpha = 0.12f),
                        value = summary.jobsCompleted.toString(),
                        label = "Jobs",
                        modifier = Modifier.weight(1f)
                    )
                    TrendySummaryTile(
                        icon = Icons.Default.Star,
                        iconTint = StarYellow,
                        iconBackground = StarYellow.copy(alpha = 0.18f),
                        value = if (summary.rating > 0) String.format("%.1f", summary.rating) else "—",
                        label = "Rating",
                        modifier = Modifier.weight(1f)
                    )
                    TrendySummaryTile(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = SuccessGreen,
                        iconBackground = SuccessGreen.copy(alpha = 0.14f),
                        value = "₹${summary.earnings}",
                        label = "Earned",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendySummaryTile(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(1.dp, BorderGray.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MediumGray,
            fontFamily = sansProText
        )
    }
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText,
        fontFamily = sansProText
    )
}

@Composable
private fun EmptyTodayJobsCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MediumGray, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text("No jobs scheduled for today", fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
            Text("New requests will appear in the Jobs tab", fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
        }
    }
}

@Composable
private fun ProTodayJobCard(job: Booking, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PromoBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_wn_professional),
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(job.serviceName, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                Text(job.time, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            }
            Column(horizontalAlignment = Alignment.End) {
                ProJobStatusChip(job.status)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "₹${if (job.proEarning > 0) job.proEarning else job.price}",
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = sansProText
                )
            }
        }
    }
}

@Composable
private fun ProJobStatusChip(status: BookingStatus) {
    val (label, bg, fg) = when (status) {
        BookingStatus.PENDING -> Triple("New request", PromoBackground, PrimaryBlue)
        BookingStatus.CONFIRMED -> Triple("Accepted", SuccessGreenLight, SuccessGreen)
        BookingStatus.ON_THE_WAY -> Triple("On the way", PromoBackground, PrimaryBlue)
        BookingStatus.IN_PROGRESS -> Triple("Work started", WarningAmberLight, WarningAmber)
        BookingStatus.ARRIVED -> Triple("Arrived", SuccessGreenLight, SuccessGreen)
        BookingStatus.COMPLETED -> Triple("Done", SuccessGreenLight, SuccessGreen)
        BookingStatus.COMPLETED_PENDING_OTP -> Triple("OTP pending", WarningAmberLight, WarningAmber)
        else -> Triple("Active", PromoBackground, PrimaryBlue)
    }
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = fg,
        fontFamily = sansProText
    )
}

private fun timeGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}
