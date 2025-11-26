package com.example.liftium.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.liftium.R
import com.example.liftium.model.ProgressPhoto
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressPhotoCard(
    photo: ProgressPhoto,
    formattedDate: String,
    formattedWeight: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f), // Portrait aspect ratio
        shape = RoundedCornerShape(Dimens.CardPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.CardElevation
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Photo placeholder - takes most of the card space
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(
                    topStart = Dimens.CardPadding,
                    topEnd = Dimens.CardPadding,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
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
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.fitness_center_24px),
                            placeholder = painterResource(id = R.drawable.fitness_center_24px)
                        )
                    } else {
                        // Placeholder for mock/empty photos
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.fitness_center_24px),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Progress\nPhoto",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 0.9
                            )
                        }
                    }
                }
            }
            
            // Photo info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceSmall)
            ) {
                // Date
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Weight
                Text(
                    text = formattedWeight,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Notes preview (if available)
                photo.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 0.9
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Progress Photo Card - Light Mode")
@Preview(name = "Progress Photo Card - Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProgressPhotoCardPreview() {
    LiftiumTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card with weight
                ProgressPhotoCard(
                    photo = ProgressPhoto(
                        id = "1",
                        userId = "user1",
                        imagePath = "photo1",
                        weight = 175.5,
                        notes = "Feeling great after 3 months of consistent training!",
                        date = LocalDate.now(),
                        createdAt = LocalDateTime.now()
                    ),
                    formattedDate = "Today",
                    formattedWeight = "175 lbs",
                    onClick = {},
                    modifier = Modifier.width(150.dp)
                )
                
                // Card without weight
                ProgressPhotoCard(
                    photo = ProgressPhoto(
                        id = "2",
                        userId = "user1",
                        imagePath = "photo2",
                        weight = null,
                        notes = "Post-workout pump check",
                        date = LocalDate.now().minusDays(2),
                        createdAt = LocalDateTime.now().minusDays(2)
                    ),
                    formattedDate = "2 days ago",
                    formattedWeight = "No weight",
                    onClick = {},
                    modifier = Modifier.width(150.dp)
                )
            }
        }
    }
}
