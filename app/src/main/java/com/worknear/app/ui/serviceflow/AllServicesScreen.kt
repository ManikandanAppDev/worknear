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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.data.model.CatalogService
import com.worknear.app.data.model.ServiceCatalog
import com.worknear.app.ui.components.IconTile
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.sansProText

@Composable
fun AllServicesScreen(
    onNavigateBack: () -> Unit,
    onOpenService: (String) -> Unit
) {
    val categories = ServiceCatalog.homeTiles

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(CardColor).statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkText)
            }
            Text(
                "All Services",
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Spacer(Modifier.size(40.dp))
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories.forEach { tile ->
                item(key = tile.categoryId) {
                    val label = if (tile.categoryId == "more") "More Services" else tile.label
                    CategorySection(
                        label = label,
                        services = ServiceCatalog.servicesFor(tile.categoryId),
                        onServiceClick = { service ->
                            ServiceFlowState.startCategory(tile.categoryId)
                            ServiceFlowState.ensureSelected(service)
                            onOpenService(tile.categoryId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySection(
    label: String,
    services: List<CatalogService>,
    onServiceClick: (CatalogService) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            services.chunked(4).forEach { rowServices ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowServices.forEach { service ->
                        SubServiceTile(
                            service = service,
                            onClick = { onServiceClick(service) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - rowServices.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun SubServiceTile(
    service: CatalogService,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconTile(iconRes = service.iconRes, tileSize = 54.dp, iconSize = 26.dp, cornerRadius = 16.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            shortLabel(service.name),
            fontSize = 11.sp,
            color = DarkText,
            fontFamily = sansProText,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

/** Trims long service names for the compact grid label. */
private fun shortLabel(name: String): String = when (name) {
    "Bathroom cleaning" -> "Bathroom"
    "Kitchen cleaning" -> "Kitchen"
    "Full home cleaning" -> "Full home"
    "Sofa cleaning" -> "Sofa"
    "Bedroom cleaning" -> "Bedroom"
    "AC service" -> "AC service"
    "AC installation" -> "Installation"
    "AC gas refill" -> "Gas refill"
    "AC repair" -> "Repair"
    "AC deep clean" -> "Deep clean"
    "Tap repair" -> "Tap repair"
    "Pipe leakage" -> "Pipe leakage"
    "Geyser installation" -> "Geyser install"
    "Wash basin work" -> "Wash basin"
    "Toilet / flush repair" -> "Toilet/flush"
    "Fan installation" -> "Fan"
    "Light work" -> "Light"
    "Switch replacement" -> "Switch"
    "Wiring inspection" -> "Wiring"
    "MCB / Fuse replacement" -> "MCB"
    "Wall socket installation" -> "Wall socket"
    "Interior painting" -> "Interior"
    "Exterior painting" -> "Exterior"
    "Wall texture / POP" -> "Texture/POP"
    "Waterproofing" -> "Waterproofing"
    "Touch-up painting" -> "Touch-up"
    "Washing machine" -> "Washing machine"
    "Refrigerator" -> "Refrigerator"
    "Microwave" -> "Microwave"
    "Television" -> "Television"
    "Geyser repair" -> "Geyser"
    "Furniture repair" -> "Furniture"
    "Door repair" -> "Door"
    "Bed / cot work" -> "Bed/cot"
    "Drawer & locks" -> "Drawer & locks"
    "Shelf installation" -> "Shelf"
    "General repair" -> "General repair"
    "Home deep clean" -> "Home deep clean"
    "Appliance install" -> "Appliance install"
    "Electrical check" -> "Electrical check"
    else -> name
}
