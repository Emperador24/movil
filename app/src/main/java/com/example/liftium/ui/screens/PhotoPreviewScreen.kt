package com.example.liftium.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.liftium.model.ProgressPhotoViewModel
import com.example.liftium.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPreviewScreen(
    photoUri: Uri,
    onSaveClick: (Double?, String?) -> Unit,
    onRetakeClick: () -> Unit,
    onCancelClick: () -> Unit,
    progressPhotoViewModel: ProgressPhotoViewModel? = null,
    modifier: Modifier = Modifier
) {
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var weightError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Progress Details") },
                navigationIcon = {
                    IconButton(onClick = onCancelClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!isSaving) {
                                // Validate weight
                                val parsedWeight = if (weight.isNotBlank()) {
                                    weight.toDoubleOrNull()?.also {
                                        if (it <= 0 || it > 1000) {
                                            weightError = "Weight must be between 0 and 1000 lbs"
                                            return@IconButton
                                        }
                                    } ?: run {
                                        weightError = "Invalid weight value"
                                        return@IconButton
                                    }
                                } else {
                                    null
                                }
                                
                                val trimmedNotes = notes.trim().ifBlank { null }
                                
                                isSaving = true
                                onSaveClick(parsedWeight, trimmedNotes)
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            // Photo preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f),
                shape = RoundedCornerShape(Dimens.CardPadding),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = Dimens.CardElevation
                )
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Progress photo preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            // Retake button
            OutlinedButton(
                onClick = onRetakeClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text("Retake Photo")
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            
            // Details section
            Text(
                text = "Progress Details (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            // Weight input
            OutlinedTextField(
                value = weight,
                onValueChange = {
                    weight = it
                    weightError = null
                },
                label = { Text("Weight (lbs)") },
                placeholder = { Text("e.g., 175") },
                isError = weightError != null,
                supportingText = weightError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            // Notes input
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("How are you feeling? Any milestones?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.SpaceMedium)
                ) {
                    Text(
                        text = "💡 Pro Tip",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                    Text(
                        text = "Take photos at the same time of day, in similar lighting, and from the same angles for the most accurate progress tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceExtraLarge))
            
            // Save button
            Button(
                onClick = {
                    if (!isSaving) {
                        // Validate weight
                        val parsedWeight = if (weight.isNotBlank()) {
                            weight.toDoubleOrNull()?.also {
                                if (it <= 0 || it > 1000) {
                                    weightError = "Weight must be between 0 and 1000 lbs"
                                    return@Button
                                }
                            } ?: run {
                                weightError = "Invalid weight value"
                                return@Button
                            }
                        } else {
                            null
                        }
                        
                        val trimmedNotes = notes.trim().ifBlank { null }
                        
                        isSaving = true
                        onSaveClick(parsedWeight, trimmedNotes)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                    Text("Saving...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.IconSize)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                    Text(
                        text = "Save Progress Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
        }
    }
}

