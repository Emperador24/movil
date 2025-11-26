package com.example.liftium.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.mock.MockData
import com.example.liftium.model.SplitDay
import com.example.liftium.ui.components.WorkoutStepProgressIndicator
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedWorkoutScreen(
    completedSplitDay: SplitDay?,
    completedExercises: Int = 0,
    totalSets: Int = 0,
    onReturnToWorkout: () -> Unit = {},
    onFinishWorkout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Start Workout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            
            // Header with step progress
            CompletedWorkoutHeader()
            
            Spacer(modifier = Modifier.height(Dimens.SpaceExtraLarge))
            
            // Success content
            CompletedWorkoutContent(
                completedExercises = completedExercises,
                totalSets = totalSets
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action buttons
            CompletedWorkoutActions(
                onReturnToWorkout = onReturnToWorkout,
                onFinishWorkout = onFinishWorkout
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
        }
    }
}

@Composable
private fun CompletedWorkoutHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Step 4 of 4",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
        
        WorkoutStepProgressIndicator(
            currentStep = 4,
            totalSteps = 4,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CompletedWorkoutContent(
    completedExercises: Int,
    totalSets: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(Dimens.CardPadding),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Success icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Workout Complete",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            
            // Success title
            Text(
                text = "Workout Complete!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
            
            // Success description
            Text(
                text = "You've successfully logged all exercises for this workout.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            
            // Workout summary
            Text(
                text = "Workout Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge)
            ) {
                // Exercises completed
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(Dimens.SpaceSmall)
                    ) {
                        Text(
                            text = completedExercises.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Text(
                        text = "Exercises",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Sets completed
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(Dimens.SpaceSmall)
                    ) {
                        Text(
                            text = totalSets.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Text(
                        text = "Sets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            
            // Additional message
            Text(
                text = "Your workout has been saved. You can view your workout history anytime.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CompletedWorkoutActions(
    onReturnToWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
    ) {
        // Return to Workout button
        OutlinedButton(
            onClick = onReturnToWorkout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.ButtonRadius),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
            Text(
                text = "Return to Workout",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Finish Workout button
        Button(
            onClick = onFinishWorkout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.ButtonRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
            Text(
                text = "Finish Workout",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompletedWorkoutScreenPreview() {
    LiftiumTheme {
        CompletedWorkoutScreen(
            completedSplitDay = MockData.pushDay,
            completedExercises = 3,
            totalSets = 6
        )
    }
}
