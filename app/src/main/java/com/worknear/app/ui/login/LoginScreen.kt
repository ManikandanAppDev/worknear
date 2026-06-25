package com.worknear.app.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearAlert
import com.worknear.app.ui.components.WorkNearAlertType
import com.worknear.app.ui.components.WorkNearTextField
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.WorkNearTheme
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: (newUser: Boolean, role: String?) -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LoginContent(
        uiState = uiState,
        onNavigateBack = {
            if (uiState.step == LoginStep.OTP) viewModel.backToPhone() else onNavigateBack()
        },
        onPhoneChange = viewModel::onPhoneChange,
        onOtpChange = viewModel::onOtpChange,
        onPrimaryClick = {
            when (uiState.step) {
                LoginStep.PHONE -> viewModel.requestOtp()
                LoginStep.OTP -> viewModel.verifyOtp(onLoginSuccess)
            }
        },
        onResend = viewModel::requestOtp
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onNavigateBack: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onPrimaryClick: () -> Unit,
    onResend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val isPhoneStep = uiState.step == LoginStep.PHONE

        Text(
            text = if (isPhoneStep) "Enter your mobile number" else "Verify OTP",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isPhoneStep) {
                "We'll send a one-time password to confirm your number"
            } else {
                "Enter the code sent to ${uiState.phone}"
            },
            fontSize = 16.sp,
            color = MediumGray,
            fontFamily = sansProText
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (isPhoneStep) {
            WorkNearTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                placeholder = "10-digit mobile number",
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                )
            )
        } else {
            WorkNearTextField(
                value = uiState.otp,
                onValueChange = onOtpChange,
                placeholder = "6-digit OTP",
                leadingIcon = Icons.Default.Sms,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            if (uiState.devCode != null) {
                Spacer(modifier = Modifier.height(8.dp))
                WorkNearAlert(
                    message = "Dev mode OTP: ${uiState.devCode}",
                    type = WorkNearAlertType.INFO
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Resend OTP",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = onResend),
                color = PrimaryBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = sansProText
            )
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            WorkNearAlert(
                message = uiState.errorMessage,
                type = WorkNearAlertType.ERROR
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        WorkNearButton(
            text = if (isPhoneStep) "Send OTP" else "Verify & Continue",
            buttonType = WorkNearButtonType.FILLED,
            loading = uiState.isLoading,
            onClick = onPrimaryClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    WorkNearTheme {
        LoginContent(
            uiState = LoginUiState(step = LoginStep.OTP, phone = "+919000000001", devCode = "4821"),
            onNavigateBack = {},
            onPhoneChange = {},
            onOtpChange = {},
            onPrimaryClick = {},
            onResend = {}
        )
    }
}
