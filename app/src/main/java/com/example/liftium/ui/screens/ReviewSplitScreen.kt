package com.example.liftium.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liftium.ui.components.CreationStepper
import com.example.liftium.ui.theme.LiftiumTheme
import androidx.compose.foundation.BorderStroke
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSplitScreen(
    splitName: String = "My Training Split",
    days: List<DayState> = emptyList(),
    onBackClick: () -> Unit = {}, // Bottom back button (previous step)
    onBackToSettings: () -> Unit = {}, // Top navigation button (to settings)
    onFinishClick: () -> Unit = {},
    workoutViewModel: com.example.liftium.model.WorkoutViewModel? = null,
    onSaveSplit: (String, Map<Int, String>) -> Unit = { _, _ -> }
) {
    val trainingDaysCount = days.count { it.isTrainingDay }
    var isSaving by androidx.compose.runtime.remember { mutableStateOf(false) }
    
    // Show loading dialog while saving
    if (isSaving) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Creating Your Split") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Setting up your workout routine...")
                }
            },
            confirmButton = { }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = onBackToSettings) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Back to Training Split Settings",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            ReviewBottomBar(
                onBackClick = onBackClick,
                onSaveClick = {
                    // Convert days to dayAssignments map (dayOfWeek -> routineName)
                    val dayAssignments = mutableMapOf<Int, String>()
                    days.forEachIndexed { index, dayState ->
                        if (dayState.isTrainingDay) {
                            dayAssignments[index] = dayState.workoutName
                        } else {
                            dayAssignments[index] = "Rest"
                        }
                    }
                    
                    // Call the save function
                    isSaving = true
                    onSaveSplit(splitName, dayAssignments)
                },
                isEnabled = !isSaving
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            CreationStepper(currentStep = 3)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Review Your Training Split",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please review your training split configuration before saving",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            SplitNameHeader(name = splitName, trainingDays = trainingDaysCount)
            Spacer(modifier = Modifier.height(24.dp))


            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                val chunkedDays = days.chunked(2)
                for (row in chunkedDays) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (day in row) {
                            Box(modifier = Modifier.weight(1f)) {
                                DayReviewCard(day = day)
                            }
                        }

                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SplitNameHeader(name: String, trainingDays: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Badge(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "$trainingDays Training Days",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DayReviewCard(day: DayState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (day.isTrainingDay) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = day.dayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!day.isTrainingDay) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            "Rest Day",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (day.isTrainingDay) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Workout",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = day.workoutName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                }
            }
        }
    }
}

@Composable
fun ReviewBottomBar(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    isEnabled: Boolean = true
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = onSaveClick,
                enabled = isEnabled,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            ) {
                Text("Save Split", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReviewSplitScreenPreview() {
    val sampleDays = listOf(
        DayState("Sunday", isTrainingDay = false),
        DayState("Monday", isTrainingDay = false),
        DayState("Tuesday", isTrainingDay = true, workoutName = "Push Day", exerciseCount = 5),
        DayState("Wednesday", isTrainingDay = false),
        DayState("Thursday", isTrainingDay = true, workoutName = "Pull Day", exerciseCount = 6),
        DayState("Friday", isTrainingDay = false),
        DayState("Saturday", isTrainingDay = true, workoutName = "Leg Day", exerciseCount = 4)
    )
    LiftiumTheme {
        ReviewSplitScreen(
            splitName = "My PPL Split",
            days = sampleDays
        )
    }
}