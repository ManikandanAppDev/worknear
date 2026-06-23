package com.worknear.app.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearTopBar
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType
import androidx.compose.ui.res.stringResource

@Composable
fun BookServiceScreen(
    professionalId: String,
    onNavigateBack: () -> Unit,
    onContinue: (String) -> Unit,
    viewModel: BookServiceViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val bookData by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(professionalId) { viewModel.load(professionalId) }

    Scaffold(
        topBar = {
            WorkNearTopBar(title = stringResource(R.string.book_service), onBackClick = onNavigateBack)
        },
        containerColor = Background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardColor)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.total),
                            fontSize = 12.sp,
                            color = MediumGray,
                            fontFamily = sansProText
                        )
                        Text(
                            "₹${bookData.price}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText,
                            fontFamily = sansProText
                        )
                    }
                    Text(
                        stringResource(R.string.view_details),
                        color = PrimaryBlue,
                        fontSize = 14.sp,
                        fontFamily = sansProText
                    )
                }
                if (bookData.errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        bookData.errorMessage!!,
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontFamily = sansProText
                    )
                }
                Spacer(Modifier.height(12.dp))
                WorkNearButton(
                    text = stringResource(R.string.continue_btn),
                    buttonType = WorkNearButtonType.FILLED,
                    loading = bookData.isSubmitting,
                    onClick = { viewModel.submit(onContinue) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            BookingStepper(activeStep = 1)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BookingDetailRow(stringResource(R.string.service_label), bookData.serviceName)
                    HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
                    BookingDetailRow(stringResource(R.string.professional_label), bookData.professionalName)
                    HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
                    BookingDetailRow(stringResource(R.string.date_label), bookData.date)
                    HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
                    BookingDetailRow(stringResource(R.string.time_slot_label), bookData.timeSlot)
                    HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
                    Column {
                        Text(
                            stringResource(R.string.problem_description),
                            fontSize = 14.sp,
                            color = MediumGray,
                            fontFamily = sansProText
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bookData.problemDescription,
                            onValueChange = viewModel::onDescriptionChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Describe the problem (optional)",
                                    color = MediumGray,
                                    fontFamily = sansProText
                                )
                            },
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderGray
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = sansProText, color = DarkText)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingStepper(activeStep: Int) {
    val steps = listOf(
        stringResource(R.string.step_service),
        stringResource(R.string.step_details),
        stringResource(R.string.step_confirm)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, step ->
            val stepNumber = index + 1
            val isActive = stepNumber == activeStep
            val isCompleted = stepNumber < activeStep
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$stepNumber",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive || isCompleted) CardColor else MediumGray,
                    modifier = Modifier
                        .background(
                            if (isActive || isCompleted) PrimaryBlue else BorderGray,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    fontFamily = sansProText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    step,
                    fontSize = 11.sp,
                    color = if (isActive) PrimaryBlue else MediumGray,
                    fontFamily = sansProText
                )
            }
        }
    }
}

@Composable
private fun BookingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, color = DarkText, fontWeight = FontWeight.Medium, fontFamily = sansProText)
        }
        Text(stringResource(R.string.change), color = PrimaryBlue, fontSize = 14.sp, fontFamily = sansProText)
    }
}
