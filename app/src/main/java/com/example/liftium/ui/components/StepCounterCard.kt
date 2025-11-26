package com.example.liftium.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme

@Composable
fun StepCounterCard(
    dailySteps: Int,
    isSensorAvailable: Boolean,
    modifier: Modifier = Modifier,
    goalSteps: Int = 10000,
    isTestMode: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.CardPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.CardElevation
        )
    ) {
        if (isSensorAvailable || isTestMode) {
            StepCounterContent(
                dailySteps = dailySteps,
                goalSteps = goalSteps,
                isTestMode = isTestMode
            )
        } else {
            StepCounterUnavailable()
        }
    }
}

@Composable
private fun StepCounterContent(
    dailySteps: Int,
    goalSteps: Int,
    isTestMode: Boolean = false
) {
    val progress = (dailySteps.toFloat() / goalSteps).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.CardPadding)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Daily Steps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Test mode indicator
                if (isTestMode) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "TEST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Progress percentage
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        // Step count display
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = dailySteps.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "/ $goalSteps",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))

        // Progress bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        // Motivational message
        Text(
            text = getMotivationalMessage(dailySteps, goalSteps),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StepCounterUnavailable() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.CardPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))

        Text(
            text = "Step Counter Unavailable",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceExtraSmall))

        Text(
            text = "Your device doesn't have a step counter sensor. Try using a physical device with this feature.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Returns a motivational message based on step progress
 */
private fun getMotivationalMessage(steps: Int, goal: Int): String {
    val percentage = (steps.toFloat() / goal * 100).toInt()

    return when {
        steps >= goal -> "Goal achieved! You're crushing it today!"
        percentage >= 75 -> "Almost there! Just ${goal - steps} steps to go!"
        percentage >= 50 -> "Halfway to your goal! Keep moving!"
        percentage >= 25 -> "Good start! Every step counts!"
        steps > 0 -> "Let's get moving! Start your fitness journey!"
        else -> "Take your first step toward your goal!"
    }
}

@Preview(name = "Light Mode - With Steps")
@Preview(name = "Dark Mode - With Steps", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StepCounterCardPreview() {
    LiftiumTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StepCounterCard(
                    dailySteps = 6543,
                    isSensorAvailable = true
                )
            }
        }
    }
}

@Preview(name = "Light Mode - Goal Reached")
@Preview(name = "Dark Mode - Goal Reached", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StepCounterCardGoalReachedPreview() {
    LiftiumTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StepCounterCard(
                    dailySteps = 12500,
                    isSensorAvailable = true
                )
            }
        }
    }
}

@Preview(name = "Light Mode - Unavailable")
@Preview(name = "Dark Mode - Unavailable", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StepCounterCardUnavailablePreview() {
    LiftiumTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StepCounterCard(
                    dailySteps = 0,
                    isSensorAvailable = false
                )
            }
        }
    }
}

