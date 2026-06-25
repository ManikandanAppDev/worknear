package com.worknear.app.ui.serviceflow

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.data.model.ServiceCatalog
import com.worknear.app.data.model.ServiceCategory
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.IconTile
import com.worknear.app.ui.home.HomeViewModel
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText

@Composable
fun AllServicesScreen(
    onNavigateBack: () -> Unit,
    onOpenService: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = uiState.categories

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

        when {
            categories.isEmpty() && uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(32.dp)) }

            categories.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Services aren't available right now. Please try again later.",
                    fontSize = 14.sp,
                    color = MediumGray,
                    fontFamily = sansProText,
                    textAlign = TextAlign.Center
                )
            }

            else -> LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                categories.chunked(4).forEachIndexed { index, rowCategories ->
                    item(key = "row-$index") {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowCategories.forEach { category ->
                                CategoryTile(
                                    category = category,
                                    onClick = {
                                        ServiceFlowState.startCategory(category.id)
                                        onOpenService(category.id)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(4 - rowCategories.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: ServiceCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconTile(iconRes = ServiceCatalog.iconFor(category.id), tileSize = 58.dp, iconSize = 28.dp, cornerRadius = 18.dp)
        Spacer(Modifier.height(8.dp))
        Text(
            category.title,
            fontSize = 11.sp,
            color = DarkText,
            fontFamily = sansProText,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}
