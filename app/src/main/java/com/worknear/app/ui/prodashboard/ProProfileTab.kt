package com.worknear.app.ui.prodashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.worknear.app.data.remote.ApiResult
import com.worknear.app.data.remote.dto.ProProfileDto
import com.worknear.app.data.repository.AccountRepository
import com.worknear.app.data.repository.ProfessionalRepository
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.PromoBackground
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.SuccessGreenLight
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

@Composable
fun ProProfileTab(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    accountRepository: AccountRepository,
    professionalRepository: ProfessionalRepository
) {
    var loading by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("Professional") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<ProProfileDto?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        val me = accountRepository.getMe()
        if (me is ApiResult.Success) {
            name = me.data.fullName.orEmpty().ifBlank { name }
            avatarUrl = me.data.avatarUrl
        }
        when (val result = professionalRepository.getMyProfile()) {
            is ApiResult.Success -> profile = result.data
            is ApiResult.Error -> Unit
        }
        loading = false
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "Profile",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Spacer(Modifier.height(20.dp))
        }

        if (loading) {
            item {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile photo",
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(PromoBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.firstOrNull()?.uppercase() ?: "P",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    fontFamily = sansProText
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                        profile?.let { pro ->
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = listOfNotNull(pro.city, pro.area).filter { it.isNotBlank() }.joinToString(", "),
                                fontSize = 13.sp,
                                color = MediumGray,
                                fontFamily = sansProText
                            )
                            if (pro.verificationStatus.equals("APPROVED", ignoreCase = true)) {
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .background(SuccessGreenLight, RoundedCornerShape(50))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.size(6.dp))
                                    Text("Verified Professional", fontSize = 12.sp, color = SuccessGreen, fontFamily = sansProText)
                                }
                            }
                            if (pro.services.isNotEmpty()) {
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    text = pro.services.joinToString(" • ") { it.categoryName.orEmpty() },
                                    fontSize = 13.sp,
                                    color = MediumGray,
                                    fontFamily = sansProText
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                WorkNearButton(
                    text = "Logout",
                    buttonType = WorkNearButtonType.OUTLINED,
                    onClick = onLogout,
                    icon = Icons.Default.Logout
                )
            }
        }
    }
}
