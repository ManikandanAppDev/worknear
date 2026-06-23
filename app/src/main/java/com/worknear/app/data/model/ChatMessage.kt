package com.worknear.app.data.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String
)

object ChatMessages {
    fun forProfessional(professionalId: String): List<ChatMessage> = listOf(
        ChatMessage("1", "Hi! I've accepted your booking request.", false, "10:30 AM"),
        ChatMessage("2", "Great! What time will you arrive?", true, "10:32 AM"),
        ChatMessage("3", "I'll be there around 10:00 AM tomorrow.", false, "10:33 AM"),
        ChatMessage("4", "Perfect, see you then!", true, "10:34 AM"),
        ChatMessage("5", "Please make sure the power is switched off before I arrive.", false, "10:35 AM")
    )
}
