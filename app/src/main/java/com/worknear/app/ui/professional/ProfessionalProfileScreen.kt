package com.worknear.app.ui.professional

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearTopBar
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.StarYellow
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

@Composable
fun ProfessionalProfileScreen(
    professionalId: String,
    onNavigateBack: () -> Unit,
    onBookNow: (String) -> Unit,
    viewModel: ProfessionalProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(professionalId) { viewModel.load(professionalId) }

    val professional = uiState.professional
    if (professional == null) {
        Scaffold(
            topBar = { WorkNearTopBar(title = "Professional", onBackClick = onNavigateBack) },
            containerColor = Background
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = PrimaryBlue)
                } else {
                    Text(
                        uiState.errorMessage ?: "Professional not found",
                        color = MediumGray,
                        textAlign = TextAlign.Center,
                        fontFamily = sansProText,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardColor)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.service_charge),
                        fontSize = 12.sp,
                        color = MediumGray,
                        fontFamily = sansProText
                    )
                    Text(
                        "₹${professional.startingPrice}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontFamily = sansProText
                    )
                }
                WorkNearButton(
                    text = stringResource(R.string.book_now),
                    buttonType = WorkNearButtonType.FILLED,
                    modifier = Modifier.width(160.dp),
                    onClick = { onBookNow(professional.id) }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(professional.imageRes),
                        contentDescription = professional.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Share, "Share", tint = Color.White)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                professional.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                fontFamily = sansProText
                            )
                            if (professional.isVerified) {
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.CheckCircle, "Verified", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = StarYellow, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${professional.rating} (${professional.reviewCount} reviews) • ${professional.experienceYears} yrs experience",
                                fontSize = 14.sp,
                                color = MediumGray,
                                fontFamily = sansProText
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.about),
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = sansProText
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    professional.about,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 14.sp,
                    color = MediumGray,
                    lineHeight = 22.sp,
                    fontFamily = sansProText
                )
            }

            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.services),
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = sansProText
                )
                Spacer(Modifier.height(8.dp))
                professional.services.forEach { service ->
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ", color = PrimaryBlue, fontSize = 16.sp)
                        Text(service, fontSize = 14.sp, color = DarkText, fontFamily = sansProText)
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}
