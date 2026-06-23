package com.worknear.app.ui.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.data.model.Transaction
import com.worknear.app.data.model.TransactionType
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearScreenTitle
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.sansProText

@Composable
fun WalletScreen(
    modifier: Modifier = Modifier,
    viewModel: WalletViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        WorkNearScreenTitle(title = stringResource(R.string.wallet))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.available_balance),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontFamily = sansProText
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "₹${String.format("%.2f", uiState.balance)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = sansProText
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.add_money), color = PrimaryBlue, fontFamily = sansProText)
                    }
                }
                Image(
                    painter = painterResource(R.drawable.avatar_five),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            stringResource(R.string.recent_transactions),
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.transactions.isEmpty()) {
                item {
                    Text(
                        if (uiState.isLoading) "Loading…" else "No transactions yet",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        color = MediumGray,
                        fontFamily = sansProText
                    )
                }
            }
            items(uiState.transactions, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, fontWeight = FontWeight.Medium, color = DarkText, fontFamily = sansProText)
                Text(transaction.date, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            }
            Text(
                text = if (transaction.type == TransactionType.CREDIT) {
                    "+₹${transaction.amount}"
                } else {
                    "-₹${transaction.amount}"
                },
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.type == TransactionType.CREDIT) SuccessGreen else ErrorRed,
                fontFamily = sansProText
            )
        }
    }
}
