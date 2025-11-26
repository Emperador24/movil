package com.example.liftium.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.example.liftium.R
import com.example.liftium.model.*
import com.example.liftium.util.BiometricAuthenticator
import com.example.liftium.ui.screens.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Auth Navigation keys
@Serializable
data object LoginRoute : NavKey

@Serializable
data object RegisterRoute : NavKey

@Serializable
data object ForgotPasswordRoute : NavKey

// App Navigation keys
@Serializable
data object HomeRoute : NavKey

@Serializable
data object ProfileRoute : NavKey

@Serializable
data object WorkoutRoute : NavKey

@Serializable
data object ProgressRoute : NavKey

@Serializable
data object WorkoutSummaryRoute : NavKey

@Serializable
data object RoutineSelectorRoute : NavKey

@Serializable
data object ActiveWorkoutRoute : NavKey

@Serializable
data object CompletedWorkoutRoute : NavKey

@Serializable
data object TrainingSplitSettingsRoute : NavKey

@Serializable
data object SelectRoutinesRoute : NavKey

// este es dataclass para poder pasar por parametros la selecci[on de rutinas
@Serializable
data class CreateSplitDayRoute(val selectedRoutines: List<String>) : NavKey

@Serializable
data class ReviewSplitRoute (
    val splitName: String,
    val days: List<DayState>
) : NavKey

@Serializable
data object VisualProgressRoute : NavKey

@Serializable
data object GymFinderRoute : NavKey

@Serializable
data object ChatRoomsRoute : NavKey

@Serializable
data class ChatRoomRoute(val roomId: String) : NavKey
data object CameraRoute : NavKey

