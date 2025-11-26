package com.example.liftium.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.mock.MockData
import com.example.liftium.model.Exercise
import com.example.liftium.model.WorkoutSet
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme

@Composable
fun ExercisePlanCard(
    exerciseNumber: Int,
    exercise: Exercise,
    previousSets: List<WorkoutSet>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(Dimens.CardPadding),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.CardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding)
        ) {
            // Exercise header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
                ) {
                    // Exercise number
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = exerciseNumber.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Muscle groups tags
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)
                        ) {
                            exercise.muscleGroups.split(", ").take(2).forEach { muscleGroup ->
                                MuscleGroupTag(
                                    muscleGroup = muscleGroup,
                                    modifier = Modifier
                                )
                            }
                        }
                    }
                }
                
                Text(
                    text = "${exercise.defaultSets} sets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            
             // Sets display - Individual cards for each set with data
             repeat(exercise.defaultSets) { setIndex ->
                 val setNumber = setIndex + 1
                 val previousSet = previousSets.find { it.setNumber == setNumber }
                 
                 SetCard(
                     setNumber = setNumber,
                     previousSet = previousSet,
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(horizontal = Dimens.SpaceSmall)
                         .padding(bottom = Dimens.SpaceSmall)
                 )
             }
             
             // Rest time indicator - once per exercise
             RestTimeCard(
                 restTime = exercise.restTimeSec,
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(horizontal = Dimens.SpaceSmall)
             )
        }
    }
}

@Composable
private fun SetCard(
    setNumber: Int,
    previousSet: WorkoutSet?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Set number and data
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium)
            ) {
                Text(
                    text = "Set $setNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (previousSet != null) {
                    Text(
                        text = "${previousSet.reps} reps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${previousSet.weight.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "kg/lbs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Right side: Previous indicator
            if (previousSet != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceExtraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Previous performance",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Previous",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RestTimeCard(
    restTime: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            modifier = Modifier.padding(horizontal = Dimens.SpaceMedium, vertical = Dimens.SpaceSmall)
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.example.liftium.R.drawable.fitness_center_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "${restTime}s rest between sets",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MuscleGroupTag(
    muscleGroup: String,
    modifier: Modifier = Modifier
) {
    val tagColor = when (muscleGroup.lowercase()) {
        "biceps", "back" -> Color(0xFF2196F3) // Blue
        "chest", "triceps" -> Color(0xFFFF5722) // Orange
        "legs", "quadriceps", "hamstrings", "glutes" -> Color(0xFF4CAF50) // Green
        "shoulders" -> Color(0xFF9C27B0) // Purple
        else -> MaterialTheme.colorScheme.primary
    }
    
    Surface(
        color = tagColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = muscleGroup,
            style = MaterialTheme.typography.labelSmall,
            color = tagColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExercisePlanCardPreview() {
    LiftiumTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExercisePlanCard(
                exerciseNumber = 1,
                exercise = MockData.benchPress,
                previousSets = listOf(
                    MockData.pullSessionSets[0], // Mock previous set data
                    MockData.pullSessionSets[1]
                )
            )
            
            ExercisePlanCard(
                exerciseNumber = 2,
                exercise = MockData.pullUps,
                previousSets = emptyList()
            )
        }
    }
}
