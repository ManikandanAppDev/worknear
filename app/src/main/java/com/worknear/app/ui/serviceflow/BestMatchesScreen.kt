package com.worknear.app.ui.serviceflow

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.worknear.app.data.model.Professional
import com.worknear.app.di.AppViewModelProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.R
import com.worknear.app.ui.components.IconTile
import com.worknear.app.ui.components.PrimaryPillButton
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.StarYellow
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.sansProText

@Composable
fun BestMatchesScreen(
    onNavigateBack: () -> Unit,
    onContinue: () -> Unit,
    viewModel: BookingFlowViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val selectedId = ServiceFlowState.selectedProId
    val lockAmount = ServiceFlowState.lockAmount
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMatches() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(CardColor).statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkText)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Best matches", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                    Text("Top pros for your selected services", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
                }
                Spacer(Modifier.width(40.dp))
            }
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            when {
                state.loadingMatches -> CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(32.dp))
                state.matchError != null -> MatchesMessage(
                    title = "Couldn't load professionals",
                    subtitle = state.matchError ?: "Please try again.",
                    actionText = "Retry",
                    onAction = { viewModel.loadMatches(force = true) }
                )
                state.matches.isEmpty() -> MatchesMessage(
                    title = "No professionals available",
                    subtitle = "We couldn't find pros for this service right now. Please try again later.",
                    actionText = "Retry",
                    onAction = { viewModel.loadMatches(force = true) }
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    items(state.matches, key = { it.id }) { pro ->
                        val match = pro.toMatch()
                        ProMatchCard(
                            pro = match,
                            selected = pro.id == selectedId,
                            onSelect = {
                                ServiceFlowState.selectedProId = pro.id
                                ServiceFlowState.selectedMatch = match
                            },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        LockSummaryBar(
            lockAmount = lockAmount,
            enabled = selectedId != null,
            onContinue = onContinue
        )
    }
}

private fun Professional.toMatch(): MatchProfessional = MatchProfessional(
    id = id,
    name = name.ifBlank { "Professional" },
    skills = profession,
    experience = if (experienceYears > 0) "$experienceYears+ years experience" else "Verified pro",
    rating = if (rating > 0.0) rating else 4.8,
    onTimePercent = 95,
    startsAt = startingPrice
)

@Composable
private fun MatchesMessage(title: String, subtitle: String, actionText: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        PrimaryPillButton(text = actionText, onClick = onAction)
    }
}

@Composable
private fun ProMatchCard(
    pro: MatchProfessional,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardColor)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) PrimaryBlue else BorderGray,
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onSelect)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                pro.name.first().toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                fontFamily = sansProText
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(pro.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                Spacer(Modifier.width(4.dp))
                Icon(painterResource(R.drawable.ic_wn_verified), null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
            }
            Text(pro.skills, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_wn_star), null, tint = StarYellow, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(3.dp))
                Text("${pro.rating}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
                Text("  ·  ${pro.onTimePercent}% on-time", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            }
            Spacer(Modifier.height(2.dp))
            Text("Starts at ₹${pro.startsAt}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
        }
        SelectPill(selected = selected, onClick = onSelect)
    }
}

@Composable
private fun SelectPill(selected: Boolean, onClick: () -> Unit) {
    Text(
        if (selected) "Selected" else "Select",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) Color.White else PrimaryBlue,
        fontFamily = sansProText,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) PrimaryBlue else PrimaryBlue.copy(alpha = 0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun LockSummaryBar(lockAmount: Int, enabled: Boolean, onContinue: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(CardColor)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(iconRes = R.drawable.ic_wn_lock, tileSize = 44.dp, iconSize = 22.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Locked on booking", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
                Text("₹$lockAmount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
            }
            PrimaryPillButton(
                text = "Continue",
                enabled = enabled,
                onClick = onContinue
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_wn_verified), null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "Amount locked · Paid after OTP confirmation",
                fontSize = 11.sp,
                color = MediumGray,
                fontFamily = sansProText
            )
        }
    }
}
