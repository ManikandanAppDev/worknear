package com.worknear.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.address.AddressType
import com.worknear.app.ui.components.PrimaryPillButton
import com.worknear.app.ui.components.WorkNearAlert
import com.worknear.app.ui.components.WorkNearAlertType
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText

@Composable
fun CompleteProfileScreen(
    onNavigateBack: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: CompleteProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onNavigateBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    "Almost there",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = sansProText
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tell us your name and where you'd like the pro to come.",
                    fontSize = 14.sp,
                    color = MediumGray,
                    fontFamily = sansProText
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            SectionLabel("Your Name")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Full name", color = MediumGray, fontSize = 14.sp, fontFamily = sansProText) },
                textStyle = TextStyle(color = DarkText, fontSize = 14.sp, fontFamily = sansProText),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(22.dp))
            SectionLabel("Address Type")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddressType.entries.forEach { t ->
                    AddressTypeTile(
                        type = t,
                        selected = t == uiState.addressType,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onTypeChange(t) }
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            SectionLabel("Full Address")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.fullAddress,
                onValueChange = viewModel::onAddressChange,
                modifier = Modifier.fillMaxWidth().height(96.dp),
                placeholder = { Text("House / Flat no, Street, Area, City - PIN", color = MediumGray, fontSize = 14.sp, fontFamily = sansProText) },
                textStyle = TextStyle(color = DarkText, fontSize = 14.sp, fontFamily = sansProText),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )
            Spacer(Modifier.height(10.dp))
            UseCurrentLocationChip(onClick = viewModel::useCurrentLocation)

            Spacer(Modifier.height(22.dp))
            SectionLabel("Landmark (Optional)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.landmark,
                onValueChange = viewModel::onLandmarkChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Nearby landmark for easy identification", color = MediumGray, fontSize = 14.sp, fontFamily = sansProText) },
                textStyle = TextStyle(color = DarkText, fontSize = 14.sp, fontFamily = sansProText),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                WorkNearAlert(message = uiState.errorMessage!!, type = WorkNearAlertType.ERROR)
            }

            Spacer(Modifier.height(16.dp))
        }

        Column(modifier = Modifier.fillMaxWidth().background(CardColor).padding(20.dp)) {
            PrimaryPillButton(
                text = if (uiState.isLoading) "Saving..." else "Save & Continue",
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSave && !uiState.isLoading,
                onClick = { viewModel.save(onCompleted) }
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = BorderGray,
    cursorColor = PrimaryBlue
)

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MediumGray, fontFamily = sansProText)
}

@Composable
private fun AddressTypeTile(
    type: AddressType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) PrimaryBlue.copy(alpha = 0.08f) else CardColor)
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) PrimaryBlue else BorderGray,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painterResource(type.iconRes),
            null,
            tint = if (selected) PrimaryBlue else MediumGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            type.label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) PrimaryBlue else DarkText,
            fontFamily = sansProText
        )
    }
}

@Composable
private fun UseCurrentLocationChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PrimaryBlue.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(R.drawable.ic_wn_location), null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Use Current Location", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = sansProText)
    }
}
