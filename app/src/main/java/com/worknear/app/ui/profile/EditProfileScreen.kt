package com.worknear.app.ui.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = copyImageToInternal(context, uri)
                if (saved != null) viewModel.onAvatarPicked(saved)
            }
        }
    }

    if (uiState.showPhoneSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closePhoneSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CardColor
        ) {
            ChangePhoneSheet(uiState = uiState, viewModel = viewModel)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
            }
            Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        }

        Spacer(Modifier.height(8.dp))

        // Avatar with edit badge
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
        ) {
            val avatar = uiState.avatarUri
            if (!avatar.isNullOrBlank()) {
                AsyncImage(
                    model = avatar,
                    contentDescription = "Profile photo",
                    modifier = Modifier.size(104.dp).clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, "Change photo", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Tap photo to change",
            fontSize = 12.sp,
            color = MediumGray,
            fontFamily = sansProText,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            FieldLabel("Full name")
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Your name", color = MediumGray, fontFamily = sansProText) },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(16.dp))

            FieldLabel("Email (optional)")
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("you@example.com", color = MediumGray, fontFamily = sansProText) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(16.dp))

            FieldLabel("Phone number")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardColor)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    uiState.phone.ifBlank { "Not set" },
                    color = DarkText,
                    fontFamily = sansProText,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Change",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = sansProText,
                    modifier = Modifier.clickable { viewModel.openPhoneSheet() }
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(uiState.errorMessage!!, color = ErrorRed, fontSize = 13.sp, fontFamily = sansProText)
            }
            if (uiState.savedMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(uiState.savedMessage!!, color = SuccessGreen, fontSize = 13.sp, fontFamily = sansProText)
            }

            Spacer(Modifier.height(28.dp))
            WorkNearButton(
                text = "Save changes",
                buttonType = WorkNearButtonType.FILLED,
                loading = uiState.isSaving,
                onClick = viewModel::saveProfile
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChangePhoneSheet(uiState: EditProfileUiState, viewModel: EditProfileViewModel) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Change phone number", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        Spacer(Modifier.height(8.dp))

        when (uiState.phoneStep) {
            PhoneChangeStep.ENTER_PHONE -> {
                Text(
                    "Enter your new mobile number. We'll send an OTP to verify it.",
                    fontSize = 14.sp, color = MediumGray, fontFamily = sansProText
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.newPhone,
                    onValueChange = viewModel::onNewPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g. 9876543210", color = MediumGray, fontFamily = sansProText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
                if (uiState.phoneError != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(uiState.phoneError!!, color = ErrorRed, fontSize = 13.sp, fontFamily = sansProText)
                }
                Spacer(Modifier.height(20.dp))
                WorkNearButton(
                    text = "Send OTP",
                    buttonType = WorkNearButtonType.FILLED,
                    loading = uiState.phoneSubmitting,
                    onClick = viewModel::requestPhoneOtp
                )
            }

            PhoneChangeStep.ENTER_OTP -> {
                Text(
                    "Enter the OTP sent to your new number.",
                    fontSize = 14.sp, color = MediumGray, fontFamily = sansProText
                )
                if (!uiState.devCode.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Dev code: ${uiState.devCode}", fontSize = 12.sp, color = PrimaryBlue, fontFamily = sansProText)
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.otpCode,
                    onValueChange = viewModel::onOtpChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("OTP", color = MediumGray, fontFamily = sansProText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
                if (uiState.phoneError != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(uiState.phoneError!!, color = ErrorRed, fontSize = 13.sp, fontFamily = sansProText)
                }
                Spacer(Modifier.height(20.dp))
                WorkNearButton(
                    text = "Verify & update",
                    buttonType = WorkNearButtonType.FILLED,
                    loading = uiState.phoneSubmitting,
                    onClick = viewModel::confirmPhoneChange
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Resend OTP",
                    color = PrimaryBlue,
                    fontSize = 13.sp,
                    fontFamily = sansProText,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { viewModel.requestPhoneOtp() }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MediumGray,
        fontFamily = sansProText,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = BorderGray,
    cursorColor = PrimaryBlue,
    focusedTextColor = DarkText,
    unfocusedTextColor = DarkText,
    focusedContainerColor = CardColor,
    unfocusedContainerColor = CardColor
)

/** Copies the picked image into app-internal storage and returns a file:// URI string. */
private suspend fun copyImageToInternal(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            null
        }
    }
