package com.worknear.app.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearAlert
import com.worknear.app.ui.components.WorkNearAlertType
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.SuccessGreenLight
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.LocationHelper
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private data class DocSlot(
    val type: String,
    val label: String,
    val subtitle: String,
    val uploadLabel: String,
    val icon: ImageVector
)

private val DOCUMENT_SLOTS = listOf(
    DocSlot(
        type = "GOV_ID",
        label = "Aadhar Card",
        subtitle = "Government ID — front side only",
        uploadLabel = "Upload front",
        icon = Icons.Outlined.Badge
    ),
    DocSlot(
        type = "WORK_CERTIFICATE",
        label = "Pan Card",
        subtitle = "Tax ID — front side only",
        uploadLabel = "Upload front",
        icon = Icons.Outlined.CreditCard
    ),
    DocSlot(
        type = "ADDRESS_PROOF",
        label = "Profile Photo",
        subtitle = "Clear face photo for your profile",
        uploadLabel = "Upload photo",
        icon = Icons.Outlined.CameraAlt
    )
)

@Composable
fun ProfessionalOnboardingScreen(
    onNavigateBack: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: ProfessionalOnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler {
        when (uiState.step) {
            ProOnboardStep.DOCUMENTS -> viewModel.backToProfile()
            ProOnboardStep.PROFILE -> onNavigateBack()
        }
    }

    suspend fun fetchLocation() {
        if (uiState.locationState == LocationFetchState.SUCCESS) return
        viewModel.beginLocationFetch()
        val resolved = LocationHelper.resolveCurrentLocation(context)
        if (resolved != null) {
            viewModel.onLocationResolved(resolved)
        } else if (LocationHelper.hasLocationPermission(context)) {
            viewModel.onLocationFetchFailed()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch { fetchLocation() }
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    fun retryLocation() {
        if (LocationHelper.hasLocationPermission(context)) {
            scope.launch { fetchLocation() }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(uiState.step) {
        if (uiState.step != ProOnboardStep.PROFILE) return@LaunchedEffect
        if (uiState.locationState == LocationFetchState.SUCCESS) return@LaunchedEffect
        if (uiState.locationPromptStarted) return@LaunchedEffect

        viewModel.markLocationPromptStarted()
        if (LocationHelper.hasLocationPermission(context)) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var pickingDocType by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val type = pickingDocType
        pickingDocType = null
        if (uri == null || type == null) return@rememberLauncherForActivityResult
        scope.launch {
            val copied = copyDocToInternal(context, uri, type)
            if (copied != null) {
                viewModel.onDocumentPicked(
                    ProDocumentPick(
                        type = type,
                        file = copied.first,
                        mimeType = copied.second,
                        displayName = copied.first.name
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (uiState.step) {
                ProOnboardStep.PROFILE -> ProfileSetupStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    onNavigateBack = onNavigateBack,
                    onRetryLocation = ::retryLocation,
                    onOpenLocationSettings = { openAppSettings(context) }
                )
                ProOnboardStep.DOCUMENTS -> DocumentsVerifyStep(
                    uiState = uiState,
                    onBack = viewModel::backToProfile,
                    onPickDocument = { type ->
                        pickingDocType = type
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            if (uiState.errorMessage != null) {
                WorkNearAlert(
                    message = uiState.errorMessage!!,
                    type = WorkNearAlertType.ERROR,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            when (uiState.step) {
                ProOnboardStep.PROFILE -> WorkNearButton(
                    text = "Continue",
                    buttonType = WorkNearButtonType.FILLED,
                    loading = uiState.isSubmitting,
                    enabled = !uiState.isLoading &&
                        uiState.isProfileStepComplete &&
                        uiState.locationState != LocationFetchState.FETCHING,
                    onClick = viewModel::saveProfileAndContinue
                )
                ProOnboardStep.DOCUMENTS -> WorkNearButton(
                    text = "Submit",
                    buttonType = WorkNearButtonType.FILLED,
                    loading = uiState.isSubmitting,
                    onClick = { viewModel.submit(onCompleted) }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileSetupStep(
    uiState: ProfessionalOnboardingUiState,
    viewModel: ProfessionalOnboardingViewModel,
    onNavigateBack: () -> Unit,
    onRetryLocation: () -> Unit,
    onOpenLocationSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.14f),
                            PrimaryBlue.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                    Spacer(Modifier.weight(1f))
                    StepBadge(text = "Step 1 of 2")
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Let's set up your profile",
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = sansProText
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Build a professional presence customers can trust",
                    fontSize = 15.sp,
                    color = MediumGray,
                    fontFamily = sansProText
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileSectionCard(title = "Basic details", subtitle = "How customers will know you") {
                ProOutlinedField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onNameChange,
                    label = "Full Name",
                    placeholder = "Name as per Aadhaar"
                )
            }

            ProfileSectionCard(
                title = "Services you offer",
                subtitle = "Select all trades that apply to you"
            ) {
                if (uiState.isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = PrimaryBlue,
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.categories.forEach { category ->
                            ServiceChip(
                                name = category.name,
                                selected = uiState.selectedCategoryIds.contains(category.id),
                                onClick = { viewModel.toggleCategory(category.id) }
                            )
                        }
                    }
                    if (uiState.selectedCategoryIds.isNotEmpty()) {
                        Text(
                            text = "${uiState.selectedCategoryIds.size} selected",
                            fontSize = 12.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium,
                            fontFamily = sansProText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            ProfileSectionCard(title = "Experience", subtitle = "How long you've been in the trade") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PRO_EXPERIENCE_OPTIONS.forEach { option ->
                        ExperiencePill(
                            label = option.label,
                            selected = uiState.selectedExperienceYears == option.years,
                            onClick = { viewModel.onExperienceSelected(option.years) }
                        )
                    }
                }
            }

            ProfileSectionCard(
                title = "Work location",
                subtitle = "Auto-detected from GPS — used to match nearby jobs"
            ) {
                LocationAutoFetchCard(
                    state = uiState.locationState,
                    city = uiState.city,
                    area = uiState.area,
                    onRetry = onRetryLocation,
                    onOpenSettings = onOpenLocationSettings
                )
            }
        }
    }
}

@Composable
private fun StepBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = PrimaryBlue.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryBlue,
            fontFamily = sansProText
        )
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardColor,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                fontFamily = sansProText
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MediumGray,
                fontFamily = sansProText
            )
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun ServiceChip(name: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) PrimaryBlue else Color.White
    val border = if (selected) PrimaryBlue else BorderGray
    val textColor = if (selected) Color.White else DarkText
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(BorderStroke(1.dp, border), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (selected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            fontFamily = sansProText
        )
    }
}

@Composable
private fun ExperiencePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) PrimaryBlue else Color.White)
            .border(
                BorderStroke(1.dp, if (selected) PrimaryBlue else BorderGray),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) Color.White else DarkText,
            fontFamily = sansProText
        )
    }
}

@Composable
private fun LocationAutoFetchCard(
    state: LocationFetchState,
    city: String,
    area: String,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "locPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                when (state) {
                    LocationFetchState.SUCCESS -> SuccessGreenLight
                    LocationFetchState.DENIED, LocationFetchState.FAILED -> Color(0xFFFFF7ED)
                    else -> PrimaryBlue.copy(alpha = 0.06f)
                }
            )
            .border(
                BorderStroke(
                    1.dp,
                    when (state) {
                        LocationFetchState.SUCCESS -> SuccessGreen.copy(alpha = 0.35f)
                        LocationFetchState.DENIED, LocationFetchState.FAILED -> Color(0xFFFDBA74)
                        else -> PrimaryBlue.copy(alpha = 0.2f)
                    }
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when (state) {
                            LocationFetchState.SUCCESS -> SuccessGreen.copy(alpha = 0.15f)
                            else -> PrimaryBlue.copy(alpha = 0.12f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    LocationFetchState.FETCHING -> CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = PrimaryBlue,
                        strokeWidth = 2.dp
                    )
                    LocationFetchState.SUCCESS -> Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    else -> Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { alpha = if (state == LocationFetchState.NOT_STARTED) 1f else pulseAlpha }
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (state) {
                        LocationFetchState.FETCHING -> "Finding your location…"
                        LocationFetchState.SUCCESS -> "Location detected"
                        LocationFetchState.DENIED -> "Location access needed"
                        LocationFetchState.FAILED -> "Couldn't detect location"
                        LocationFetchState.NOT_STARTED -> "Preparing location…"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText,
                    fontFamily = sansProText
                )
                Text(
                    text = when (state) {
                        LocationFetchState.FETCHING -> "Pinpointing your city and area — this may take a moment"
                        LocationFetchState.SUCCESS -> "Your work area is set automatically"
                        LocationFetchState.DENIED -> "Allow location so we can fill your city and area"
                        LocationFetchState.FAILED -> "Check GPS is on, then try again"
                        LocationFetchState.NOT_STARTED -> "We'll ask for location permission next"
                    },
                    fontSize = 12.sp,
                    color = MediumGray,
                    fontFamily = sansProText,
                    lineHeight = 16.sp
                )
            }
        }

        if (state == LocationFetchState.FETCHING) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryBlue,
                trackColor = PrimaryBlue.copy(alpha = 0.15f)
            )
        }

        if (state == LocationFetchState.SUCCESS && city.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LocationTag(label = "City", value = city, modifier = Modifier.weight(1f))
                LocationTag(label = "Area", value = area, modifier = Modifier.weight(1f))
            }
        }

        if (state == LocationFetchState.DENIED || state == LocationFetchState.FAILED) {
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LocationActionChip(
                    label = "Try again",
                    icon = Icons.Default.Refresh,
                    onClick = onRetry
                )
                if (state == LocationFetchState.DENIED) {
                    LocationActionChip(
                        label = "Open settings",
                        icon = Icons.Default.MyLocation,
                        onClick = onOpenSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationTag(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 11.sp, color = MediumGray, fontFamily = sansProText)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText,
            fontFamily = sansProText,
            maxLines = 2
        )
    }
}

@Composable
private fun LocationActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PrimaryBlue.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue, fontFamily = sansProText)
    }
}

