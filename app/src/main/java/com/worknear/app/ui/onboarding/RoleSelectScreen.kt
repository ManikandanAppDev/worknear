package com.worknear.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearAlert
import com.worknear.app.ui.components.WorkNearAlertType
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

private const val ROLE_CUSTOMER = "CUSTOMER"
private const val ROLE_PROFESSIONAL = "PROFESSIONAL"

/**
 * Post-OTP "Who are you?" step. Shown until the user explicitly confirms customer or professional
 * on the backend ([UserDto.roleConfirmed]). New accounts default to CUSTOMER at OTP, but that is
 * not treated as a completed choice until this screen is finished.
 */
@Composable
fun RoleSelectScreen(
    onCustomer: () -> Unit,
    onProfessional: () -> Unit,
    viewModel: RoleSelectViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<String?>(null) }

    // This is a deliberate fork in onboarding — block accidental back navigation.
    BackHandler(enabled = true) { }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "How do you want to use WorkNear?",
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Pick the option that fits you. This sets up the right experience for your account.",
            fontSize = 15.sp,
            color = MediumGray,
            fontFamily = sansProText
        )

        Spacer(Modifier.height(28.dp))

        RoleCard(
            icon = Icons.Default.Person,
            title = "I need a service",
            subtitle = "Find and book trusted professionals for your home — cleaning, repairs, appliances and more.",
            selected = selected == ROLE_CUSTOMER,
            onClick = { selected = ROLE_CUSTOMER }
        )

        Spacer(Modifier.height(16.dp))

        RoleCard(
            icon = Icons.Default.Handyman,
            title = "I provide a service",
            subtitle = "Get job requests from customers near you and grow your business with WorkNear.",
            selected = selected == ROLE_PROFESSIONAL,
            onClick = { selected = ROLE_PROFESSIONAL }
        )

        if (uiState.errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            WorkNearAlert(message = uiState.errorMessage!!, type = WorkNearAlertType.ERROR)
        }

        Spacer(Modifier.weight(1f))

        WorkNearButton(
            text = "Continue",
            buttonType = WorkNearButtonType.FILLED,
            enabled = selected != null && !uiState.isSubmitting,
            loading = uiState.isSubmitting,
            onClick = {
                when (selected) {
                    ROLE_CUSTOMER -> viewModel.chooseCustomer(onCustomer)
                    ROLE_PROFESSIONAL -> viewModel.chooseProfessional(onProfessional)
                }
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) PrimaryBlue else BorderGray
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) PrimaryBlue.copy(alpha = 0.06f) else CardColor)
            .border(BorderStroke(if (selected) 2.dp else 1.dp, borderColor), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryBlue)
        }

        Spacer(Modifier.size(14.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                fontFamily = sansProText
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MediumGray,
                fontFamily = sansProText
            )
        }

        if (selected) {
            Spacer(Modifier.size(10.dp))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = PrimaryBlue
            )
        }
    }
}
