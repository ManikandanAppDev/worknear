package com.worknear.app.ui.address

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.sansProText

@Composable
fun ManageAddressesScreen(
    onNavigateBack: () -> Unit,
    onEditAddress: (String) -> Unit,
    onAddNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardColor)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkText)
            }
            Text(
                "Manage Addresses",
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Spacer(Modifier.width(40.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AddressStore.addresses.forEach { address ->
                item(key = address.id) {
                    ManageAddressCard(
                        address = address,
                        onEdit = { onEditAddress(address.id) },
                        onSetDefault = { AddressStore.setDefault(address.id) },
                        onDelete = { AddressStore.delete(address.id) }
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().background(CardColor).padding(20.dp)) {
            val canAddMore = AddressStore.canAddMore
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .border(1.5.dp, if (canAddMore) PrimaryBlue else BorderGray, RoundedCornerShape(50))
                    .clickable(enabled = canAddMore, onClick = onAddNew),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+ Add New Address",
                    color = if (canAddMore) PrimaryBlue else MediumGray,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    fontFamily = sansProText
                )
            }
            if (!canAddMore) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Limit reached — you can save up to ${AddressStore.MAX_ADDRESSES} addresses.",
                    fontSize = 12.sp,
                    color = MediumGray,
                    fontFamily = sansProText
                )
            }
        }
    }
}

@Composable
private fun ManageAddressCard(
    address: SavedAddress,
    onEdit: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(address.line1, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
                Spacer(Modifier.height(2.dp))
                Text(address.cityState, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
                if (address.landmark.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text("Landmark: ${address.landmark}", fontSize = 11.sp, color = MediumGray, fontFamily = sansProText)
                }
            }
            AddressTypeBadge(address.type)
        }

        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderGray.copy(alpha = 0.6f)))
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
            ActionItem(Icons.Default.Edit, "Edit", PrimaryBlue, onEdit)
            if (address.isDefault) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Default", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SuccessGreen, fontFamily = sansProText)
                }
            } else {
                ActionItem(Icons.Default.Star, "Set as Default", MediumGray, onSetDefault)
            }
            Spacer(Modifier.weight(1f))
            ActionItem(Icons.Default.Delete, "Delete", ErrorRed, onDelete)
        }
    }
}

@Composable
private fun ActionItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color, fontFamily = sansProText)
    }
}