private fun openAppSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}

@Composable
private fun DocumentsVerifyStep(
    uiState: ProfessionalOnboardingUiState,
    onBack: () -> Unit,
    onPickDocument: (String) -> Unit
) {
    val uploadedCount = DOCUMENT_SLOTS.count { uiState.documents[it.type] != null }
    val progress = uploadedCount.toFloat() / DOCUMENT_SLOTS.size

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.14f),
                            PrimaryBlue.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                    }
                    Spacer(Modifier.weight(1f))
                    StepBadge(text = "Step 2 of 2")
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Verify your documents",
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = sansProText
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Upload clear photos — we review them to build trust with customers",
                    fontSize = 15.sp,
                    color = MediumGray,
                    fontFamily = sansProText,
                    lineHeight = 21.sp
                )
                Spacer(Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardColor,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$uploadedCount of ${DOCUMENT_SLOTS.size} uploaded",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkText,
                                fontFamily = sansProText,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                fontFamily = sansProText
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            progress = { progress },
                            color = PrimaryBlue,
                            trackColor = PrimaryBlue.copy(alpha = 0.12f)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DOCUMENT_SLOTS.forEach { slot ->
                val pick = uiState.documents[slot.type]
                DocumentUploadCard(
                    slot = slot,
                    uploaded = pick != null,
                    previewUri = pick?.file?.toURI()?.toString(),
                    onClick = { onPickDocument(slot.type) }
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = PrimaryBlue.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Documents are encrypted and only used for verification",
                        fontSize = 13.sp,
                        color = MediumGray,
                        fontFamily = sansProText,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentUploadCard(
    slot: DocSlot,
    uploaded: Boolean,
    previewUri: String?,
    onClick: () -> Unit
) {
    val borderColor = if (uploaded) SuccessGreen.copy(alpha = 0.4f) else BorderGray.copy(alpha = 0.8f)
    val cardBg = if (uploaded) SuccessGreenLight else CardColor

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        shadowElevation = if (uploaded) 0.dp else 2.dp,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (uploaded) SuccessGreen.copy(alpha = 0.15f)
                            else PrimaryBlue.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uploaded) Icons.Default.CheckCircle else slot.icon,
                        contentDescription = null,
                        tint = if (uploaded) SuccessGreen else PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText,
                        fontFamily = sansProText
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = slot.subtitle,
                        fontSize = 12.sp,
                        color = MediumGray,
                        fontFamily = sansProText,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (uploaded && previewUri != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = previewUri,
                        contentDescription = slot.label,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Uploaded",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen,
                            fontFamily = sansProText
                        )
                        Text(
                            text = "Tap to replace",
                            fontSize = 12.sp,
                            color = MediumGray,
                            fontFamily = sansProText
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(
                            BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.25f)),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            slot.icon,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = slot.uploadLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            fontFamily = sansProText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { FieldFloatingLabel(label) },
        placeholder = { Text(placeholder, color = MediumGray, fontFamily = sansProText) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = proFieldColors()
    )
}

@Composable
private fun FieldFloatingLabel(text: String) {
    Text(text, fontFamily = sansProText, fontSize = 14.sp)
}

@Composable
private fun proFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = BorderGray,
    cursorColor = PrimaryBlue,
    focusedTextColor = DarkText,
    unfocusedTextColor = DarkText,
    focusedContainerColor = CardColor,
    unfocusedContainerColor = CardColor,
    focusedLabelColor = MediumGray,
    unfocusedLabelColor = MediumGray
)

/** Copies the picked document image into app-internal storage; returns the file and its MIME type. */
private suspend fun copyDocToInternal(context: Context, uri: Uri, type: String): Pair<File, String?>? =
    withContext(Dispatchers.IO) {
        try {
            val mime = context.contentResolver.getType(uri)
            val ext = when {
                mime?.contains("png") == true -> "png"
                mime?.contains("webp") == true -> "webp"
                else -> "jpg"
            }
            val file = File(context.filesDir, "doc_${type}_${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file to (mime ?: "image/jpeg")
        } catch (_: Exception) {
            null
        }
    }
