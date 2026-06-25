package com.worknear.app.ui.serviceflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.R
import com.worknear.app.data.model.ServiceCatalog
import com.worknear.app.ui.components.PrimaryPillButton
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.StarYellow
import com.worknear.app.ui.theme.sansProText
import java.util.Calendar

private const val NOTES_LIMIT = 200

private data class DateOption(
    val id: String,
    val day: String,
    val date: String,
    val month: String,
    val full: String,
    val iso: String
)

private fun upcomingDates(count: Int): List<DateOption> {
    val days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val cal = Calendar.getInstance()
    return (0 until count).map {
        val day = days[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val date = cal.get(Calendar.DAY_OF_MONTH).toString()
        val month = months[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        val iso = String.format(
            java.util.Locale.US,
            "%04d-%02d-%02d",
            year,
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val option = DateOption(
            id = "${cal.get(Calendar.MONTH)}-$date",
            day = day,
            date = date,
            month = month,
            full = "$day, $date $month $year",
            iso = iso
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
        option
    }
}

private val compactSlots = listOf(
    "9:00 AM", "10:00 AM", "11:00 AM",
    "12:00 PM", "1:00 PM", "2:00 PM",
    "3:00 PM", "4:00 PM"
)
private val morningSlots = listOf("8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "11:30 AM")
private val afternoonSlots = listOf("12:00 PM", "12:30 PM", "1:00 PM", "2:00 PM", "3:00 PM", "3:30 PM")
private val eveningSlots = listOf("4:00 PM", "5:00 PM", "6:00 PM", "7:00 PM")

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ScheduleBookingScreen(
    onNavigateBack: () -> Unit,
    onContinue: () -> Unit,
    onAddAddress: () -> Unit = {}
) {
    val pro = ServiceFlowState.selectedPro
    val lockAmount = ServiceFlowState.lockAmount
    val dates = remember { upcomingDates(10) }

    var selectedDateId by remember { mutableStateOf(ServiceFlowState.scheduledDate?.let { saved -> dates.firstOrNull { it.full == saved }?.id }) }
    var selectedTime by remember { mutableStateOf(ServiceFlowState.scheduledTime) }
    var notes by remember { mutableStateOf(ServiceFlowState.notes) }
    var timeExpanded by remember { mutableStateOf(false) }
    var showAddressSheet by remember { mutableStateOf(false) }

    val selectedAddress = com.worknear.app.ui.address.AddressStore.selected
    val canContinue = selectedDateId != null && selectedTime != null && selectedAddress != null

    if (showAddressSheet) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showAddressSheet = false },
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CardColor
        ) {
            com.worknear.app.ui.address.SelectAddressSheetContent(
                onSelect = {
                    com.worknear.app.ui.address.AddressStore.select(it)
                    showAddressSheet = false
                },
                onAddNew = {
                    showAddressSheet = false
                    onAddAddress()
                },
                onManage = {
                    showAddressSheet = false
                    onAddAddress()
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        ScheduleTopBar(title = "Schedule Booking", onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(14.dp))
            ProHeaderCard(
                name = pro?.name ?: "Your pro",
                skills = pro?.skills ?: ServiceCatalog.titleFor(ServiceFlowState.categoryId),
                rating = pro?.rating ?: 4.8,
                onTime = pro?.onTimePercent ?: 98
            )

            Spacer(Modifier.height(22.dp))
            SectionTitle("Service Address")
            Spacer(Modifier.height(12.dp))
            AddressSelectCard(
                address = selectedAddress?.display,
                onClick = {
                    if (selectedAddress == null) onAddAddress() else showAddressSheet = true
                }
            )

            Spacer(Modifier.height(22.dp))
            SectionTitle("Select Date")
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dates.forEach { d ->
                    DateChip(
                        date = d,
                        selected = d.id == selectedDateId,
                        onClick = { selectedDateId = d.id }
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            SectionTitle("Select Time")
            Text(
                "All times are in your local time",
                fontSize = 12.sp,
                color = MediumGray,
                fontFamily = sansProText,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.height(12.dp))

            if (!timeExpanded) {
                SlotGrid(
                    slots = compactSlots,
                    selected = selectedTime,
                    onSelect = { selectedTime = it }
                )
                Spacer(Modifier.height(10.dp))
                ExpandToggle(text = "More options", expanded = false) { timeExpanded = true }
            } else {
                SlotGroup("Morning", morningSlots, selectedTime) { selectedTime = it }
                Spacer(Modifier.height(14.dp))
                SlotGroup("Afternoon", afternoonSlots, selectedTime) { selectedTime = it }
                Spacer(Modifier.height(14.dp))
                SlotGroup("Evening", eveningSlots, selectedTime) { selectedTime = it }
                Spacer(Modifier.height(10.dp))
                ExpandToggle(text = "Close", expanded = true) { timeExpanded = false }
            }

            Spacer(Modifier.height(22.dp))
            SectionTitle("Add Notes (Optional)")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { if (it.length <= NOTES_LIMIT) notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = {
                    Text(
                        "Tell us more about your task...",
                        color = MediumGray,
                        fontSize = 14.sp,
                        fontFamily = sansProText
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = DarkText,
                    fontSize = 14.sp,
                    fontFamily = sansProText
                ),
                keyboardOptions = KeyboardOptions.Default,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderGray,
                    cursorColor = PrimaryBlue
                )
            )
            Text(
                "${notes.length}/$NOTES_LIMIT",
                fontSize = 11.sp,
                color = MediumGray,
                fontFamily = sansProText,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        ScheduleBottomBar(
            amount = lockAmount,
            enabled = canContinue,
            buttonText = "Continue"
        ) {
            val chosen = dates.firstOrNull { it.id == selectedDateId }
            ServiceFlowState.scheduledDate = chosen?.full
            ServiceFlowState.scheduledIsoDate = chosen?.iso
            ServiceFlowState.scheduledTime = selectedTime
            ServiceFlowState.notes = notes
            selectedAddress?.let { ServiceFlowState.address = it.display }
            onContinue()
        }
    }
}

@Composable
private fun AddressSelectCard(address: String?, onClick: () -> Unit) {
    val hasAddress = address != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (hasAddress) CardColor else PrimaryBlue.copy(alpha = 0.06f))
            .border(
                1.dp,
                if (hasAddress) BorderGray else PrimaryBlue,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_wn_location), null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (hasAddress) {
                Text("Service at", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
                Spacer(Modifier.height(2.dp))
                Text(
                    address!!,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText,
                    fontFamily = sansProText,
                    maxLines = 2
                )
            } else {
                Text("Add your address", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                Spacer(Modifier.height(2.dp))
                Text("Required to confirm your booking", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            if (hasAddress) "Change" else "Add",
            color = PrimaryBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = sansProText
        )
    }
}

@Composable
internal fun ScheduleTopBar(title: String, onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardColor)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onNavigateBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkText)
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText
        )
        Spacer(Modifier.width(40.dp))
    }
}

@Composable
internal fun ProHeaderCard(name: String, skills: String, rating: Double, onTime: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardColor)
            .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(PrimaryBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(name.first().toString(), color = PrimaryBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = sansProText)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
                Spacer(Modifier.width(4.dp))
                Icon(painterResource(R.drawable.ic_wn_verified), null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
            }
            Text(skills, fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_wn_star), null, tint = StarYellow, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(3.dp))
                Text("$rating", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
                Text("  ·  $onTime% on-time", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
}

@Composable
private fun DateChip(date: DateOption, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) PrimaryBlue else CardColor)
            .border(1.dp, if (selected) PrimaryBlue else BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(date.day, fontSize = 12.sp, color = if (selected) Color.White.copy(alpha = 0.85f) else MediumGray, fontFamily = sansProText)
        Spacer(Modifier.height(4.dp))
        Text(date.date, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else DarkText, fontFamily = sansProText)
        Spacer(Modifier.height(2.dp))
        Text(date.month, fontSize = 11.sp, color = if (selected) Color.White.copy(alpha = 0.85f) else MediumGray, fontFamily = sansProText)
    }
}

@Composable
private fun SlotGroup(label: String, slots: List<String>, selected: String?, onSelect: (String) -> Unit) {
    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MediumGray, fontFamily = sansProText)
    Spacer(Modifier.height(8.dp))
    SlotGrid(slots = slots, selected = selected, onSelect = onSelect)
}

@Composable
private fun SlotGrid(slots: List<String>, selected: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        slots.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { slot ->
                    TimeChip(
                        text = slot,
                        selected = slot == selected,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(slot) }
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TimeChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) PrimaryBlue else CardColor)
            .border(1.dp, if (selected) PrimaryBlue else BorderGray, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else DarkText,
            fontFamily = sansProText
        )
    }
}

@Composable
private fun ExpandToggle(text: String, expanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = sansProText)
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            null,
            tint = PrimaryBlue,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun ScheduleBottomBar(
    amount: Int,
    enabled: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardColor)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Est. Amount", fontSize = 12.sp, color = MediumGray, fontFamily = sansProText)
            Text("₹$amount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText, fontFamily = sansProText)
        }
        PrimaryPillButton(text = buttonText, enabled = enabled, onClick = onClick)
    }
}
