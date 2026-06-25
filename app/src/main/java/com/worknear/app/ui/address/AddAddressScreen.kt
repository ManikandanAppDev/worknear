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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.R
import com.worknear.app.ui.components.PrimaryPillButton
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText

@Composable
fun AddAddressScreen(
    addressId: String?,
    onNavigateBack: () -> Unit
) {
    val existing = remember(addressId) { AddressStore.byId(addressId) }
    val isEdit = existing != null

    var type by remember { mutableStateOf(existing?.type ?: AddressType.HOME) }
    var fullAddress by remember { mutableStateOf(existing?.let { "${it.line1}, ${it.cityState}" } ?: "") }
    var landmark by remember { mutableStateOf(existing?.landmark ?: "") }
    var setDefault by remember { mutableStateOf(existing?.isDefault ?: false) }

    val canSave = fullAddress.isNotBlank()

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
                if (isEdit) "Edit Address" else "Add New Address",
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Spacer(Modifier.width(40.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            SectionLabel("Address Type")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddressType.entries.forEach { t ->
                    AddressTypeTile(
                        type = t,
                        selected = t == type,
                        modifier = Modifier.weight(1f),
                        onClick = { type = t }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Full Address")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = fullAddress,
                onValueChange = { fullAddress = it },
                modifier = Modifier.fillMaxWidth().height(96.dp),
                placeholder = { Text("House / Flat no, Street, Area, City - PIN", color = MediumGray, fontSize = 14.sp, fontFamily = sansProText) },
                textStyle = TextStyle(color = DarkText, fontSize = 14.sp, fontFamily = sansProText),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderGray,
                    cursorColor = PrimaryBlue
                )
            )
            Spacer(Modifier.height(10.dp))
            UseCurrentLocationChip(onClick = {
                fullAddress = "Current location, Chennai - 600001"
            })

            Spacer(Modifier.height(20.dp))
            SectionLabel("Landmark (Optional)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = landmark,
                onValueChange = { landmark = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Nearby landmark for easy identification", color = MediumGray, fontSize = 14.sp, fontFamily = sansProText) },
                textStyle = TextStyle(color = DarkText, fontSize = 14.sp, fontFamily = sansProText),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderGray,
                    cursorColor = PrimaryBlue
                )
            )

            Spacer(Modifier.height(20.dp))
            DefaultCheckboxRow(checked = setDefault, onToggle = { setDefault = !setDefault })
            Spacer(Modifier.height(16.dp))
        }

        Column(modifier = Modifier.fillMaxWidth().background(CardColor).padding(20.dp)) {
            PrimaryPillButton(
                text = "Save Address",
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave,
                onClick = {
                    val (line1, cityState) = splitAddress(fullAddress)
                    if (isEdit && existing != null) {
                        AddressStore.update(
                            existing.copy(type = type, line1 = line1, cityState = cityState, landmark = landmark, isDefault = setDefault)
                        )
                    } else {
                        AddressStore.add(
                            SavedAddress(
                                id = AddressStore.newId(),
                                type = type,
                                line1 = line1,
                                cityState = cityState,
                                landmark = landmark,
                                isDefault = setDefault
                            )
                        )
                    }
                    onNavigateBack()
                }
            )
        }
    }
}

/** Splits a free-text address into a primary line and a city/state line for display. */
private fun splitAddress(input: String): Pair<String, String> {
    val trimmed = input.trim()
    val lastComma = trimmed.lastIndexOf(',')
    return if (lastComma in 1 until trimmed.length - 1) {
        trimmed.substring(0, lastComma).trim() to trimmed.substring(lastComma + 1).trim()
    } else {
        trimmed to ""
    }
}

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

@Composable
private fun DefaultCheckboxRow(checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) PrimaryBlue else CardColor)
                .border(1.5.dp, if (checked) PrimaryBlue else BorderGray, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text("Set as default address", fontSize = 14.sp, color = DarkText, fontFamily = sansProText)
    }
}
