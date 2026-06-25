package com.worknear.app.ui.projobs

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worknear.app.R
import com.worknear.app.data.model.Booking
import com.worknear.app.data.model.BookingStatus
import com.worknear.app.di.AppViewModelProvider
import com.worknear.app.ui.components.WorkNearAlert
import com.worknear.app.ui.components.WorkNearAlertType
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.ErrorRed
import com.worknear.app.ui.theme.ErrorRedLight
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.PromoBackground
import com.worknear.app.ui.theme.SuccessGreen
import com.worknear.app.ui.theme.SuccessGreenLight
import com.worknear.app.ui.theme.WarningAmberLight
import com.worknear.app.ui.theme.sansProText
import com.worknear.app.utils.WorkNearButton
import com.worknear.app.utils.WorkNearButtonType

@Composable
fun ProfessionalJobsScreen(
    modifier: Modifier = Modifier,
    embedded: Boolean = false,
    onLogout: () -> Unit,
    viewModel: ProfessionalJobsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .then(if (embedded) Modifier else Modifier.statusBarsPadding())
            .padding(20.dp)
    ) {
        if (!embedded) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_wn_professional),
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Column(Modifier.weight(1f)) {
                    Text("Professional jobs", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                    Text("Update job status from here", fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
                }
                Text(
                    "Logout",
                    modifier = Modifier.clickable(onClick = onLogout),
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = sansProText
                )
            }
            Spacer(Modifier.height(16.dp))

            val status = state.verificationStatus
            if (status != null && !status.equals("APPROVED", ignoreCase = true)) {
                val (message, type) = when (status.uppercase()) {
                    "PENDING" -> "Your profile is under review. We'll notify you once you're approved to receive jobs." to WorkNearAlertType.INFO
                    "MORE_INFO" -> "We need more information to verify your profile. Please update your documents." to WorkNearAlertType.WARNING
                    "REJECTED" -> "Your verification was not approved. Please contact support." to WorkNearAlertType.ERROR
                    else -> "Finish setting up your profile to start receiving jobs." to WorkNearAlertType.INFO
                }
                WorkNearAlert(message = message, type = type)
                Spacer(Modifier.height(12.dp))
            }
        } else {
            Text(
                text = "Jobs",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = sansProText
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Accept requests and update job status",
                fontSize = 14.sp,
                color = MediumGray,
                fontFamily = sansProText
            )
            Spacer(Modifier.height(16.dp))

            if (state.verificationStatus != null &&
                !state.verificationStatus.equals("APPROVED", ignoreCase = true)
            ) {
                WorkNearAlert(
                    message = "Complete verification on the Home tab to receive new jobs.",
                    type = WorkNearAlertType.INFO
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            state.jobs.isEmpty() -> EmptyJobs()
            else -> {
                state.errorMessage?.let {
                    WorkNearAlert(message = it, type = WorkNearAlertType.ERROR)
                    Spacer(Modifier.height(10.dp))
                }
                state.successMessage?.let {
                    WorkNearAlert(message = it, type = WorkNearAlertType.SUCCESS)
                    Spacer(Modifier.height(10.dp))
                }
                LazyColumn {
                    item {
                        state.selectedJob?.let { job ->
                            ProfessionalJobActionCard(
                                job = job,
                                otp = state.otp,
                                loading = state.isSubmitting,
                                onOtpChange = viewModel::onOtpChange,
                                acceptJob = viewModel::acceptJob,
                                rejectJob = viewModel::rejectJob,
                                onTheWay = viewModel::markOnTheWay,
                                arrived = viewModel::markArrived,
                                startWork = viewModel::startWork,
                                completeWork = viewModel::markWorkCompleted,
                                verifyOtp = viewModel::verifyOtp
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                    items(state.jobs, key = { it.uuid }) { job ->
                        ProfessionalJobRow(
                            job = job,
                            selected = state.selectedJob?.uuid == job.uuid,
                            onClick = { viewModel.selectJob(job) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyJobs() {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardColor)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No active jobs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
            Spacer(Modifier.height(6.dp))
            Text("Accepted jobs will appear here.", fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
        }
    }
}

@Composable
private fun ProfessionalJobRow(job: Booking, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) PromoBackground else CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(PromoBackground)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(statusIcon(job.status)),
                    contentDescription = null,
                    tint = PrimaryBlue
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(job.serviceName, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                Text("${job.date} • ${job.time}", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            }
            StatusBadge(job.status)
        }
    }
}

@Composable
private fun ProfessionalJobActionCard(
    job: Booking,
    otp: String,
    loading: Boolean,
    onOtpChange: (String) -> Unit,
    acceptJob: () -> Unit,
    rejectJob: () -> Unit,
    onTheWay: () -> Unit,
    arrived: () -> Unit,
    startWork: () -> Unit,
    completeWork: () -> Unit,
    verifyOtp: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PromoBackground)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(statusIcon(job.status)),
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(job.serviceName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                    Text(job.displayStatus.ifBlank { "Update job status" }, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
                }
            }
            Spacer(Modifier.height(12.dp))

            if (job.customerContactVisible) {
                CustomerContactCard(
                    job = job,
                    onCall = {
                        if (job.customerPhone.isNotBlank()) {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.customerPhone}")))
                        }
                    },
                    onOpenMap = {
                        val lat = job.latitude
                        val lng = job.longitude
                        val uri = if (lat != null && lng != null) {
                            Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(job.address)})")
                        } else {
                            Uri.parse("geo:0,0?q=${Uri.encode(job.address)}")
                        }
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                )
                Spacer(Modifier.height(12.dp))
            } else {
                WorkNearAlert(
                    message = "Accept this job to view customer contact details and service location.",
                    type = WorkNearAlertType.INFO
                )
                Spacer(Modifier.height(12.dp))
            }

            LockInfo(job)
            Spacer(Modifier.height(14.dp))

            when (job.status) {
                BookingStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WorkNearButton(
                            text = "Accept",
                            modifier = Modifier.weight(1f),
                            loading = loading,
                            onClick = acceptJob
                        )
                        WorkNearButton(
                            text = "Reject",
                            modifier = Modifier.weight(1f),
                            buttonType = WorkNearButtonType.DANGER,
                            loading = loading,
                            onClick = rejectJob
                        )
                    }
                }
                BookingStatus.CONFIRMED -> WorkNearButton(text = "On the way", loading = loading, onClick = onTheWay)
                BookingStatus.ON_THE_WAY -> WorkNearButton(text = "I have arrived", loading = loading, onClick = arrived)
                BookingStatus.ARRIVED -> WorkNearButton(text = "Start work", loading = loading, onClick = startWork)
                BookingStatus.IN_PROGRESS -> WorkNearButton(text = "Mark work completed", loading = loading, onClick = completeWork)
                BookingStatus.COMPLETED_PENDING_OTP -> {
                    Text("Enter customer OTP", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = onOtpChange,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("6-digit OTP", fontFamily = sansProText) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderGray)
                    )
                    Spacer(Modifier.height(10.dp))
                    WorkNearButton(text = "Verify OTP & complete", loading = loading, onClick = verifyOtp)
                }
                BookingStatus.COMPLETED -> WorkNearAlert(
                    message = "Payment released after OTP verification.",
                    type = WorkNearAlertType.SUCCESS
                )
                else -> WorkNearAlert(
                    message = "Accept the job before status updates are available.",
                    type = WorkNearAlertType.INFO
                )
            }
        }
    }
}

