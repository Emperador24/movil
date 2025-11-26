package com.example.liftium.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.R
import com.example.liftium.mock.MockData
import com.example.liftium.model.Split
import com.example.liftium.ui.components.WorkoutStepProgressIndicator
import com.example.liftium.ui.components.SplitSelectionCard
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineSelectorScreen(
    onSplitSelected: (Split) -> Unit = {},
    onGoHome: () -> Unit = {},
    workoutViewModel: com.example.liftium.model.WorkoutViewModel? = null,
    modifier: Modifier = Modifier
) {
    val workoutState = workoutViewModel?.state?.collectAsState()?.value
    
    // Only use user's splits from Firebase - no fallback templates
    val availableSplits = workoutState?.userSplits ?: emptyList()
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with step progress
            RoutineSelectorHeader()
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.ScreenPadding)
            ) {
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                // Title and Description
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.fitness_center_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
                    
                    Text(
                        text = "Select a Training Split",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                    
                    Text(
                        text = "Choose a training split to begin your workout session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                // Loading state
                if (workoutState?.isLoading == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpaceLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                // Error state
                workoutState?.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Dimens.SpaceMedium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(Dimens.CardPadding),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                // Empty state message if no splits
                if (availableSplits.isEmpty() && workoutState?.isLoading != true) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Dimens.SpaceMedium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.CardPadding * 2),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.fitness_center_24px),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
                            Text(
                                text = "No Training Splits Yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                            Text(
                                text = "Create your first training split in Settings → Training Split Settings to get started!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                            Button(
                                onClick = onGoHome,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                                Text("Go to Settings")
                            }
                        }
                    }
                }
                
                // Split Selection Cards
                availableSplits.forEach { split ->
                    SplitSelectionCard(
                        split = split,
                        onSplitClick = { onSplitSelected(split) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Dimens.SpaceMedium)
                    )
                }
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                // Go Home Button
                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ButtonHeight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.IconSizeSmall)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                    Text(
                        text = "Go Home",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(Dimens.SpaceExtraLarge))
            }
        }
    }
}

@Composable
private fun RoutineSelectorHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding)
            .padding(top = Dimens.SpaceLarge, bottom = Dimens.SpaceMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Start Workout",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Step 1 of 6",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
        
        WorkoutStepProgressIndicator(
            currentStep = 1,
            totalSteps = 6,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RoutineSelectorScreenPreview() {
    LiftiumTheme {
        RoutineSelectorScreen()
    }
}
