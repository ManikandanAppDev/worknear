package com.worknear.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.worknear.app.R
import com.worknear.app.data.remote.dto.BannerDto
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.onboarding.OnboardingViewModel
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.Typography
import com.worknear.app.ui.theme.WorkNearTheme
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType
import kotlinx.coroutines.delay

private const val DEFAULT_HEADLINE = "We'll help you,\nalways"
private const val DEFAULT_SUBTITLE = "Book trusted professionals for your home services"

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingContent(banners = uiState.banners, onGetStarted = onGetStarted)
}

@Composable
private fun OnboardingContent(
    banners: List<BannerDto>,
    onGetStarted: () -> Unit
) {
    val pageCount = banners.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Auto-advance the carousel while there is more than one slide.
    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(3500)
                val next = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (banners.isEmpty()) {
                Image(
                    painter = painterResource(R.drawable.bg_onboard),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = banners[page].imageUrl,
                        contentDescription = banners[page].title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            val current = banners.getOrNull(pagerState.currentPage)

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
            ) {
                Text(
                    text = current?.title?.takeIf { it.isNotBlank() } ?: DEFAULT_HEADLINE,
                    color = Color.White,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = sansProText
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = current?.subtitle?.takeIf { it.isNotBlank() } ?: DEFAULT_SUBTITLE,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontFamily = sansProText
                )

                Spacer(modifier = Modifier.height(20.dp))

                OnboardingPagination(
                    count = banners.size.coerceAtLeast(1),
                    activeIndex = pagerState.currentPage
                )
            }
        }

        BottomCard(onGetStarted = onGetStarted)
    }
}

@Composable
private fun OnboardingPagination(count: Int, activeIndex: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            if (index == activeIndex) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PrimaryBlue)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun BottomCard(
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        WorkNearButton(
            text = stringResource(R.string.get_started),
            buttonType = WorkNearButtonType.FILLED,
            onClick = onGetStarted
        )

        Spacer(modifier = Modifier.height(28.dp))

        TrustedUsers()

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun TrustedUsers() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Trusted by 10L+ users",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Gray,
            fontSize = 14.sp,
            fontFamily = sansProText,
            style = Typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val avatars = listOf(
                R.drawable.avatar_one,
                R.drawable.avatar_two,
                R.drawable.avatar_three,
                R.drawable.avatar_four,
                R.drawable.avatar_five
            )
            avatars.forEach { avatar ->
                Image(
                    painter = painterResource(avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(38.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    WorkNearTheme {
        OnboardingContent(banners = emptyList(), onGetStarted = {})
    }
}
