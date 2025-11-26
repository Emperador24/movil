package com.example.liftium.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.R
import com.example.liftium.model.ProgressPhoto
import com.example.liftium.model.ProgressPhotoWithDetails
import com.example.liftium.ui.components.ProgressPhotoCard
import com.example.liftium.ui.components.LiftiumTopAppBar
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme
import android.net.Uri
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Period

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualProgressScreen(
    photos: List<ProgressPhotoWithDetails> = emptyList(),
    onBackClick: () -> Unit = {},
    onPhotoClick: (ProgressPhoto) -> Unit = {},
    onAddPhotoClick: () -> Unit = {},
    onDeletePhoto: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progressPhotos = photos // Use actual photos, no mock data fallback
    var selectedPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Show photo detail dialog if a photo is selected
    selectedPhoto?.let { photo ->
        ProgressPhotoDetailDialog(
            photo = photo,
            onDismiss = { selectedPhoto = null },
            onDelete = { showDeleteConfirmation = true }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation && selectedPhoto != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDeletePhoto(selectedPhoto!!.id)
                selectedPhoto = null
                showDeleteConfirmation = false
            },
            onDismiss = {
                showDeleteConfirmation = false
            }
        )
    }

    Scaffold(
        topBar = {
            LiftiumTopAppBar(
                title = "Visual Progress",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            GradientBorderFAB(
                onClick = onAddPhotoClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress header with stats (only show if there are photos)
            if (progressPhotos.isNotEmpty()) {
                ProgressHeader(
                    totalPhotos = progressPhotos.size,
                    latestPhoto = progressPhotos.firstOrNull()?.photo,
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            }
            
            // Show empty state card when no photos
            if (progressPhotos.isEmpty()) {
                EmptyPhotosCard(
                    modifier = Modifier.padding(Dimens.ScreenPadding)
                )
            }
            
            // Photo grid (always shown, empty when no photos)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    top = if (progressPhotos.isEmpty()) Dimens.SpaceMedium else 0.dp,
                    bottom = Dimens.SpaceLarge
                ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
                modifier = Modifier.fillMaxSize()
            ) {
                items(progressPhotos) { photoWithDetails ->
                    ProgressPhotoCard(
                        photo = photoWithDetails.photo,
                        formattedDate = photoWithDetails.formattedDate,
                        formattedWeight = photoWithDetails.formattedWeight,
                        onClick = { 
                            selectedPhoto = photoWithDetails.photo
                            onPhotoClick(photoWithDetails.photo)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPhotosCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens.SpaceLarge),
        shape = RoundedCornerShape(Dimens.CardPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.CardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            Text(
                text = "No Progress Photos Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
            
            Text(
                text = "Tap the + button below to take your first progress photo and start tracking your fitness journey!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
            )
        }
    }
}

@Composable
private fun ProgressHeader(
    totalPhotos: Int,
    latestPhoto: ProgressPhoto?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
        
        Text(
            text = "Your Progress Journey",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge)
        ) {
            // Total photos
            Column {
                Text(
                    text = totalPhotos.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Photos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Latest weight
            latestPhoto?.weight?.let { weight ->
                Column {
                    Text(
                        text = "${weight.toInt()} lbs",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Latest Weight",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Time span
            if (totalPhotos > 1) {
                Column {
                    Text(
                        text = "3 months",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Journey",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ProgressPhotoDetailDialog(
    photo: ProgressPhoto,
    onDismiss: () -> Unit,
    onDelete: () -> Unit = {}
) {
    // Format the date for display
    val formattedDate = remember(photo.date) {
        formatPhotoDate(photo.date)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress Photo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete photo",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        text = {
            Column {
                // Photo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Try to load actual photo if path exists
                        if (photo.imagePath.isNotBlank() && photo.imagePath != "photo1" && photo.imagePath != "photo2" && photo.imagePath != "photo3") {
                            AsyncImage(
                                model = Uri.parse(photo.imagePath),
                                contentDescription = "Progress photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                error = painterResource(id = R.drawable.fitness_center_24px),
                                placeholder = painterResource(id = R.drawable.fitness_center_24px)
                            )
                        } else {
                            // Placeholder for mock/empty photos
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.fitness_center_24px),
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                                Text(
                                    text = "Progress Photo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                // Photo details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Weight",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = photo.weight?.let { "${it.toInt()} lbs" } ?: "Not recorded",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                photo.notes?.let { notes ->
                    Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
                    
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Delete Progress Photo?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this progress photo? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Helper function to format photo date for display
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun formatPhotoDate(date: LocalDate): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    
    return when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> {
            val period = Period.between(date, today)
            val daysAgo = period.days + (period.months * 30) + (period.years * 365)
            when {
                daysAgo < 7 -> "$daysAgo day${if (daysAgo != 1) "s" else ""} ago"
                daysAgo < 30 -> {
                    val weeks = daysAgo / 7
                    "$weeks week${if (weeks > 1) "s" else ""} ago"
                }
                daysAgo < 365 -> {
                    val months = period.months + (period.years * 12)
                    "$months month${if (months > 1) "s" else ""} ago"
                }
                else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Visual Progress Screen - Light Mode")
@Preview(name = "Visual Progress Screen - Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VisualProgressScreenPreview() {
    LiftiumTheme {
        VisualProgressScreen()
    }
}

@Composable
private fun GradientBorderFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.primaryContainer
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(52.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add new progress photo",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(name = "Gradient FAB - Light Mode")
@Preview(name = "Gradient FAB - Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradientBorderFABPreview() {
    LiftiumTheme {
        Surface(
            modifier = Modifier.padding(32.dp)
        ) {
            GradientBorderFAB(
                onClick = {}
            )
        }
    }
}
