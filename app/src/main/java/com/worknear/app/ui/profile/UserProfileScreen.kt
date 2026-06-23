package com.worknear.app.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.data.model.ProfileMenuItem
import com.worknear.app.data.model.UserData
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.SuccessGreenLight
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val profile = UserData.profile
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, "Settings", tint = DarkText)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, "Notifications", tint = DarkText)
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(profile.imageRes),
                    contentDescription = profile.name,
                    modifier = Modifier.size(90.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    uiState.name.ifBlank { profile.name },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = sansProText
                )
                Text(
                    uiState.phone.ifBlank { profile.phone },
                    fontSize = 14.sp,
                    color = MediumGray,
                    fontFamily = sansProText
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.edit_profile),
                    color = PrimaryBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = sansProText,
                    modifier = Modifier.clickable {}
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }

        items(UserData.menuItems, key = { it.id }) { item ->
            ProfileMenuRow(
                item = item,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        item {
            Spacer(Modifier.height(24.dp))
            WorkNearButton(
                text = "Log out",
                buttonType = WorkNearButtonType.OUTLINED,
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick = { viewModel.logout(onLogout) }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileMenuRow(item: ProfileMenuItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable {},
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.title, modifier = Modifier.weight(1f), color = DarkText, fontFamily = sansProText)
            if (item.badge != null) {
                Text(
                    item.badge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreenLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Medium,
                    fontFamily = sansProText
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.Default.ChevronRight, null, tint = MediumGray)
        }
    }
}