@Composable
private fun CustomerContactCard(
    job: Booking,
    onCall: () -> Unit,
    onOpenMap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SuccessGreenLight.copy(alpha = 0.45f))
            .padding(14.dp)
    ) {
        Text(
            text = "Customer details",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = SuccessGreen,
            fontFamily = sansProText
        )
        Spacer(Modifier.height(10.dp))
        ContactRow(Icons.Default.Person, "Name", job.customerName.ifBlank { "Customer" })
        Spacer(Modifier.height(8.dp))
        ContactRow(
            icon = Icons.Default.Call,
            label = "Phone",
            value = job.customerPhone.ifBlank { "Not available" },
            onClick = if (job.customerPhone.isNotBlank()) onCall else null
        )
        Spacer(Modifier.height(8.dp))
        ContactRow(
            icon = Icons.Default.LocationOn,
            label = "Location",
            value = job.address.ifBlank { "Address not available" },
            onClick = if (job.address.isNotBlank()) onOpenMap else null
        )
    }
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = MediumGray, fontFamily = sansProText)
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (onClick != null) PrimaryBlue else DarkText,
                fontFamily = sansProText
            )
        }
    }
}

@Composable
private fun LockInfo(job: Booking) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PromoBackground)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_wn_lock),
                contentDescription = null,
                tint = PrimaryBlue
            )
            Spacer(Modifier.width(8.dp))
            Text("Locked amount", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
        }
        Text("₹${job.lockedAmount}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue, fontFamily = sansProText)
        Text("Payment releases only after customer OTP verification.", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
    }
}

private fun statusIcon(status: BookingStatus): Int = when (status) {
    BookingStatus.PENDING, BookingStatus.CONFIRMED -> R.drawable.ic_wn_professional
    BookingStatus.ON_THE_WAY -> R.drawable.ic_wn_route
    BookingStatus.ARRIVED -> R.drawable.ic_wn_arrived
    BookingStatus.IN_PROGRESS -> R.drawable.ic_wn_tools
    BookingStatus.COMPLETED_PENDING_OTP -> R.drawable.ic_wn_otp_shield
    BookingStatus.COMPLETED -> R.drawable.ic_wn_receipt
    BookingStatus.REJECTED, BookingStatus.CANCELLED -> R.drawable.ic_wn_receipt
}

@Composable
private fun StatusBadge(status: BookingStatus) {
    val label = when (status) {
        BookingStatus.PENDING -> "Request"
        BookingStatus.CONFIRMED -> "Assigned"
        BookingStatus.ON_THE_WAY -> "On way"
        BookingStatus.ARRIVED -> "Arrived"
        BookingStatus.IN_PROGRESS -> "Working"
        BookingStatus.COMPLETED_PENDING_OTP -> "OTP"
        BookingStatus.COMPLETED -> "Done"
        BookingStatus.REJECTED -> "Rejected"
        BookingStatus.CANCELLED -> "Cancelled"
    }
    val bg = when (status) {
        BookingStatus.COMPLETED -> SuccessGreenLight
        BookingStatus.REJECTED, BookingStatus.CANCELLED -> ErrorRedLight
        else -> WarningAmberLight
    }
    val fg = when (status) {
        BookingStatus.COMPLETED -> SuccessGreen
        BookingStatus.REJECTED, BookingStatus.CANCELLED -> ErrorRed
        else -> PrimaryBlue
    }
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = sansProText
    )
}
