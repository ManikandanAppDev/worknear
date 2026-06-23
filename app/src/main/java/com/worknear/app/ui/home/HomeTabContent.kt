package com.worknear.app.ui.home

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.data.model.Professional
import com.worknear.app.data.model.ServiceCategory
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.LightGray
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.PromoBackground
import com.worknear.app.ui.theme.StarYellow
import com.worknear.app.ui.theme.sansProText

@Composable
fun HomeTabContent(
    modifier: Modifier = Modifier,
    onSeeAllCategories: () -> Unit,
    onCategoryClick: (ServiceCategory) -> Unit,
    onBookNow: () -> Unit,
    onProfessionalClick: (Professional) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        item {
            HomeTopBar(
                userName = uiState.userName,
                location = uiState.location
            )
        }

        item {
            HomeSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            CategoriesSection(
                categories = uiState.categories,
                onSeeAll = onSeeAllCategories,
                onCategoryClick = onCategoryClick,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            PromoBanner(
                onBookNow = onBookNow,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            PopularProfessionalsSection(
                professionals = uiState.professionals,
                onProfessionalClick = onProfessionalClick,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun HomeTopBar(userName: String, location: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) {
            Icon(Icons.Default.Menu, "Menu", tint = DarkText)
        }

        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.hello_user, userName),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = MediumGray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(location, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
            }
        }

        IconButton(onClick = {}) {
            Icon(Icons.Default.Notifications, "Notifications", tint = DarkText)
        }
    }
}

@Composable
private fun HomeSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(stringResource(R.string.search_service), color = LightGray, fontFamily = sansProText)
            },
            singleLine = true,
            shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp, topEnd = 0.dp, bottomEnd = 0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BorderGray,
                unfocusedBorderColor = BorderGray,
                focusedContainerColor = CardColor,
                unfocusedContainerColor = CardColor
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = sansProText)
        )

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                .background(PrimaryBlue)
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Search, "Search", tint = Color.White)
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<ServiceCategory>,
    onSeeAll: () -> Unit,
    onCategoryClick: (ServiceCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.categories),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Text(
                stringResource(R.string.see_all),
                modifier = Modifier.clickable(onClick = onSeeAll),
                color = PrimaryBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = sansProText
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            items(categories, key = { it.id }) { category ->
                CategoryItem(category = category, onClick = { onCategoryClick(category) })
            }
        }
    }
}

@Composable
private fun CategoryItem(category: ServiceCategory, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(category.imageRes),
            contentDescription = category.title,
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(category.title, fontSize = 13.sp, color = DarkText, fontFamily = sansProText)
    }
}

@Composable
private fun PromoBanner(onBookNow: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PromoBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.promo_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText,
                    fontFamily = sansProText,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onBookNow,
                    modifier = Modifier.width(120.dp).height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        stringResource(R.string.book_now),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = sansProText
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.avatar_five),
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun PopularProfessionalsSection(
    professionals: List<Professional>,
    onProfessionalClick: (Professional) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            stringResource(R.string.popular_professionals),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText
        )
        Spacer(Modifier.height(16.dp))
        professionals.forEach { professional ->
            ProfessionalCard(professional = professional, onClick = { onProfessionalClick(professional) })
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProfessionalCard(professional: Professional, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
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
                Text(professional.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = StarYellow, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.rating_format, professional.rating.toString(), professional.reviewCount),
                        fontSize = 13.sp,
                        color = MediumGray,
                        fontFamily = sansProText
                    )
                }
                Text(professional.profession, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
                Text(
                    stringResource(R.string.starts_from, professional.startingPrice),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText,
                    fontFamily = sansProText
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = MediumGray)
        }
    }
}
