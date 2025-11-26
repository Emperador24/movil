package com.example.liftium.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.example.liftium.R
import com.example.liftium.model.DayOfWeek
import com.example.liftium.ui.components.WorkoutStepProgressIndicator
import com.example.liftium.ui.components.WorkoutDaySelector
import com.example.liftium.ui.components.ExercisePlanCard
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    onStartWorkout: () -> Unit = {},
    onBackToHome: () -> Unit = {},
    workoutViewModel: com.example.liftium.model.WorkoutViewModel? = null,
    modifier: Modifier = Modifier
) {
    val workoutState = workoutViewModel?.state?.collectAsState()?.value
    
    // Get current split and split days
    val currentSplit = workoutState?.currentSplit
    val splitDays = currentSplit?.splitDays ?: emptyList()
    
    // State for selected workout day - default to today's split day
    val todaysSplitDay = workoutViewModel?.getTodaysSplitDay()
    var selectedSplitDay by remember { mutableStateOf(todaysSplitDay) }
    
    val selectedSplitDayWithExercises = splitDays.find { it.splitDay.id == selectedSplitDay?.id }
    val selectedExercises = selectedSplitDayWithExercises?.exercises ?: emptyList()
    
    // Previous performance data - load from Firebase
    var previousSets by remember { mutableStateOf<Map<String, List<com.example.liftium.model.WorkoutSet>>>(emptyMap()) }
    var isLoadingPreviousSets by remember { mutableStateOf(false) }
    var isStartingWorkout by remember { mutableStateOf(false) }
    
    // Load previous sets for all exercises when exercises change
    LaunchedEffect(selectedExercises) {
        if (selectedExercises.isNotEmpty() && workoutViewModel != null) {
            isLoadingPreviousSets = true
            val setsMap = mutableMapOf<String, List<com.example.liftium.model.WorkoutSet>>()
            selectedExercises.forEach { exercise ->
                val sets = workoutViewModel.getPreviousSetsForExercise(exercise.id)
                if (sets.isNotEmpty()) {
                    setsMap[exercise.id] = sets
                }
            }
            previousSets = setsMap
            isLoadingPreviousSets = false
        }
    }
    
    // Watch for when session is ready and navigate to workout
    LaunchedEffect(workoutState?.currentSession, workoutState?.currentExercises) {
        if (isStartingWorkout && 
            workoutState?.currentSession != null && 
            workoutState.currentExercises.isNotEmpty()) {
            isStartingWorkout = false
            onStartWorkout()
        }
    }
    
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
            WorkoutSummaryHeader()
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.ScreenPadding)
            ) {
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                // Workout Summary Title and Description
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
                        text = "Workout Summary",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                    
                    Text(
                        text = "Review your workout before starting",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                 // Workout Day Selector - only show if we have split days
                 if (splitDays.isNotEmpty()) {
                     WorkoutDaySelector(
                         selectedDay = selectedSplitDay?.name ?: "No Workout",
                         dayOfWeek = selectedSplitDay?.let { DayOfWeek.fromInt(it.dayOfWeek).displayName } ?: "Select Day",
                         availableDays = splitDays.map { it.splitDay },
                         onDaySelected = { splitDay ->
                             selectedSplitDay = splitDay
                         }
                     )
                 } else {
                     // No split available message
                     Card(
                         modifier = Modifier.fillMaxWidth(),
                         colors = CardDefaults.cardColors(
                             containerColor = MaterialTheme.colorScheme.surfaceVariant
                         ),
                         shape = RoundedCornerShape(Dimens.CardPadding)
                     ) {
                         Column(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(Dimens.CardPadding),
                             horizontalAlignment = Alignment.CenterHorizontally
                         ) {
                             Text(
                                 text = "No Training Split",
                                 style = MaterialTheme.typography.titleMedium,
                                 fontWeight = FontWeight.Bold
                             )
                             Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                             Text(
                                 text = "Create a training split in Settings first",
                                 style = MaterialTheme.typography.bodyMedium,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                                 textAlign = TextAlign.Center
                             )
                         }
                     }
                 }
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                 // Exercise Plan Section
                 if (selectedExercises.isNotEmpty() && selectedSplitDay?.isRestDay == false) {
                    Text(
                        text = "Exercise Plan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = Dimens.SpaceMedium)
                    )
                    
                    // Show loading indicator while loading previous sets
                    if (isLoadingPreviousSets) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Dimens.SpaceMedium),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.SpaceLarge),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(Dimens.SpaceMedium))
                                Text(
                                    text = "Loading previous workout data...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                     selectedExercises.forEachIndexed { index, exercise ->
                        ExercisePlanCard(
                            exerciseNumber = index + 1,
                            exercise = exercise,
                            previousSets = previousSets[exercise.id] ?: emptyList(),
                            modifier = Modifier.padding(bottom = Dimens.SpaceMedium)
                        )
                    }
                } else {
                    // Rest day or no exercises
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.SpaceLarge),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(Dimens.CardPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpaceLarge),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Rest Day",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                            Text(
                                text = "Take a break and recover for tomorrow's workout!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                 // Action Buttons
                 if (selectedExercises.isNotEmpty() && selectedSplitDay?.isRestDay == false) {
                    Button(
                        onClick = {
                            // Start creating the workout session
                            selectedSplitDay?.let { splitDay ->
                                isStartingWorkout = true
                                workoutViewModel?.startWorkoutSession(splitDay.id)
                            }
                        },
                        enabled = !isStartingWorkout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isStartingWorkout) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                            Text(
                                text = "Starting...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.fitness_center_24px),
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.IconSizeSmall)
                            )
                            Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                            Text(
                                text = "Start Workout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
                }
                
                OutlinedButton(
                    onClick = onBackToHome,
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
                        text = "Back to Home",
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
private fun WorkoutSummaryHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
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
                 text = "Step 2 of 6",
                 style = MaterialTheme.typography.bodyMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant
             )
        }
        
        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
        
         WorkoutStepProgressIndicator(
             currentStep = 2,
             totalSteps = 6,
             modifier = Modifier.fillMaxWidth()
         )
    }
}

// Preview removed - requires real data
