package com.example.liftium.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ChatState(
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null,
    val mediaUploadState: MediaUploadState = MediaUploadState.Idle
)

class ChatViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private var currentRoomId: String? = null

    // Cargar mensajes de una sala
    fun loadMessages(roomId: String) {
        currentRoomId = roomId

        viewModelScope.launch {
            try {
                _chatState.value = _chatState.value.copy(isLoading = true, error = null)

                // Listener en tiempo real para mensajes
                firestore.collection("chatRooms")
                    .document(roomId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _chatState.value = _chatState.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                            return@addSnapshotListener
                        }

                        val messages = snapshot?.documents?.mapNotNull { doc ->
                            try {
                                ChatMessage(
                                    id = doc.id,
                                    roomId = doc.getString("roomId") ?: "",
                                    userId = doc.getString("userId") ?: "",
                                    userName = doc.getString("userName") ?: "Unknown",
                                    message = doc.getString("message") ?: "",
                                    messageType = MessageType.valueOf(
                                        doc.getString("messageType") ?: "TEXT"
                                    ),
                                    mediaUrl = doc.getString("mediaUrl"),
                                    timestamp = doc.getTimestamp("timestamp")
                                        ?: com.google.firebase.Timestamp.now(),
                                    isEdited = doc.getBoolean("isEdited") ?: false
                                )
                            } catch (e: Exception) {
                                null
                            }
                        } ?: emptyList()

                        _chatState.value = _chatState.value.copy(
                            isLoading = false,
                            messages = messages,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _chatState.value = _chatState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading messages"
                )
            }
        }
    }

    // Enviar mensaje de texto
    fun sendTextMessage(roomId: String, message: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                val chatMessage = hashMapOf(
                    "roomId" to roomId,
                    "userId" to currentUser.uid,
                    "userName" to (currentUser.displayName ?: "Anonymous"),
                    "message" to message,
                    "messageType" to MessageType.TEXT.name,
                    "mediaUrl" to null,
                    "timestamp" to com.google.firebase.Timestamp.now(),
                    "isEdited" to false
                )

                firestore.collection("chatRooms")
                    .document(roomId)
                    .collection("messages")
                    .add(chatMessage)
                    .await()

                // Actualizar último mensaje de la sala
                updateLastMessage(roomId, message, MessageType.TEXT)

            } catch (e: Exception) {
                _chatState.value = _chatState.value.copy(
                    error = "Failed to send message: ${e.message}"
                )
            }
        }
    }

    // Subir imagen
    fun uploadImage(roomId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                _chatState.value = _chatState.value.copy(
                    mediaUploadState = MediaUploadState.Uploading(0f)
                )

                // Crear referencia única para la imagen
                val imageId = UUID.randomUUID().toString()
                val imageRef = storage.reference
                    .child("chat_images/$roomId/$imageId.jpg")

                // Subir imagen
                val uploadTask = imageRef.putFile(imageUri)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                    _chatState.value = _chatState.value.copy(
                        mediaUploadState = MediaUploadState.Uploading(progress / 100f)
                    )
                }.await()

                // Obtener URL de descarga
                val downloadUrl = imageRef.downloadUrl.await().toString()

                // Crear mensaje con imagen
                val chatMessage = hashMapOf(
                    "roomId" to roomId,
                    "userId" to currentUser.uid,
                    "userName" to (currentUser.displayName ?: "Anonymous"),
                    "message" to "📷 Image",
                    "messageType" to MessageType.IMAGE.name,
                    "mediaUrl" to downloadUrl,
                    "timestamp" to com.google.firebase.Timestamp.now(),
                    "isEdited" to false
                )

                firestore.collection("chatRooms")
                    .document(roomId)
                    .collection("messages")
                    .add(chatMessage)
                    .await()

                updateLastMessage(roomId, "📷 Image", MessageType.IMAGE)

                _chatState.value = _chatState.value.copy(
                    mediaUploadState = MediaUploadState.Success(downloadUrl)
                )

            } catch (e: Exception) {
                _chatState.value = _chatState.value.copy(
                    mediaUploadState = MediaUploadState.Error(e.message ?: "Upload failed"),
                    error = "Failed to upload image: ${e.message}"
                )
            }
        }
    }

    // Subir audio
    fun uploadAudio(roomId: String, audioUri: Uri) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch

                _chatState.value = _chatState.value.copy(
                    mediaUploadState = MediaUploadState.Uploading(0f)
                )

                // Crear referencia única para el audio
                val audioId = UUID.randomUUID().toString()
                val audioRef = storage.reference
                    .child("chat_audio/$roomId/$audioId.m4a")

                // Subir audio
                val uploadTask = audioRef.putFile(audioUri)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                    _chatState.value = _chatState.value.copy(
                        mediaUploadState = MediaUploadState.Uploading(progress / 100f)
                    )
                }.await()

                // Obtener URL de descarga
                val downloadUrl = audioRef.downloadUrl.await().toString()

                // Crear mensaje con audio
                val chatMessage = hashMapOf(
                    "roomId" to roomId,
                    "userId" to currentUser.uid,
                    "userName" to (currentUser.displayName ?: "Anonymous"),
                    "message" to "🎤 Voice message",
                    "messageType" to MessageType.AUDIO.name,
                    "mediaUrl" to downloadUrl,
                    "timestamp" to com.google.firebase.Timestamp.now(),
                    "isEdited" to false
                )

                firestore.collection("chatRooms")
                    .document(roomId)
                    .collection("messages")
                    .add(chatMessage)
                    .await()

                updateLastMessage(roomId, "🎤 Voice message", MessageType.AUDIO)

                _chatState.value = _chatState.value.copy(
                    mediaUploadState = MediaUploadState.Success(downloadUrl)
                )

            } catch (e: Exception) {
                _chatState.value = _chatState.value.copy(
                    mediaUploadState = MediaUploadState.Error(e.message ?: "Upload failed"),
                    error = "Failed to upload audio: ${e.message}"
                )
            }
        }
    }

    // Actualizar último mensaje de la sala
    private suspend fun updateLastMessage(roomId: String, message: String, type: MessageType) {
        try {
            val currentUser = auth.currentUser ?: return

            val lastMessageData = hashMapOf(
                "message" to message,
                "messageType" to type.name,
                "userName" to (currentUser.displayName ?: "Anonymous"),
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("chatRooms")
                .document(roomId)
                .set(lastMessageData)
                .await()

        } catch (e: Exception) {
            // Log error but don't fail the message send
        }
    }

    // Limpiar errores
    fun clearError() {
        _chatState.value = _chatState.value.copy(error = null)
    }

    // Resetear estado de upload
    fun resetUploadState() {
        _chatState.value = _chatState.value.copy(mediaUploadState = MediaUploadState.Idle)
    }
}