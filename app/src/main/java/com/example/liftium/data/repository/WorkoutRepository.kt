package com.example.liftium.data.repository

import android.util.Log
import com.example.liftium.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class WorkoutRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val sessionsCollection = firestore.collection("sessions")
    private val setsCollection = firestore.collection("sets")
    
    companion object {
        private const val TAG = "WorkoutRepository"
    }
    
    /**
     * Creates a new workout session
     */
    suspend fun createSession(splitDayId: String): Result<Session> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            val session = Session(
                id = UUID.randomUUID().toString(),
                userId = userId,
                splitDayId = splitDayId,
                date = LocalDate.now(),
                createdAt = LocalDateTime.now()
            )
            
            val sessionMap = hashMapOf(
                "id" to session.id,
                "userId" to session.userId,
                "splitDayId" to session.splitDayId,
                "date" to session.date.toEpochDay(),
                "createdAt" to session.createdAt.toEpochSecond(ZoneOffset.UTC),
                "completedAt" to null,
                "isCompleted" to false
            )
            
            sessionsCollection.document(session.id).set(sessionMap).await()
            Log.d(TAG, "Session created successfully: ${session.id}")
            Result.success(session)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating session", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets recent sessions for the current user
     */
    suspend fun getRecentSessions(limit: Int = 10): Result<List<Session>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            // Query without orderBy to avoid needing a composite index
            val documents = sessionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val sessions = documents.documents.mapNotNull { document ->
                try {
                    Session(
                        id = document.getString("id") ?: return@mapNotNull null,
                        userId = document.getString("userId") ?: return@mapNotNull null,
                        splitDayId = document.getString("splitDayId") ?: return@mapNotNull null,
                        date = document.getLong("date")?.let {
                            LocalDate.ofEpochDay(it)
                        } ?: LocalDate.now(),
                        createdAt = document.getLong("createdAt")?.let {
                            LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
                        } ?: LocalDateTime.now(),
                        completedAt = document.getLong("completedAt")?.let {
                            LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
                        },
                        isCompleted = document.getBoolean("isCompleted") ?: false
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing session document", e)
                    null
                }
            }
            
            // Sort in memory by createdAt descending and apply limit
            val sortedSessions = sessions.sortedByDescending { it.createdAt }.take(limit)
            
            Result.success(sortedSessions)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent sessions", e)
            Result.failure(e)
        }
    }
    
    /**
     * Saves a workout set
     */
    suspend fun saveSet(workoutSet: WorkoutSet): Result<WorkoutSet> {
        return try {
            val setMap = hashMapOf(
                "id" to workoutSet.id,
                "sessionId" to workoutSet.sessionId,
                "exerciseId" to workoutSet.exerciseId,
                "setNumber" to workoutSet.setNumber,
                "reps" to workoutSet.reps,
                "weight" to workoutSet.weight
            )
            
            setsCollection.document(workoutSet.id).set(setMap).await()
            Log.d(TAG, "Set saved successfully: ${workoutSet.id}")
            Result.success(workoutSet)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving set", e)
            Result.failure(e)
        }
    }
    
    /**
     * Completes a workout session
     */
    suspend fun completeSession(sessionId: String): Result<Session> {
        return try {
            val completedAt = LocalDateTime.now()
            val updateMap = hashMapOf<String, Any>(
                "completedAt" to completedAt.toEpochSecond(ZoneOffset.UTC),
                "isCompleted" to true
            )
            
            sessionsCollection.document(sessionId).update(updateMap).await()
            Log.d(TAG, "Session completed successfully: $sessionId")
            
            // Fetch and return the updated session
            val document = sessionsCollection.document(sessionId).get().await()
            val session = Session(
                id = document.getString("id") ?: sessionId,
                userId = document.getString("userId") ?: "",
                splitDayId = document.getString("splitDayId") ?: "",
                date = document.getLong("date")?.let {
                    LocalDate.ofEpochDay(it)
                } ?: LocalDate.now(),
                createdAt = document.getLong("createdAt")?.let {
                    LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
                } ?: LocalDateTime.now(),
                completedAt = completedAt,
                isCompleted = true
            )
            
            Result.success(session)
        } catch (e: Exception) {
            Log.e(TAG, "Error completing session", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets all sets for a specific session
     */
    suspend fun getSetsForSession(sessionId: String): Result<List<WorkoutSet>> {
        return try {
            // Query without orderBy to avoid needing a composite index
            val documents = setsCollection
                .whereEqualTo("sessionId", sessionId)
                .get()
                .await()
            
            val sets = documents.documents.mapNotNull { document ->
                try {
                    WorkoutSet(
                        id = document.getString("id") ?: return@mapNotNull null,
                        sessionId = document.getString("sessionId") ?: return@mapNotNull null,
                        exerciseId = document.getString("exerciseId") ?: return@mapNotNull null,
                        setNumber = document.getLong("setNumber")?.toInt() ?: return@mapNotNull null,
                        reps = document.getLong("reps")?.toInt() ?: return@mapNotNull null,
                        weight = document.getDouble("weight") ?: return@mapNotNull null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing set document", e)
                    null
                }
            }
            
            // Sort in memory by setNumber ascending
            val sortedSets = sets.sortedBy { it.setNumber }
            
            Result.success(sortedSets)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sets for session", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets all sets for a specific exercise across all sessions
     */
    suspend fun getSetsForExercise(exerciseId: String): Result<List<WorkoutSet>> {
        return try {
            val documents = setsCollection
                .whereEqualTo("exerciseId", exerciseId)
                .get()
                .await()
            
            val sets = documents.documents.mapNotNull { document ->
                try {
                    WorkoutSet(
                        id = document.getString("id") ?: return@mapNotNull null,
                        sessionId = document.getString("sessionId") ?: return@mapNotNull null,
                        exerciseId = document.getString("exerciseId") ?: return@mapNotNull null,
                        setNumber = document.getLong("setNumber")?.toInt() ?: return@mapNotNull null,
                        reps = document.getLong("reps")?.toInt() ?: return@mapNotNull null,
                        weight = document.getDouble("weight") ?: return@mapNotNull null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing set document", e)
                    null
                }
            }
            
            // Sort in memory by setNumber if needed
            val sortedSets = sets.sortedBy { it.setNumber }
            
            Result.success(sortedSets)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sets for exercise", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets workout statistics for the current user
     */
    suspend fun getProgressStats(): Result<ProgressStats> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            val sessionsResult = getRecentSessions(100)
            val sessions = sessionsResult.getOrNull() ?: emptyList()
            
            // Calculate total workouts
            val totalWorkouts = sessions.size
            
            // Calculate current streak (simplified)
            var currentStreak = 0
            val sortedSessions = sessions.sortedByDescending { it.date }
            var currentDate = LocalDate.now()
            
            for (session in sortedSessions) {
                if (session.date == currentDate || session.date == currentDate.minusDays(1)) {
                    currentStreak++
                    currentDate = session.date.minusDays(1)
                } else {
                    break
                }
            }
            
            // Get all sets for volume calculation
            var totalVolume = 0.0
            val exerciseVolumes = mutableMapOf<String, Double>()
            
            for (session in sessions) {
                val setsResult = getSetsForSession(session.id)
                val sets = setsResult.getOrNull() ?: emptyList()
                
                for (set in sets) {
                    val volume = set.weight * set.reps
                    totalVolume += volume
                    exerciseVolumes[set.exerciseId] = (exerciseVolumes[set.exerciseId] ?: 0.0) + volume
                }
            }
            
            val favoriteExerciseId = exerciseVolumes.maxByOrNull { it.value }?.key
            val favoriteExercise = favoriteExerciseId ?: "None"
            
            val stats = ProgressStats(
                totalWorkouts = totalWorkouts,
                currentStreak = currentStreak,
                longestStreak = currentStreak, // Simplified - would need more logic for actual longest streak
                totalVolume = totalVolume,
                favoriteExercise = favoriteExercise,
                averageWorkoutDuration = 60 // Placeholder
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating progress stats", e)
            Result.failure(e)
        }
    }
}

