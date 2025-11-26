package com.example.liftium.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liftium.data.repository.SplitRepository
import com.example.liftium.data.repository.TemplateRoutineRepository
import com.example.liftium.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WorkoutState(
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // User's splits
    val userSplits: List<Split> = emptyList(),
    val currentSplit: SplitWithDays? = null,
    val selectedSplitDay: SplitDay? = null,
    
    // Current session
    val currentSession: Session? = null,
    val currentExercises: List<Exercise> = emptyList(),
    val currentSessionSets: List<WorkoutSet> = emptyList(),
    
    // Recent workouts
    val recentSessions: List<Session> = emptyList(),
    val recentWorkoutsData: List<RecentWorkoutData> = emptyList(),
    
    // Progress stats
    val progressStats: ProgressStats? = null,
    
    // Today's workout info
    val todaysWorkoutName: String = "No Workout",
    val hasWorkoutToday: Boolean = false,
    val workoutStatus: String = "Rest Day"
)

class WorkoutViewModel : ViewModel() {
    private val splitRepository = SplitRepository()
    private val workoutRepository = WorkoutRepository()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    
    private val _state = MutableStateFlow(WorkoutState())
    val state: StateFlow<WorkoutState> = _state.asStateFlow()
    
    private var currentUserId: String? = null
    
    companion object {
        private const val TAG = "WorkoutViewModel"
        
        // Fallback motivational messages
        private val motivationalMessages = listOf(
            "Today is a great day for a workout!",
            "Your only limit is your mind!",
            "Strong is the new beautiful!",
            "Every workout counts!",
            "Push your limits today!",
            "Consistency is key!",
            "You're stronger than yesterday!"
        )
    }
    
    init {
        // Set up auth state listener to detect user changes
        auth.addAuthStateListener { firebaseAuth ->
            val newUserId = firebaseAuth.currentUser?.uid
            if (newUserId != currentUserId) {
                Log.d(TAG, "User changed from $currentUserId to $newUserId")
                currentUserId = newUserId
                if (newUserId != null) {
                    // User logged in or switched - reload data
                    clearState()
                    loadUserData()
                } else {
                    // User logged out - clear state
                    clearState()
                }
            }
        }
        
        // Initial load if user is already logged in
        if (auth.currentUser != null) {
            currentUserId = auth.currentUser?.uid
            loadUserData()
        }
    }
    
    /**
     * Clear all state data
     */
    fun clearState() {
        Log.d(TAG, "Clearing all state data")
        _state.value = WorkoutState()
    }
    
    /**
     * Force reload of all data (bypasses cache)
     */
    fun forceReload() {
        Log.d(TAG, "Force reloading all data")
        clearState()
        loadUserData()
    }
    
