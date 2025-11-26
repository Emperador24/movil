package com.example.liftium.model

import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId


// Tipos de mensajes
enum class MessageType {
    TEXT,
    IMAGE,
    AUDIO
}

// Salas de chat predefinidas
enum class ChatRoom(val displayName: String, val id: String) {
    SMARTFIT("SmartFit", "smartfit"),
    STARK("Stark", "stark"),
    BODYTECH("BodyTech", "bodytech"),
    GENERAL("General", "general")
}

// Mensaje de chat
data class ChatMessage(
    val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val userName: String = "",
    val message: String = "",
    val messageType: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val isEdited: Boolean = false
) {
    // Convertir Timestamp a LocalDateTime para UI
    fun getLocalDateTime(): LocalDateTime {
        return timestamp.toDate()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    // Formato de hora para mostrar
    fun getFormattedTime(): String {
        val dateTime = getLocalDateTime()
        val hour = dateTime.hour
        val minute = dateTime.minute.toString().padStart(2, '0')
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return "$displayHour:$minute $amPm"
    }

    // Formato de fecha para mostrar
    fun getFormattedDate(): String {
        val dateTime = getLocalDateTime()
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val messageDate = dateTime.toLocalDate()

        return when {
            messageDate == today -> "Today"
            messageDate == today.minusDays(1) -> "Yesterday"
            else -> {
                val month = dateTime.monthValue
                val day = dateTime.dayOfMonth
                val year = dateTime.year
                "$month/$day/$year"
            }
        }
    }
}

// Información de la sala de chat con último mensaje
data class ChatRoomInfo(
    val room: ChatRoom,
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0,
    val memberCount: Int = 0
)

// Estado de carga de media
sealed class MediaUploadState {
    object Idle : MediaUploadState()
    data class Uploading(val progress: Float) : MediaUploadState()
    data class Success(val url: String) : MediaUploadState()
    data class Error(val message: String) : MediaUploadState()
}