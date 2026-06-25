package com.worknear.app.ui.prodashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.worknear.app.data.repository.ProWorkspaceStore
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.PromoBackground
import com.worknear.app.ui.theme.sansProText

@Composable
fun ProEarningsTab(
    modifier: Modifier = Modifier,
    proWorkspaceStore: ProWorkspaceStore
) {
    val completedJobs by proWorkspaceStore.completedJobs.collectAsStateWithLifecycle()
    val isRefreshing by proWorkspaceStore.isRefreshing.collectAsStateWithLifecycle()

    val totalEarnings = completedJobs.sumOf { if (it.proEarning > 0) it.proEarning else it.price }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
    ) {
        item {
            Text(
                text = "Earnings",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Track payments released after job completion",
                fontSize = 14.sp,
                color = MediumGray,
                fontFamily = sansProText
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(PromoBackground, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PrimaryBlue)
                    }
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text("Total earned", fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
                        Text(
                            text = "₹$totalEarnings",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            fontFamily = sansProText
                        )
                        Text(
                            text = "${completedJobs.size} completed jobs",
                            fontSize = 12.sp,
                            color = MediumGray,
                            fontFamily = sansProText
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        when {
            isRefreshing && completedJobs.isEmpty() -> item {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            completedJobs.isEmpty() -> item {
                Text("Completed jobs with released payments will show here.", color = MediumGray, fontFamily = sansProText)
            }
            else -> {
                item {
                    Text(
                        text = "Recent payouts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontFamily = sansProText
                    )
                    Spacer(Modifier.height(12.dp))
                }
                items(completedJobs.take(20), key = { it.uuid }) { job ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(job.serviceName, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
                            Text("${job.date} • ${job.time}", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
                        }
                        Text(
                            text = "+₹${if (job.proEarning > 0) job.proEarning else job.price}",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            fontFamily = sansProText
                        )
                    }
                }
            }
        }
    }
}
