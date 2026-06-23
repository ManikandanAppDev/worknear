package com.worknear.app.ui.servicelist

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.data.model.Professional
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearTopBar
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.LightGray
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.StarYellow
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

enum class ServiceFilter(val label: String) {
    RECOMMENDED("Recommended"),
    NEAREST("Nearest"),
    TOP_RATED("Top Rated"),
    LOW_PRICE("Low Price")
}

@Composable
fun ServiceListScreen(
    categoryId: String,
    onNavigateBack: () -> Unit,
    onProfessionalClick: (String) -> Unit,
    onBookNow: (String) -> Unit,
    viewModel: ServiceListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(categoryId) { viewModel.load(categoryId) }

    val title = uiState.title
    val professionals = uiState.professionals
    var selectedFilter by remember { mutableStateOf(ServiceFilter.RECOMMENDED) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            WorkNearTopBar(title = title, onBackClick = onNavigateBack)
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(stringResource(R.string.search_service), color = LightGray, fontFamily = sansProText)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderGray,
                            unfocusedBorderColor = BorderGray,
                            focusedContainerColor = CardColor,
                            unfocusedContainerColor = CardColor
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = sansProText)
                    )
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, "Filter", tint = DarkText)
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ServiceFilter.entries) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(filter.label, fontFamily = sansProText, fontSize = 13.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = CardColor,
                                labelColor = DarkText
                            )
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            if (professionals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        } else {
                            Text(
                                uiState.errorMessage ?: "No professionals available yet",
                                color = MediumGray,
                                textAlign = TextAlign.Center,
                                fontFamily = sansProText
                            )
                        }
                    }
                }
            }

            items(professionals, key = { it.id }) { professional ->
                ServiceListProfessionalCard(
                    professional = professional,
                    onClick = { onProfessionalClick(professional.id) },
                    onBookNow = { onBookNow(professional.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ServiceListProfessionalCard(
    professional: Professional,
    onClick: () -> Unit,
    onBookNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(professional.imageRes),
                contentDescription = professional.name,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(professional.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText, fontFamily = sansProText)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = StarYellow, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${professional.rating} • ${professional.experienceYears} yrs exp",
                        fontSize = 13.sp,
                        color = MediumGray,
                        fontFamily = sansProText
                    )
                }
                Text(
                    stringResource(R.string.starts_from, professional.startingPrice),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText,
                    fontFamily = sansProText
                )
            }
            WorkNearButton(
                text = stringResource(R.string.book_now),
                buttonType = WorkNearButtonType.FILLED,
                modifier = Modifier.width(100.dp).height(40.dp),
                onClick = onBookNow
            )
        }
    }
}
