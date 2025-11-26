package com.example.liftium.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liftium.R
import com.example.liftium.mock.MockData
import com.example.liftium.model.RecentWorkoutData
import com.example.liftium.model.*
import com.example.liftium.ui.components.BottomNavItem
import com.example.liftium.ui.components.LiftiumBottomNavigation
import com.example.liftium.ui.components.LiftiumTopAppBar
import com.example.liftium.ui.components.WorkoutCard
import com.example.liftium.ui.theme.Dimens
import com.example.liftium.ui.theme.LiftiumTheme
import com.example.liftium.ui.components.NavigationCard
import com.example.liftium.ui.components.StepCounterCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit = {},
    onTrackProgress: () -> Unit = {},
    onGetStronger: () -> Unit = {},
    onStartFirstWorkout: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onViewAllWorkouts: () -> Unit = {},
    onViewWorkoutDetails: (String) -> Unit = {},
    dailySteps: Int = 0,
    isSensorAvailable: Boolean = false,
    isStepCounterTestMode: Boolean = false,
    currentRoute: String = "home",
    workoutViewModel: WorkoutViewModel? = null,
    modifier: Modifier = Modifier
) {
    // Use ViewModel or fallback to mock data
    val vmState = workoutViewModel?.state?.collectAsState()?.value
    
    val user = androidx.compose.runtime.remember {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    }
    val userName = user?.displayName ?: "User"
    
    val motivationalMessage = workoutViewModel?.getRandomMotivationalMessage() 
        ?: "Today is a great day for a workout!"
    val hasWorkoutToday = vmState?.hasWorkoutToday ?: false
    val workoutStatus = vmState?.workoutStatus ?: "Loading..."
    val todaysWorkoutName = vmState?.todaysWorkoutName ?: "Loading..."
    val hasRecentWorkouts = (vmState?.recentWorkoutsData?.size ?: 0) > 0
    val recentWorkoutsData = vmState?.recentWorkoutsData ?: emptyList()

    val bottomNavItems = listOf(
        BottomNavItem("home", "Home", iconVector = Icons.Default.Home),
        BottomNavItem("profile", "Profile & Settings", iconVector = Icons.Default.Person)
    )

    Scaffold(
        topBar = {
            LiftiumTopAppBar(
                title = "LIFTIUM",
                actions = {
                    Row(
                        modifier = Modifier.padding(end = Dimens.SpaceSmall),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)
                    ) {
                        // Camera/Progress Photos Icon
                        Surface(
                            onClick = onCameraClick,
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Take progress photos",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Chat Icon
                        Surface(
                            onClick = onChatClick,
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Chat with nearby users",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            LiftiumBottomNavigation(
                items = bottomNavItems,
                currentRoute = currentRoute,
                onItemClick = { route ->
                    when (route) {
                        "profile" -> onNavigateToProfile()
                        "home" -> { /* Already on home */ }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

            // Welcome Section
            WelcomeSection(
                userName = userName,
                motivationalMessage = motivationalMessage
            )

            Spacer(modifier = Modifier.height(Dimens.SectionSpacing))

            // Step Counter Card
            StepCounterCard(
                dailySteps = dailySteps,
                isSensorAvailable = isSensorAvailable,
                isTestMode = isStepCounterTestMode
            )

            Spacer(modifier = Modifier.height(Dimens.SectionSpacing))

            // Ready to Train Card
            ReadyToTrainSection(
                hasWorkout = hasWorkoutToday,
                workoutStatus = workoutStatus,
                workoutName = todaysWorkoutName,
                onStartWorkout = onStartWorkout,
                onGetStronger = onGetStronger,
                onTrackProgress = onTrackProgress
            )

            Spacer(modifier = Modifier.height(Dimens.SectionSpacing))

            // Gym Navigation Section
            NavigationCard(
                onNavigateToGyms = onMapClick
            )

            Spacer(modifier = Modifier.height(Dimens.SectionSpacing))

            // Recent Workouts Section - Only show if user has workouts or is loading
            if (vmState?.isLoading == true) {
                // Show loading state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.CardPadding),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.CardPadding * 2),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (hasRecentWorkouts && recentWorkoutsData.isNotEmpty()) {
                RecentWorkoutsCard(
                    recentWorkouts = recentWorkoutsData,
                    onViewAllClick = onViewAllWorkouts,
                    onViewDetailsClick = onViewWorkoutDetails
                )
            }
            // If no recent workouts and not loading, don't show anything

            Spacer(modifier = Modifier.height(Dimens.SpaceExtraLarge))
        }
    }
}

// Keep all existing composables (WelcomeSection, ReadyToTrainSection, ActionButton, RecentWorkoutsSection)
// ... [Previous composables remain the same] ...

@Composable
private fun WelcomeSection(
    userName: String,
    motivationalMessage: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, $userName",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))

        Text(
            text = "Track your fitness journey",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        Text(
            text = motivationalMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ReadyToTrainSection(
    hasWorkout: Boolean,
    workoutStatus: String,
    workoutName: String,
    onStartWorkout: () -> Unit,
    onGetStronger: () -> Unit,
    onTrackProgress: () -> Unit,
    modifier: Modifier = Modifier
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.IconSizeLarge)
                )
                Text(
                    text = "Ready to Train?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

            // Today's Training Status
            WorkoutCard(
                title = workoutName,
                subtitle = "Today's Training",
                statusText = workoutStatus,
                statusColor = if (hasWorkout) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                iconPainter = painterResource(id = R.drawable.fitness_center_24px),
                onClick = onStartWorkout
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

            // Start Workout Button
            Button(
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSize)
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                Text(
                    text = "Start Workout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    text = "Get Stronger",
                    iconPainter = painterResource(id = R.drawable.fitness_center_24px),
                    onClick = onGetStronger,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(Dimens.SpaceMedium))

                ActionButton(
                    text = "Track Progress",
                    iconPainter = painterResource(id = R.drawable.trending_up_24px),
                    onClick = onTrackProgress,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.IconSize)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecentWorkoutsSection(
    hasRecentWorkouts: Boolean,
    onStartFirstWorkout: () -> Unit,
    modifier: Modifier = Modifier
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recent Workouts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

            if (!hasRecentWorkouts) {
                // Empty State
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

                Text(
                    text = "No workout history yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

                Button(
                    onClick = onStartFirstWorkout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.IconSizeSmall)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSmall))
                    Text(
                        text = "Start Your First Workout",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

// New Recent Workouts Card Implementation
@Composable
fun RecentWorkoutsCard(
    recentWorkouts: List<RecentWorkoutData>,
    onViewAllClick: () -> Unit = {},
    onViewDetailsClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
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
                Text(
                    text = "Recent Workouts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = onViewAllClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

            // Recent Workouts List
            recentWorkouts.forEach { workout ->
                RecentWorkoutItem(
                    workout = workout,
                    onViewDetailsClick = { onViewDetailsClick(workout.id) },
                    modifier = Modifier.padding(bottom = Dimens.SpaceMedium)
                )
            }
        }
    }
}

@Composable
private fun RecentWorkoutItem(
    workout: RecentWorkoutData,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with trophy and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Trophy icon if available
                    workout.trophy?.let { trophy ->
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Trophy",
                            tint = when (trophy.type) {
                                TrophyType.GOLD -> Color(0xFFFFD700)
                                TrophyType.SILVER -> Color(0xFFC0C0C0)
                                TrophyType.BRONZE -> Color(0xFFCD7F32)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${workout.progress}% PROGRESS",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(workout.progress / 100f)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date and leg day info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = workout.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Leg day :(",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            if (workout.tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(workout.tags) { tag ->
                        WorkoutTagChip(tag = tag)
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Exercises",
                    value = workout.exercises.toString(),
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    label = "Total Sets",
                    value = workout.totalSets.toString(),
                    modifier = Modifier.weight(1f)
                )

                StatItem(
                    label = "Main Focus",
                    value = workout.mainFocus,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Overall Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${workout.progress}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { workout.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // View Details Button
            OutlinedButton(
                onClick = onViewDetailsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "View Details",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun WorkoutTagChip(
    tag: WorkoutTag,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (tag.type) {
        TagType.PUSH_UP -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        TagType.PULL_UP -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        TagType.CUSTOM -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
    }

    val textColor = when (tag.type) {
        TagType.PUSH_UP -> MaterialTheme.colorScheme.onPrimaryContainer
        TagType.PULL_UP -> MaterialTheme.colorScheme.onSecondaryContainer
        TagType.CUSTOM -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = tag.text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (label == "Main Focus") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    LiftiumTheme {
        HomeScreen(
            onCameraClick = {},
            onChatClick = {},
            onMapClick = {},
            onViewAllWorkouts = {},
            onViewWorkoutDetails = {},
            dailySteps = 6543,
            isSensorAvailable = true,
            isStepCounterTestMode = true
        )
    }
}