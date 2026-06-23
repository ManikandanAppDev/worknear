package com.worknear.app.ui.chat

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worknear.app.R
import com.worknear.app.data.model.ChatMessage
import com.worknear.app.data.model.ChatMessages
import com.worknear.app.data.model.Professionals
import com.worknear.app.ui.components.WorkNearTopBar
import com.worknear.app.ui.theme.Background
import com.worknear.app.ui.theme.BorderGray
import com.worknear.app.ui.theme.CardColor
import com.worknear.app.ui.theme.DarkText
import com.worknear.app.ui.theme.LightGray
import com.worknear.app.ui.theme.MediumGray
import com.worknear.app.ui.theme.PrimaryBlue
import com.worknear.app.ui.theme.sansProText

@Composable
fun ChatListScreen(
    modifier: Modifier = Modifier,
    onChatClick: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Text(
            stringResource(R.string.nav_chat),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            fontSize = 22.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = DarkText,
            fontFamily = sansProText
        )

        Professionals.all.take(3).forEach { professional ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onChatClick(professional.id) })
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(professional.imageRes),
                    contentDescription = professional.name,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(professional.name, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = DarkText, fontFamily = sansProText)
                    Text(professional.profession, fontSize = 13.sp, color = MediumGray, fontFamily = sansProText)
                }
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    professionalId: String,
    onNavigateBack: () -> Unit
) {
    val professional = Professionals.byId(professionalId) ?: Professionals.all.first()
    val messages = ChatMessages.forProfessional(professionalId)
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            WorkNearTopBar(
                title = professional.name,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Phone, "Call", tint = DarkText)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = DarkText)
                    }
                }
            )
        },
        containerColor = Background,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.AttachFile, "Attach", tint = MediumGray)
                }
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.type_message), color = LightGray, fontFamily = sansProText) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BorderGray,
                        unfocusedBorderColor = BorderGray,
                        focusedContainerColor = Background,
                        unfocusedContainerColor = Background
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = sansProText)
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.CameraAlt, "Camera", tint = MediumGray)
                }
                IconButton(
                    onClick = { messageText = "" },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isFromUser) PrimaryBlue else CardColor
    val textColor = if (message.isFromUser) Color.White else DarkText

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                    )
                )
                .background(bgColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(message.text, color = textColor, fontSize = 14.sp, fontFamily = sansProText)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            message.timestamp,
            fontSize = 11.sp,
            color = MediumGray,
            fontFamily = sansProText,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
