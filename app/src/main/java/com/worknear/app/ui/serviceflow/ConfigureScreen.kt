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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.R
import com.worknear.app.data.model.ServiceCatalog
import com.worknear.app.ui.components.IconTile
import com.worknear.app.ui.components.PrimaryPillButton
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText

@Composable
fun ConfigureScreen(
    onNavigateBack: () -> Unit,
    onSeeMatches: () -> Unit
) {
    val categoryId = ServiceFlowState.categoryId
    val cartItems = ServiceCatalog.servicesFor(categoryId).mapNotNull { svc ->
        ServiceFlowState.cart[svc.id]
    }
    val total = ServiceFlowState.servicesTotal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(CardColor).statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkText)
                }
                Text(
                    "Configure",
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = sansProText
                )
                Spacer(Modifier.width(40.dp))
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            cartItems.forEach { cartItem ->
                item(key = cartItem.service.id) {
                    ConfigureCard(item = cartItem, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OptionTile(R.drawable.ic_wn_photo, "Add photos", "Optional", Modifier.weight(1f))
                    OptionTile(R.drawable.ic_wn_note, "Add notes", "Optional", Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(CardColor).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Estimated total", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
                Text("₹$total", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
            }
            PrimaryPillButton(
                text = "See Professionals",
                enabled = cartItems.isNotEmpty(),
                onClick = onSeeMatches
            )
        }
    }
}


@Composable
private fun ConfigureCard(item: CartItem, modifier: Modifier = Modifier) {
    val service = item.service
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(iconRes = service.iconRes, tileSize = 44.dp, iconSize = 22.dp)
            Spacer(Modifier.width(12.dp))
            Text(service.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText, modifier = Modifier.weight(1f))
            Text("₹${item.lineTotal}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, fontFamily = sansProText)
        }

        if (service.hasTypes) {
            Spacer(Modifier.height(14.dp))
            Text("Choose type", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                service.types.forEach { type ->
                    val selected = item.typeId == type.id
                    Text(
                        type.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selected) Color.White else DarkText,
                        fontFamily = sansProText,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) PrimaryBlue else Background)
                            .border(1.dp, if (selected) PrimaryBlue else BorderGray, RoundedCornerShape(50))
                            .clickable { ServiceFlowState.setType(service.id, type.id) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Quantity", fontSize = 13.sp, color = MediumGray, fontFamily = sansProText, modifier = Modifier.weight(1f))
            QuantityStepper(
                quantity = item.quantity,
                onDecrease = { ServiceFlowState.setQuantity(service.id, item.quantity - 1) },
                onIncrease = { ServiceFlowState.setQuantity(service.id, item.quantity + 1) }
            )
        }
    }
}

@Composable
private fun QuantityStepper(quantity: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Background)
                .border(1.dp, BorderGray, CircleShape)
                .clickable(onClick = onDecrease),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Remove, "Decrease", tint = MediumGray, modifier = Modifier.size(18.dp))
        }
        Text(
            "$quantity",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText,
            modifier = Modifier.width(40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(PrimaryBlue)
                .clickable(onClick = onIncrease),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, "Increase", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun OptionTile(iconRes: Int, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable {}
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconTile(iconRes = iconRes, tileSize = 44.dp, iconSize = 22.dp)
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
        Text(subtitle, fontSize = 11.sp, color = MediumGray, fontFamily = sansProText)
    }
}
