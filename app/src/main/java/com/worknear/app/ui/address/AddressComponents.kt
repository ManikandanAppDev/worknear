package com.worknear.app.ui.address

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.WarningAmber
import com.worknear.app.ui.theme.sansProText

/** Small coloured pill showing the address type (Home / Work / Other). */
@Composable
fun AddressTypeBadge(type: AddressType) {
    val color = when (type) {
        AddressType.HOME -> PrimaryBlue
        AddressType.WORK -> SuccessGreen
        AddressType.OTHER -> WarningAmber
    }
    Text(
        type.label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        fontFamily = sansProText,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** A selectable saved-address row used inside the Select Address bottom sheet. */
@Composable
fun SelectableAddressRow(
    address: SavedAddress,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) PrimaryBlue else BorderGray,
                RoundedCornerShape(14.dp)
            )
            .background(if (selected) PrimaryBlue.copy(alpha = 0.04f) else CardColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(address.line1, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
            Spacer(Modifier.height(2.dp))
            Text(address.cityState, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
        }
        Spacer(Modifier.width(8.dp))
        AddressTypeBadge(address.type)
        Spacer(Modifier.width(6.dp))
    }
}

/** Content of the "Select Address" bottom sheet shown from the home address pill. */
@Composable
fun SelectAddressSheetContent(
    onSelect: (String) -> Unit,
    onAddNew: () -> Unit,
    onManage: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Select Address", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AddressStore.addresses.forEach { address ->
                SelectableAddressRow(
                    address = address,
                    selected = address.id == AddressStore.selectedId,
                    onClick = { onSelect(address.id) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
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
            Spacer(Modifier.height(6.dp))
            Text(
                "You can save up to ${AddressStore.MAX_ADDRESSES} addresses. Delete one to add a new address.",
                fontSize = 12.sp,
                color = MediumGray,
                fontFamily = sansProText
            )
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onManage)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage Addresses", color = MediumGray, fontWeight = FontWeight.Medium, fontSize = 14.sp, fontFamily = sansProText)
        }
        Spacer(Modifier.height(12.dp))
    }
}
