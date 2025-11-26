package com.example.liftium.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.liftium.model.*
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRoom: ChatRoom,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = viewModel()
) {
    val chatState by chatViewModel.chatState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var messageText by remember { mutableStateOf("") }
    var showMediaOptions by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Lanzador para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatViewModel.uploadImage(chatRoom.id, it)
        }
    }

    // Cargar mensajes cuando se abre la pantalla
    LaunchedEffect(chatRoom.id) {
        chatViewModel.loadMessages(chatRoom.id)
    }

    // Auto-scroll al último mensaje
    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                chatRoom = chatRoom,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ChatBottomBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        chatViewModel.sendTextMessage(chatRoom.id, messageText)
                        messageText = ""
                    }
                },
                onAttachClick = { showMediaOptions = true },
                isLoading = chatState.isLoading
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (chatState.isLoading && chatState.messages.isEmpty()) {
                // Loading inicial
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (chatState.messages.isEmpty()) {
                // Estado vacío
                EmptyChatState(
                    roomName = chatRoom.displayName,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Lista de mensajes
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        horizontal = Dimens.ScreenPadding,
                        vertical = Dimens.SpaceMedium
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = chatState.messages,
                        key = { it.id }
                    ) { message ->
                        ChatMessageBubble(
                            message = message,
                            isCurrentUser = message.userId == currentUserId
                        )
                    }
                }
            }

            // Diálogo de opciones de media
            if (showMediaOptions) {
                MediaOptionsDialog(
                    onDismiss = { showMediaOptions = false },
                    onImageClick = {
                        showMediaOptions = false
                        imagePickerLauncher.launch("image/*")
                    },
                    onAudioClick = {
                        showMediaOptions = false
                        // TODO: Implementar grabación de audio
                    }
                )
            }

            // Indicador de carga de media
            if (chatState.mediaUploadState is MediaUploadState.Uploading) {
                UploadProgressDialog(
                    progress = (chatState.mediaUploadState as MediaUploadState.Uploading).progress
                )
            }

            // Mostrar error si existe
            chatState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(Dimens.SpaceMedium),
                    action = {
                        TextButton(onClick = { chatViewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    chatRoom: ChatRoom,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roomColor = when (chatRoom) {
        ChatRoom.SMARTFIT -> Color(0xFFFF6B35)
        ChatRoom.STARK -> Color(0xFF004E89)
        ChatRoom.BODYTECH -> Color(0xFF7B2CBF)
        ChatRoom.GENERAL -> Color(0xFF2A9D8F)
    }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
            ) {
                Surface(
                    color = roomColor.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = chatRoom.displayName.first().toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = roomColor
                        )
                    }
                }

                Column {
                    Text(
                        text = chatRoom.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = Color.Green,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Text(
                            text = "Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

@Composable
private fun ChatBottomBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceMedium),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)
        ) {
            // Botón de adjuntar
            IconButton(
                onClick = onAttachClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Attach media",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Campo de texto
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                placeholder = { Text("Type a message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                maxLines = 4
            )

            // Botón de enviar
            IconButton(
                onClick = onSendClick,
                enabled = messageText.isNotBlank() && !isLoading,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message",
                    tint = if (messageText.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Nombre del usuario (solo para mensajes de otros)
        if (!isCurrentUser) {
            Text(
                text = message.userName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Dimens.SpaceSmall, bottom = 2.dp)
            )
        }

        // Burbuja del mensaje
        Surface(
            color = if (isCurrentUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.SpaceMedium)
            ) {
                when (message.messageType) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrentUser)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    MessageType.IMAGE -> {
                        message.mediaUrl?.let { url ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Image message",
                                modifier = Modifier
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    MessageType.AUDIO -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play audio",
                                tint = if (isCurrentUser)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Voice message",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCurrentUser)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Timestamp
                Text(
                    text = message.getFormattedTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyChatState(
    roomName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Dimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubble,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        Text(
            text = "Welcome to $roomName!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))

        Text(
            text = "Be the first to send a message in this chat room",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MediaOptionsDialog(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onAudioClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Attach Media",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
            ) {
                MediaOptionItem(
                    icon = Icons.Default.Image,
                    text = "Send Image",
                    onClick = onImageClick
                )

                MediaOptionItem(
                    icon = Icons.Default.Mic,
                    text = "Send Audio",
                    onClick = onAudioClick
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MediaOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun UploadProgressDialog(
    progress: Float,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = "Uploading...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {}
    )
}

@Preview(name = "Chat Screen - Light Mode")
@Preview(name = "Chat Screen - Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatScreenPreview() {
    LiftiumTheme {
        ChatScreen(
            chatRoom = ChatRoom.GENERAL
        )
    }
}