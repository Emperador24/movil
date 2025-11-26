package com.example.liftium.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.R
import com.example.liftium.model.Exercise
import com.example.liftium.model.SplitDay
import com.example.liftium.ui.components.WorkoutStepProgressIndicator
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    selectedSplitDay: SplitDay,
    exercises: List<Exercise> = emptyList(),
    onFinishWorkout: () -> Unit = {},
    onBackToHome: () -> Unit = {},
    workoutViewModel: com.example.liftium.model.WorkoutViewModel? = null,
    modifier: Modifier = Modifier
) {
    // Use provided exercises or empty list
    val workoutExercises = if (exercises.isNotEmpty()) exercises else emptyList()
    
    var currentExerciseIndex by remember { mutableStateOf(0) }
    var restTimeRemaining by remember { mutableStateOf(0) }
    var isResting by remember { mutableStateOf(false) }
    
    // State for set logging - now track all sets
    var setInputs by remember { mutableStateOf(mutableMapOf<Int, Pair<String, String>>()) }
    var savedSets by remember { mutableStateOf(setOf<Int>()) }
    var previousSets by remember { mutableStateOf<List<com.example.liftium.model.WorkoutSet>>(emptyList()) }
    
    // Load previous sets for the current exercise
    LaunchedEffect(currentExerciseIndex, workoutExercises) {
        if (workoutExercises.isNotEmpty()) {
            val currentExercise = workoutExercises.getOrNull(currentExerciseIndex)
            if (currentExercise != null && workoutViewModel != null) {
                previousSets = workoutViewModel.getPreviousSetsForExercise(currentExercise.id)
            }
        }
    }
    
    // Handle empty exercises case
    if (workoutExercises.isEmpty()) {
        // Show empty state
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(Dimens.ScreenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.fitness_center_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
                Text(
                    text = "No Exercises Found",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                Text(
                    text = "This workout day has no exercises assigned.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                Button(onClick = onBackToHome) {
                    Text("Go Back")
                }
            }
        }
        return
    }
    
    val currentExercise = workoutExercises.getOrNull(currentExerciseIndex) ?: workoutExercises.first()
    val totalExercises = workoutExercises.size
    
    // Rest timer effect
    LaunchedEffect(isResting, restTimeRemaining) {
        if (isResting && restTimeRemaining > 0) {
            delay(1000)
            restTimeRemaining -= 1
        } else if (restTimeRemaining == 0) {
            isResting = false
            // Don't reset saved sets - keep them green to show progress
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
            ActiveWorkoutHeader()
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.ScreenPadding)
            ) {
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                // Exercise Info Card
                ExerciseInfoCard(
                    exercise = currentExercise,
                    currentExercise = currentExerciseIndex + 1,
                    totalExercises = totalExercises,
                    restTimeRemaining = if (isResting) restTimeRemaining else null
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                // Set Logging Cards - Show all sets
                AllSetsLoggingCard(
                    totalSets = currentExercise.defaultSets,
                    setInputs = setInputs,
                    savedSets = savedSets,
                    previousSets = previousSets,
                    onSetInputChange = { setNumber, reps, weight ->
                        setInputs = setInputs.toMutableMap().apply {
                            this[setNumber] = Pair(reps, weight)
                        }
                    },
                    onSaveSet = { setNumber ->
                        val input = setInputs[setNumber]
                        if (input != null && input.first.isNotBlank() && input.second.isNotBlank()) {
                            savedSets = savedSets + setNumber
                            
                            // Save to ViewModel/Firebase
                            workoutViewModel?.saveWorkoutSet(
                                exerciseId = currentExercise.id,
                                setNumber = setNumber,
                                reps = input.first.toIntOrNull() ?: 0,
                                weight = input.second.toDoubleOrNull() ?: 0.0
                            )
                            
                            // Start rest timer after saving any set (except last)
                            if (setNumber < currentExercise.defaultSets) {
                                restTimeRemaining = currentExercise.restTimeSec
                                isResting = true
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                // Exercise Navigation
                ExerciseNavigationCard(
                    currentExercise = currentExerciseIndex + 1,
                    totalExercises = totalExercises,
                    completedExercises = savedSets.size,
                    onPreviousExercise = {
                        if (currentExerciseIndex > 0) {
                            currentExerciseIndex -= 1
                            setInputs = mutableMapOf()
                            savedSets = setOf()
                            isResting = false
                            restTimeRemaining = 0
                            // Previous sets will be loaded by LaunchedEffect
                        }
                    },
                    onNextExercise = {
                        if (currentExerciseIndex < totalExercises - 1) {
                            currentExerciseIndex += 1
                            setInputs = mutableMapOf()
                            savedSets = setOf()
                            isResting = false
                            restTimeRemaining = 0
                            // Previous sets will be loaded by LaunchedEffect
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
                
                // Finish Workout Button (always available)
                OutlinedButton(
                    onClick = {
                        // Finish the workout session in Firebase
                        workoutViewModel?.finishWorkoutSession()
                        // Navigate to finish screen
                        onFinishWorkout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ButtonHeight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Finish Workout",
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
private fun ActiveWorkoutHeader(
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
                text = "Step 3 of 4",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
        
        WorkoutStepProgressIndicator(
            currentStep = 3,
            totalSteps = 4,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ExerciseInfoCard(
    exercise: Exercise,
    currentExercise: Int,
    totalExercises: Int,
    restTimeRemaining: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(Dimens.CardPadding),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exercise icon and name
            Icon(
                painter = painterResource(id = R.drawable.fitness_center_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
            
            // Muscle groups
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)
            ) {
                exercise.muscleGroups.split(", ").take(2).forEach { muscleGroup ->
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = muscleGroup,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            // Rest timer or exercise progress
            if (restTimeRemaining != null && restTimeRemaining > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.SpaceMedium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.fitness_center_24px),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                        Text(
                            text = "${restTimeRemaining}s rest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Text(
                    text = "Exercise $currentExercise of $totalExercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AllSetsLoggingCard(
    totalSets: Int,
    setInputs: Map<Int, Pair<String, String>>,
    savedSets: Set<Int>,
    previousSets: List<com.example.liftium.model.WorkoutSet>,
    onSetInputChange: (Int, String, String) -> Unit,
    onSaveSet: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(Dimens.CardPadding),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Your Sets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "$totalSets sets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
            ) {
                Text(
                    text = "Set",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Reps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Weight (kg/lbs)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
            
            // Display all sets
            repeat(totalSets) { index ->
                val setNumber = index + 1
                val input = setInputs[setNumber] ?: Pair("", "")
                val isSaved = savedSets.contains(setNumber)
                val previousSet = previousSets.find { it.setNumber == setNumber }
                
                SingleSetRow(
                    setNumber = setNumber,
                    repsValue = input.first,
                    weightValue = input.second,
                    isSaved = isSaved,
                    previousSet = previousSet,
                    onRepsChange = { reps ->
                        onSetInputChange(setNumber, reps, input.second)
                    },
                    onWeightChange = { weight ->
                        onSetInputChange(setNumber, input.first, weight)
                    },
                    onSaveSet = { onSaveSet(setNumber) }
                )
                
                if (index < totalSets - 1) {
                    Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
                }
            }
        }
    }
}

@Composable
private fun SingleSetRow(
    setNumber: Int,
    repsValue: String,
    weightValue: String,
    isSaved: Boolean,
    previousSet: com.example.liftium.model.WorkoutSet?,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSaveSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set number
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = setNumber.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Reps input with previous data
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = repsValue,
                onValueChange = onRepsChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (previousSet != null) {
                Text(
                    text = "Prev: ${previousSet.reps}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
        
        // Weight input with previous data
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = weightValue,
                onValueChange = onWeightChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (previousSet != null) {
                Text(
                    text = if (previousSet.weight > 0) "Prev: ${previousSet.weight.toInt()}" else "Prev: BW",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
        
        // Save button
        Button(
            onClick = onSaveSet,
            enabled = repsValue.isNotBlank() && weightValue.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSaved) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(48.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            if (isSaved) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Saved",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.fitness_center_24px),
                    contentDescription = "Save set",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun SetLoggingCard(
    currentSet: Int,
    totalSets: Int,
    repsValue: String,
    weightValue: String,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    saveSuccess: Boolean,
    onSaveSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(Dimens.CardPadding),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Your Sets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "$totalSets sets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
            // Input fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Set",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceExtraSmall))
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = currentSet.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reps",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceExtraSmall))
                    OutlinedTextField(
                        value = repsValue,
                        onValueChange = onRepsChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weight (kg/lbs)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceExtraSmall))
                    OutlinedTextField(
                        value = weightValue,
                        onValueChange = onWeightChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            
            // Save button
            Button(
                onClick = onSaveSet,
                enabled = repsValue.isNotBlank() && weightValue.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (saveSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (saveSuccess) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                    Text("Saved!")
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.fitness_center_24px),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                    Text("Save & Complete Set")
                }
            }
        }
    }
}

@Composable
private fun ExerciseNavigationCard(
    currentExercise: Int,
    totalExercises: Int,
    completedExercises: Int,
    onPreviousExercise: () -> Unit,
    onNextExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(Dimens.CardPadding),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Exercise Button
            Button(
                onClick = onPreviousExercise,
                enabled = currentExercise > 1,
                colors = ButtonDefaults.outlinedButtonColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous Exercise",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Next Exercise Button
            Button(
                onClick = onNextExercise,
                enabled = currentExercise < totalExercises,
                colors = ButtonDefaults.buttonColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Exercise",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Preview removed - requires real data