    /**
     * Load all user workout data
     */
    fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Log.w(TAG, "No user logged in, skipping data load")
                    _state.value = _state.value.copy(isLoading = false)
                    return@launch
                }
                
                Log.d(TAG, "Loading data for user: ${currentUser.uid}, email: ${currentUser.email}")
                
                // Load user's splits
                val splitsResult = splitRepository.getUserSplits()
                val splits = splitsResult.getOrElse {
                    Log.e(TAG, "Error loading splits: ${it.message}", it)
                    emptyList()
                }
                
                Log.d(TAG, "Loaded ${splits.size} splits")
                
                // Load current split (most recent)
                val currentSplit = if (splits.isNotEmpty()) {
                    splitRepository.getSplitWithDays(splits.first().id).getOrNull()
                } else {
                    null
                }
                
                // Load recent sessions
                val sessionsResult = workoutRepository.getRecentSessions(10)
                val sessions = sessionsResult.getOrElse {
                    Log.e(TAG, "Error loading sessions", it)
                    emptyList()
                }
                
                // Load progress stats
                val statsResult = workoutRepository.getProgressStats()
                val stats = statsResult.getOrNull()
                
                // Determine today's workout
                val todaysDayOfWeek = LocalDate.now().dayOfWeek.value % 7 // Convert to 0-6
                val todaysSplitDay = currentSplit?.splitDays?.find { 
                    it.splitDay.dayOfWeek == todaysDayOfWeek 
                }
                
                val hasWorkout = todaysSplitDay != null && !todaysSplitDay.splitDay.isRestDay
                val workoutName = todaysSplitDay?.splitDay?.name ?: "No Workout"
                val workoutStatus = if (hasWorkout) "Available" else "Rest Day"
                
                // Convert sessions to RecentWorkoutData for UI
                val recentWorkoutsData = convertSessionsToWorkoutData(sessions, currentSplit)
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    userSplits = splits,
                    currentSplit = currentSplit,
                    recentSessions = sessions,
                    recentWorkoutsData = recentWorkoutsData,
                    progressStats = stats,
                    todaysWorkoutName = workoutName,
                    hasWorkoutToday = hasWorkout,
                    workoutStatus = workoutStatus
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user data", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load workout data. Please try again."
                )
            }
        }
    }
    
    /**
     * Create a new split from a template
     */
    fun createSplitFromTemplate(
        templateName: String,
        splitName: String,
        dayAssignments: Map<Int, String>
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Check if user is authenticated
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e(TAG, "User not authenticated")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Please log in to create a split"
                    )
                    return@launch
                }
                
                Log.d(TAG, "Creating split for user: ${currentUser.uid}")
                Log.d(TAG, "Template: $templateName, Split name: $splitName")
                Log.d(TAG, "Day assignments: $dayAssignments")
                
                val result = TemplateRoutineRepository.copyTemplateToUser(
                    templateName = templateName,
                    splitName = splitName,
                    dayAssignments = dayAssignments,
                    splitRepository = splitRepository
                )
                
                result.onSuccess { createdSplit ->
                    Log.d(TAG, "Split created successfully: ${createdSplit.split.name} (${createdSplit.split.id})")
                    Log.d(TAG, "Split has ${createdSplit.splitDays.size} days")
                    
                    // Small delay to ensure Firestore has indexed the data
                    kotlinx.coroutines.delay(500)
                    
                    // Reload user data to reflect the new split
                    loadUserData()
                }.onFailure { exception ->
                    Log.e(TAG, "Error creating split from template: ${exception.message}", exception)
                    val errorMessage = when {
                        exception.message?.contains("PERMISSION_DENIED") == true -> 
                            "Permission denied. Please check Firestore security rules."
                        exception.message?.contains("UNAVAILABLE") == true -> 
                            "Network error. Please check your connection."
                        else -> "Failed to create split: ${exception.message}"
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception creating split from template: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to create split: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Start a new workout session
     */
    fun startWorkoutSession(splitDayId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val sessionResult = workoutRepository.createSession(splitDayId)
                val session = sessionResult.getOrElse {
                    Log.e(TAG, "Error creating session", it)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to start workout session. Please try again."
                    )
                    return@launch
                }
                
                // Load exercises for this split day
                val exercisesResult = splitRepository.getExercisesForSplitDay(splitDayId)
                val exercises = exercisesResult.getOrElse {
                    Log.e(TAG, "Error loading exercises", it)
                    emptyList()
                }
                
                // Find the split day
                val splitDay = _state.value.currentSplit?.splitDays?.find {
                    it.splitDay.id == splitDayId
                }?.splitDay
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    currentSession = session,
                    selectedSplitDay = splitDay,
                    currentExercises = exercises
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error starting workout session", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to start workout session. Please try again."
                )
            }
        }
    }
    
    /**
     * Save a workout set
     */
    fun saveWorkoutSet(
        exerciseId: String, 
        setNumber: Int, 
        reps: Int, 
        weight: Double
    ) {
        viewModelScope.launch {
            val session = _state.value.currentSession ?: run {
                Log.e(TAG, "No active session found")
                _state.value = _state.value.copy(
                    error = "No active workout session. Please start a workout first."
                )
                return@launch
            }
            
            val workoutSet = WorkoutSet(
                sessionId = session.id,
                exerciseId = exerciseId,
                setNumber = setNumber,
                reps = reps,
                weight = weight
            )
            
            Log.d(TAG, "Saving set: Exercise=$exerciseId, Set=$setNumber, Reps=$reps, Weight=$weight")
            
            val result = workoutRepository.saveSet(workoutSet)
            result.onSuccess {
                Log.d(TAG, "Set saved successfully")
                // Add the set to current session sets
                val currentSets = _state.value.currentSessionSets.toMutableList()
                currentSets.add(workoutSet)
                _state.value = _state.value.copy(
                    currentSessionSets = currentSets
                )
            }.onFailure {
                Log.e(TAG, "Error saving workout set: ${it.message}", it)
                _state.value = _state.value.copy(
                    error = "Failed to save set. Please try again."
                )
            }
        }
    }
    
    /**
     * Finish the current workout session
     */
    fun finishWorkoutSession() {
        viewModelScope.launch {
            val session = _state.value.currentSession ?: run {
                Log.e(TAG, "No active session to finish")
                return@launch
            }
            
            Log.d(TAG, "Finishing workout session: ${session.id}")
            
            try {
                val result = workoutRepository.completeSession(session.id)
                result.onSuccess { completedSession ->
                    Log.d(TAG, "Workout session completed successfully")
                    _state.value = _state.value.copy(
                        currentSession = null,
                        selectedSplitDay = null,
                        currentExercises = emptyList(),
                        currentSessionSets = emptyList()
                    )
                    // Reload data to update home screen stats
                    loadUserData()
                }.onFailure {
                    Log.e(TAG, "Error finishing workout session: ${it.message}", it)
                    _state.value = _state.value.copy(
                        error = "Failed to finish workout. Your progress was saved."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception finishing workout session: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "Failed to finish workout. Your progress was saved."
                )
            }
        }
    }
    
    /**
     * Load previous sets for an exercise (from the last completed session)
     */
    suspend fun getPreviousSetsForExercise(exerciseId: String): List<WorkoutSet> {
        return try {
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.w(TAG, "User not authenticated")
                return emptyList()
            }
            
            // Get recent sessions
            val sessionsResult = workoutRepository.getRecentSessions(limit = 20)
            val sessions = sessionsResult.getOrNull() ?: emptyList()
            
            // Find the most recent completed session for this exercise
            for (session in sessions.filter { it.isCompleted }) {
                val setsResult = workoutRepository.getSetsForSession(session.id)
                val sets = setsResult.getOrNull() ?: emptyList()
                val exerciseSets = sets.filter { it.exerciseId == exerciseId }
                
                if (exerciseSets.isNotEmpty()) {
                    Log.d(TAG, "Found ${exerciseSets.size} previous sets for exercise $exerciseId")
                    return exerciseSets
                }
            }
            
            Log.d(TAG, "No previous sets found for exercise $exerciseId")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting previous sets: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Get today's split day
     */
    fun getTodaysSplitDay(): SplitDay? {
        val todaysDayOfWeek = LocalDate.now().dayOfWeek.value % 7
        return _state.value.currentSplit?.splitDays?.find { 
            it.splitDay.dayOfWeek == todaysDayOfWeek 
        }?.splitDay
    }
    
    /**
     * Get a random motivational message
     */
    fun getRandomMotivationalMessage(): String {
        return motivationalMessages.random()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    /**
     * Helper function to convert sessions to RecentWorkoutData for UI
     */
    private suspend fun convertSessionsToWorkoutData(
        sessions: List<Session>,
        currentSplit: SplitWithDays?
    ): List<RecentWorkoutData> {
        return sessions.take(5).mapNotNull { session ->
            try {
                val splitDay = currentSplit?.splitDays?.find { 
                    it.splitDay.id == session.splitDayId 
                }
                
                if (splitDay == null) return@mapNotNull null
                
                val setsResult = workoutRepository.getSetsForSession(session.id)
                val sets = setsResult.getOrNull() ?: emptyList()
                
                val exercisesCount = sets.map { it.exerciseId }.distinct().size
                val totalSets = sets.size
                
                // Calculate progress (simplified - based on completion)
                val expectedSets = splitDay.exercises.sumOf { it.defaultSets }
                val progress = if (expectedSets > 0) {
                    ((totalSets.toDouble() / expectedSets) * 100).toInt().coerceIn(0, 100)
                } else {
                    100
                }
                
                // Format date
                val date = when {
                    session.date == LocalDate.now() -> "Today"
                    session.date == LocalDate.now().minusDays(1) -> "Yesterday"
                    else -> "${LocalDate.now().toEpochDay() - session.date.toEpochDay()} days ago"
                }
                
                RecentWorkoutData(
                    id = session.id,
                    name = splitDay.splitDay.name,
                    date = date,
                    progress = progress,
                    exercises = exercisesCount,
                    totalSets = totalSets,
                    mainFocus = splitDay.exercises.firstOrNull()?.muscleGroups ?: "Mixed",
                    tags = emptyList(),
                    trophy = if (progress >= 90) WorkoutTrophy(TrophyType.GOLD) else null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error converting session to workout data", e)
                null
            }
        }
    }
}