@Serializable
data class PhotoPreviewRoute(val photoUriString: String) : NavKey

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationHost(modifier: Modifier = Modifier) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    val progressPhotoViewModel: ProgressPhotoViewModel = viewModel()
    val photoState by progressPhotoViewModel.state.collectAsState()

    val stepCounterViewModel: StepCounterViewModel = viewModel()
    val stepCounterState by stepCounterViewModel.state.collectAsState()
    
    val workoutViewModel: WorkoutViewModel = viewModel()
    val workoutState by workoutViewModel.state.collectAsState()
    
    val activity = LocalContext.current as FragmentActivity
    //val biometricAuthenticator = remember { BiometricAuthenticator(activity) }

    //var visualProgressAuthenticated by rememberSaveable { mutableStateOf(false) }

    // Determinar la ruta inicial basada en el estado de autenticación
    val startDestination = if (authState.isAuthenticated) HomeRoute else LoginRoute
    val backStack = rememberNavBackStack(startDestination)
    val navigateToVisualProgress: () -> Unit = { backStack.add(VisualProgressRoute) }
    // Observar cambios en el estado de autenticación
    LaunchedEffect(authState.isAuthenticated) {
        val currentKey = backStack.lastOrNull()

        if (authState.isAuthenticated && currentKey !is HomeRoute) {
            // Usuario se autenticó, ir a Home
            backStack.clear()
            backStack.add(HomeRoute)
        } else if (!authState.isAuthenticated && currentKey !is LoginRoute && currentKey !is RegisterRoute && currentKey !is ForgotPasswordRoute) {
            // Usuario cerró sesión, ir a Login
            backStack.clear()
            backStack.add(LoginRoute)
        }
    }

    // Observar registro exitoso para volver a Login
    LaunchedEffect(authState.registrationSuccess) {
        if (authState.registrationSuccess) {
            // Volver a Login después de registro exitoso
            backStack.clear()
            backStack.add(LoginRoute)
            // No limpiar el mensaje de éxito aquí, se mostrará en LoginScreen
        }
    }

    // Helper function to handle protected navigation to VisualProgress
   /* val navigateToVisualProgress: () -> Unit = {
        if (visualProgressAuthenticated) {
            // Already authenticated this session, just navigate
            backStack.add(VisualProgressRoute)
        } else {
            // Not authenticated, show biometric prompt
            biometricAuthenticator.authenticate { success ->
                if (success) {
                    visualProgressAuthenticated = true
                    backStack.add(VisualProgressRoute)
                } else {
                    // Auth failed, do not navigate.
                    // A toast is shown by the BiometricAuthenticator.
                }
            }
        }
    }*/

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        predictivePopTransitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        popTransitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        entryProvider = { key ->
            when (key) {
                // Auth Screens
                is LoginRoute -> NavEntry(key) {
                    LoginScreen(
                        onLoginClick = { email, password ->
                            authViewModel.signInWithEmailAndPassword(email, password)
                        },
                        onNavigateToRegister = {
                            authViewModel.clearSuccess() // Limpiar mensaje al navegar a registro
                            backStack.add(RegisterRoute)
                        },
                        onNavigateToForgotPassword = {
                            authViewModel.clearSuccess() // Limpiar mensaje al navegar a forgot password
                            backStack.add(ForgotPasswordRoute)
                        },
                        isLoading = authState.isLoading,
                        errorMessage = authState.errorMessage,
                        successMessage = authState.successMessage
                    )
                }

                is RegisterRoute -> NavEntry(key) {
                    RegisterScreen(
                        onRegisterClick = { userName, email, password ->
                            authViewModel.createUserWithEmailAndPassword(userName, email, password)
                        },
                        onNavigateBack = {
                            authViewModel.clearError()
                            authViewModel.clearSuccess()
                            backStack.removeLastOrNull()
                        },
                        isLoading = authState.isLoading,
                        errorMessage = authState.errorMessage
                    )
                }

                is ForgotPasswordRoute -> NavEntry(key) {
                    ForgotPasswordScreen(
                        onResetPasswordClick = { email ->
                            authViewModel.sendPasswordResetEmail(email)
                        },
                        onNavigateBack = {
                            authViewModel.clearError()
                            authViewModel.clearSuccess()
                            backStack.removeLastOrNull()
                        },
                        isLoading = authState.isLoading,
                        errorMessage = authState.errorMessage,
                        successMessage = authState.successMessage
                    )
                }

                // App Screens
                is HomeRoute -> NavEntry(key) {
                    HomeScreen(
                        onStartWorkout = {
                            // Check if user has splits before navigating
                            if (workoutState.userSplits.isNotEmpty()) {
                                backStack.add(RoutineSelectorRoute)
                            } else {
                                // No splits - take user to create one
                                backStack.add(TrainingSplitSettingsRoute)
                            }
                        },
                        onTrackProgress = {
                            backStack.add(ProgressRoute)
                        },
                        onGetStronger = {
                            // Check if user has splits before navigating
                            if (workoutState.userSplits.isNotEmpty()) {
                                backStack.add(RoutineSelectorRoute)
                            } else {
                                // No splits - take user to create one
                                backStack.add(TrainingSplitSettingsRoute)
                            }
                        },
                        onStartFirstWorkout = {
                            // For first workout, always go to create a split
                            backStack.add(TrainingSplitSettingsRoute)
                        },
                        onNavigateToProfile = {
                            backStack.add(ProfileRoute)
                        },
                        onCameraClick = {
                            navigateToVisualProgress() // Use protected navigation
                        },
                        onChatClick = {
                            backStack.add(ChatRoomsRoute)
                        },
                        onMapClick = {
                            backStack.add(GymFinderRoute) // << conectar al mapa
                        },
                        dailySteps = stepCounterState.dailySteps,
                        isSensorAvailable = stepCounterState.isSensorAvailable,
                        isStepCounterTestMode = !stepCounterState.isSensorAvailable && stepCounterState.isListening,
                        currentRoute = "home",
                        workoutViewModel = workoutViewModel
                    )
                }

                is ProfileRoute -> NavEntry(key) {
                    SettingsScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
                        onEditTrainingSplit = {
                            backStack.add(TrainingSplitSettingsRoute)
                        },
                        onLogOut = {
                            authViewModel.signOut()
                        },
                        onNavigateToHome = {
                            backStack.removeLastOrNull()
                        },
                        currentRoute = "profile"
                    )
                }

                is WorkoutRoute -> NavEntry(key) {
                    WorkoutSummaryScreen(
                        onStartWorkout = {
                            // TODO: Navigate to actual workout screen when implemented
                        },
                        onBackToHome = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                is ProgressRoute -> NavEntry(key) {
                    HomeScreen(
                        onStartWorkout = { backStack.add(RoutineSelectorRoute) },
                        onTrackProgress = { },
                        onGetStronger = { backStack.add(RoutineSelectorRoute) },
                        onStartFirstWorkout = { backStack.add(RoutineSelectorRoute) },
                        onNavigateToProfile = { backStack.add(ProfileRoute) },
                        onCameraClick = {
                            navigateToVisualProgress() // Use protected navigation
                        },
                        onChatClick = {
                            // TODO: Navigate to Chat screen when implemented
                        },
                        onMapClick = {
                            backStack.add(GymFinderRoute) // << también desde Progress
                        },
                        currentRoute = "progress"
                    )
                }

                is RoutineSelectorRoute -> NavEntry(key) {
                    RoutineSelectorScreen(
                        onSplitSelected = { _ ->
                            backStack.add(WorkoutSummaryRoute)
                        },
                        onGoHome = {
                            backStack.removeLastOrNull()
                        },
                        workoutViewModel = workoutViewModel
                    )
                }

                is WorkoutSummaryRoute -> NavEntry(key) {
                    WorkoutSummaryScreen(
                        onStartWorkout = {
                            // Navigation will be triggered from WorkoutSummaryScreen after session is ready
                            backStack.add(ActiveWorkoutRoute)
                        },
                        onBackToHome = {
                            backStack.removeLastOrNull()
                        },
                        workoutViewModel = workoutViewModel
                    )
                }

                is ActiveWorkoutRoute -> NavEntry(key) {
                    val todaysSplitDay = workoutViewModel.getTodaysSplitDay()
                    val currentExercises = workoutState.currentExercises
                    
                    if (todaysSplitDay != null) {
                        ActiveWorkoutScreen(
                            selectedSplitDay = todaysSplitDay,
                            exercises = currentExercises,
                            onFinishWorkout = {
                                backStack.add(CompletedWorkoutRoute)
                            },
                            onBackToHome = {
                                backStack.removeLastOrNull()
                            },
                            workoutViewModel = workoutViewModel
                        )
                    } else {
                        // No split day available - show empty state
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("No Workout Available") },
                                    navigationIcon = {
                                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                    }
                                )
                            }
                        ) { padding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.fitness_center_24px),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Workout Scheduled",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Create a training split in Settings to get started!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        backStack.clear()
                                        backStack.add(HomeRoute)
                                        backStack.add(ProfileRoute)
                                        backStack.add(TrainingSplitSettingsRoute)
                                    }
                                ) {
                                    Text("Go to Training Settings")
                                }
                            }
                        }
                    }
                }

                is CompletedWorkoutRoute -> NavEntry(key) {
                    val todaysSplitDay = workoutViewModel.getTodaysSplitDay()
                    
                    if (todaysSplitDay != null) {
                        CompletedWorkoutScreen(
                            completedSplitDay = todaysSplitDay,
                            completedExercises = 3,
                            totalSets = 6,
                            onReturnToWorkout = {
                                backStack.removeLastOrNull()
                            },
                            onFinishWorkout = {
                                backStack.clear()
                                backStack.add(HomeRoute)
                            }
                        )
                    } else {
                        // Fallback - just go home
                        LaunchedEffect(Unit) {
                            backStack.clear()
                            backStack.add(HomeRoute)
                        }
                    }
                }

                is TrainingSplitSettingsRoute -> NavEntry(key) {
                    TrainingSplitSettingsScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
                        onSelectRoutines = {
                            backStack.add(SelectRoutinesRoute)
                        },
                        workoutViewModel = workoutViewModel
                    )
                }

                is SelectRoutinesRoute -> NavEntry(key) {
                    SelectRoutinesScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
                        onBackToSettings = {
                            backStack.clear()
                            backStack.add(HomeRoute)
                            backStack.add(ProfileRoute)
                            backStack.add(TrainingSplitSettingsRoute)
                        },
                        onNextClick = { selectedRoutines ->
                            backStack.add(CreateSplitDayRoute(selectedRoutines))
                        }
                    )
                }

                is CreateSplitDayRoute -> NavEntry(key) {
                    CreateSplitDayScreen(
                        availableRoutines = key.selectedRoutines,
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
                        onBackToSettings = {
                            backStack.clear()
                            backStack.add(HomeRoute)
                            backStack.add(ProfileRoute)
                            backStack.add(TrainingSplitSettingsRoute)
                        },
                        onNextClick = { finalDayState: List<DayState> ->
                            backStack.add(
                                ReviewSplitRoute(
                                    splitName = "My Training Split",
                                    days = finalDayState
                                ))
                        }
                    )
                }

                is ReviewSplitRoute -> NavEntry(key) {
                    ReviewSplitScreen(
                        splitName = key.splitName,
                        days = key.days,
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
                        onBackToSettings = {
                            backStack.clear()
                            backStack.add(HomeRoute)
                            backStack.add(ProfileRoute)
                            backStack.add(TrainingSplitSettingsRoute)
                        },
                        onFinishClick = {
                            backStack.clear()
                            backStack.add(HomeRoute)
                            backStack.add(ProfileRoute)
                            backStack.add(TrainingSplitSettingsRoute)
                        },
                        workoutViewModel = workoutViewModel,
                        onSaveSplit = { splitName, dayAssignments ->
                            // Determine which template to use based on the routines selected
                            val routineNames = dayAssignments.values.filter { it != "Rest" }.distinct()
                            
                            val templateName = when {
                                routineNames.containsAll(listOf("Push", "Pull", "Legs")) -> "Push/Pull/Legs"
                                routineNames.containsAll(listOf("Upper Body", "Lower Body")) -> "Upper/Lower"
                                routineNames.contains("Full Body") -> "Full Body"
                                else -> "Push/Pull/Legs" // Default fallback
                            }
                            
                            // Create split from template
                            workoutViewModel.createSplitFromTemplate(
                                templateName = templateName,
                                splitName = splitName,
                                dayAssignments = dayAssignments
                            )
                            
                            // Navigate back to settings after a short delay
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                kotlinx.coroutines.delay(1000) // Wait for save to complete
                                backStack.clear()
                                backStack.add(HomeRoute)
                                backStack.add(ProfileRoute)
                                backStack.add(TrainingSplitSettingsRoute)
                            }
                        }
                    )
                }

                is VisualProgressRoute -> NavEntry(key) {
                    VisualProgressScreen(
                        photos = photoState.photos,
                        onBackClick = {
                            //visualProgressAuthenticated = false // Reset auth state on exit
                            backStack.removeLastOrNull()
                        },
                        onPhotoClick = { _ ->
                            // Photo detail is handled within the screen via dialog
                        },
                        onAddPhotoClick = {
                            backStack.add(CameraRoute)
                        },
                        onDeletePhoto = { photoId ->
                            progressPhotoViewModel.deleteProgressPhoto(photoId)
                        }
                    )
                }

                is CameraRoute -> NavEntry(key) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    CameraScreen(
                        onBackClick = {
                            progressPhotoViewModel.clearCapturedPhoto()
                            backStack.removeLastOrNull()
                        },
                        onPhotoCaptured = { uri, file ->
                            progressPhotoViewModel.setCapturedPhotoUri(uri, file)
                            backStack.add(PhotoPreviewRoute(photoUriString = uri.toString()))
                        }
                    )
                }

                is PhotoPreviewRoute -> NavEntry(key) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val photoUri = Uri.parse(key.photoUriString)

                    PhotoPreviewScreen(
                        photoUri = photoUri,
                        progressPhotoViewModel = progressPhotoViewModel, // ← Añade esto
                        onSaveClick = { weight, notes ->
                            val success = progressPhotoViewModel.saveProgressPhoto(
                                photoUri = photoUri,
                                weight = weight,
                                notes = notes,
                                context = context
                            )

                            if (success) {
                                backStack.removeLastOrNull() // Remove preview
                                backStack.removeLastOrNull() // Remove camera
                            }
                        },
                        onRetakeClick = {
                            backStack.removeLastOrNull()
                        },
                        onCancelClick = {
                            progressPhotoViewModel.clearCapturedPhoto()
                            backStack.removeLastOrNull()
                            backStack.removeLastOrNull()
                        }
                    )
                }

                is GymFinderRoute -> NavEntry(key) {
                    GymFinderScreen()
                }

                is ChatRoomsRoute -> NavEntry(key) {
                    ChatRoomsScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
                        onRoomClick = { chatRoom ->
                            backStack.add(ChatRoomRoute(chatRoom.id))
                        }
                    )
                }

                is ChatRoomRoute -> NavEntry(key) {
                    val chatRoom = ChatRoom.entries.find { it.id == key.roomId } ?: ChatRoom.GENERAL
                    ChatScreen(
                        chatRoom = chatRoom,
                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                else -> throw RuntimeException("Unknown route: $key")
            }
        }
    )
}
