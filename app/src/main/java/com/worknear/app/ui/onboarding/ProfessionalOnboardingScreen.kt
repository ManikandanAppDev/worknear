package com.worknear.app.ui.onboarding

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearAlert
import com.worknear.app.ui.components.WorkNearAlertType
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
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

private data class DocSlot(val type: String, val label: String, val required: Boolean)

private val DOCUMENT_SLOTS = listOf(
    DocSlot("GOV_ID", "Government ID (Aadhaar / PAN)", required = true),
    DocSlot("ADDRESS_PROOF", "Address proof", required = false),
    DocSlot("WORK_CERTIFICATE", "Work certificate / skill proof", required = false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalOnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: ProfessionalOnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pickingType by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val type = pickingType
        if (uri != null && type != null) {
            scope.launch {
                val copied = copyDocToInternal(context, uri, type)
                if (copied != null) {
                    viewModel.onDocumentPicked(
                        ProDocumentPick(
                            type = type,
                            file = copied.first,
                            mimeType = copied.second,
                            displayName = copied.first.name
                        )
                    )
                }
            }
        }
        pickingType = null
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
            if (uiState.step == ProOnboardStep.DOCUMENTS) {
                IconButton(onClick = viewModel::backToProfile) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkText)
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.step == ProOnboardStep.PROFILE) "Set up your profile" else "Verify your documents",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
        }

        StepIndicator(step = uiState.step, modifier = Modifier.padding(horizontal = 20.dp))

        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            when (uiState.step) {
                ProOnboardStep.PROFILE -> ProfileStep(uiState, viewModel)
                ProOnboardStep.DOCUMENTS -> DocumentsStep(uiState) { type ->
                    pickingType = type
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(14.dp))
                WorkNearAlert(message = uiState.errorMessage!!, type = WorkNearAlertType.ERROR)
            }

            Spacer(Modifier.height(24.dp))

            when (uiState.step) {
                ProOnboardStep.PROFILE -> WorkNearButton(
                    text = "Continue",
                    buttonType = WorkNearButtonType.FILLED,
                    loading = uiState.isSubmitting,
                    onClick = viewModel::saveProfileAndContinue
                )
                ProOnboardStep.DOCUMENTS -> WorkNearButton(
                    text = "Submit for verification",
                    buttonType = WorkNearButtonType.FILLED,
                    loading = uiState.isSubmitting,
                    onClick = { viewModel.submit(onCompleted) }
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun StepIndicator(step: ProOnboardStep, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StepBar(active = true, modifier = Modifier.weight(1f))
        StepBar(active = step == ProOnboardStep.DOCUMENTS, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StepBar(active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (active) PrimaryBlue else BorderGray)
    )
}

@Composable
private fun ProfileStep(uiState: ProfessionalOnboardingUiState, viewModel: ProfessionalOnboardingViewModel) {
    FieldLabel("Full name")
    ProField(uiState.fullName, viewModel::onNameChange, "Your name")

    Spacer(Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            FieldLabel("City")
            ProField(uiState.city, viewModel::onCityChange, "City")
        }
        Column(Modifier.weight(1f)) {
            FieldLabel("Experience (yrs)")
            ProField(
                uiState.experience,
                viewModel::onExperienceChange,
                "e.g. 5",
                keyboardType = KeyboardType.Number
            )
        }
    }

    Spacer(Modifier.height(14.dp))
    FieldLabel("Area / locality")
    ProField(uiState.area, viewModel::onAreaChange, "Area you serve")

    Spacer(Modifier.height(14.dp))
    FieldLabel("About you (optional)")
    ProField(uiState.bio, viewModel::onBioChange, "Tell customers about your work", singleLine = false)

    Spacer(Modifier.height(20.dp))
    FieldLabel("Services you offer")
    Text(
        "Select all that apply. Customers will find you under these services.",
        fontSize = 12.sp,
        color = MediumGray,
        fontFamily = sansProText,
        modifier = Modifier.padding(bottom = 10.dp)
    )

    if (uiState.isLoading) {
        Text("Loading services…", fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
    } else {
        // Render selectable chips in fixed rows (stable replacement for FlowRow).
        uiState.categories.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { category ->
                    CategoryChip(
                        name = category.name,
                        selected = uiState.selectedCategoryIds.contains(category.id),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.toggleCategory(category.id) }
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategoryChip(name: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) PrimaryBlue else CardColor)
            .border(
                BorderStroke(1.dp, if (selected) PrimaryBlue else BorderGray),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else DarkText,
            fontFamily = sansProText
        )
    }
}

@Composable
private fun DocumentsStep(uiState: ProfessionalOnboardingUiState, onPick: (String) -> Unit) {
    Text(
        "Upload clear photos of your documents. We use these to verify you before you start getting jobs.",
        fontSize = 14.sp,
        color = MediumGray,
        fontFamily = sansProText,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    DOCUMENT_SLOTS.forEach { slot ->
        val picked = uiState.documents[slot.type]
        DocumentRow(
            label = slot.label,
            required = slot.required,
            uploadedName = picked?.displayName,
            onClick = { onPick(slot.type) }
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DocumentRow(label: String, required: Boolean, uploadedName: String?, onClick: () -> Unit) {
    val done = uploadedName != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardColor)
            .border(BorderStroke(1.dp, if (done) SuccessGreen else BorderGray), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background((if (done) SuccessGreen else PrimaryBlue).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                contentDescription = null,
                tint = if (done) SuccessGreen else PrimaryBlue
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = label + if (required) " *" else " (optional)",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                fontFamily = sansProText
            )
            Text(
                text = if (done) "Selected — tap to replace" else "Tap to upload",
                fontSize = 12.sp,
                color = if (done) SuccessGreen else MediumGray,
                fontFamily = sansProText
            )
        }
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
private fun ProField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        placeholder = { Text(placeholder, color = MediumGray, fontFamily = sansProText) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = BorderGray,
            cursorColor = PrimaryBlue,
            focusedTextColor = DarkText,
            unfocusedTextColor = DarkText,
            focusedContainerColor = CardColor,
            unfocusedContainerColor = CardColor
        )
    )
}

/** Copies the picked document image into app-internal storage; returns the file and its MIME type. */
private suspend fun copyDocToInternal(context: Context, uri: Uri, type: String): Pair<File, String?>? =
    withContext(Dispatchers.IO) {
        try {
            val mime = context.contentResolver.getType(uri)
            val ext = when {
                mime?.contains("png") == true -> "png"
                mime?.contains("webp") == true -> "webp"
                else -> "jpg"
            }
            val file = File(context.filesDir, "doc_${type}_${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file to (mime ?: "image/jpeg")
        } catch (e: Exception) {
            null
        }
    }
