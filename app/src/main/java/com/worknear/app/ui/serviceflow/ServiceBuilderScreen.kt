package com.worknear.app.ui.serviceflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.data.model.CatalogService
import com.worknear.app.data.model.ServiceCatalog
import com.worknear.app.ui.components.IconTile
import com.worknear.app.ui.components.PrimaryPillButton
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.sansProText

@Composable
fun ServiceBuilderScreen(
    categoryId: String,
    onNavigateBack: () -> Unit,
    onConfigure: () -> Unit
) {
    LaunchedEffect(categoryId) { ServiceFlowState.startCategory(categoryId) }

    val title = ServiceCatalog.titleFor(categoryId)
    val services = remember(categoryId) { ServiceCatalog.servicesFor(categoryId) }

    val count = ServiceFlowState.selectedCount
    val total = ServiceFlowState.servicesTotal

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
                title,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Spacer(Modifier.size(40.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            item { Spacer(Modifier.height(12.dp)) }

            items(services, key = { it.id }) { service ->
                ServiceRow(
                    service = service,
                    selected = ServiceFlowState.isSelected(service.id),
                    onToggle = { ServiceFlowState.toggle(service) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }

        ConfigureBar(count = count, total = total, onConfigure = onConfigure)
    }
}

@Composable
private fun ServiceRow(
    service: CatalogService,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor)
            .border(
                1.dp,
                if (selected) PrimaryBlue.copy(alpha = 0.5f) else BorderGray,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(iconRes = service.iconRes, tileSize = 48.dp, iconSize = 24.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(service.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
            Spacer(Modifier.height(2.dp))
            Text(service.subtitle, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            Spacer(Modifier.height(4.dp))
            Text(
                (if (service.priceFrom) "From " else "") + "₹${service.price}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
        }
        AddButton(selected = selected, onClick = onToggle)
    }
}

@Composable
private fun AddButton(selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(SuccessGreen.copy(alpha = 0.14f))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Added", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SuccessGreen, fontFamily = sansProText)
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(PrimaryBlue.copy(alpha = 0.10f))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue, fontFamily = sansProText)
        }
    }
}

@Composable
private fun ConfigureBar(count: Int, total: Int, onConfigure: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardColor)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(iconRes = com.worknear.app.R.drawable.ic_wn_cart, tileSize = 44.dp, iconSize = 22.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (count == 0) "No services yet" else "$count service${if (count > 1) "s" else ""} · Est ₹$total",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Text("View selection", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
        }
        PrimaryPillButton(
            text = "Configure",
            enabled = count > 0,
            onClick = onConfigure
        )
    }
}
