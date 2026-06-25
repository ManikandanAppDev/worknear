package com.worknear.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.data.model.ServiceCatalog
import com.worknear.app.data.model.ServiceCategories
import com.worknear.app.data.model.ServiceCategory
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.home.HomeViewModel
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onOpenService: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val categories = uiState.categories.ifEmpty { ServiceCategories.default }
    val results = remember(query, categories) {
        if (query.isBlank()) {
            categories
        } else {
            categories.filter { category ->
                category.title.contains(query, ignoreCase = true) ||
                    ServiceCatalog.servicesFor(category.id)
                        .any { it.name.contains(query, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .focusRequester(focusRequester),
            placeholder = { Text("Search for a service...", fontFamily = sansProText, color = MediumGray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryBlue) },
            trailingIcon = {
                IconButton(onClick = { if (query.isEmpty()) onNavigateBack() else query = "" }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = MediumGray)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = PrimaryBlue.copy(alpha = 0.25f),
                cursorColor = PrimaryBlue,
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                focusedContainerColor = CardColor,
                unfocusedContainerColor = CardColor
            )
        )

        if (results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No services match \"$query\"",
                    color = MediumGray,
                    fontFamily = sansProText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(results, key = { it.id }) { category ->
                    SearchResultRow(
                        category = category,
                        onClick = { onOpenService(category.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(category: ServiceCategory, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(ServiceCatalog.iconFor(category.id)),
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            category.title,
            color = DarkText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = sansProText
        )
    }
}
