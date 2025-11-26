package com.example.liftium.data.repository

import android.util.Log
import com.example.liftium.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class SplitRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val splitsCollection = firestore.collection("splits")
    private val splitDaysCollection = firestore.collection("split_days")
    private val exercisesCollection = firestore.collection("exercises")
    
    companion object {
        private const val TAG = "SplitRepository"
    }
    
    /**
     * Creates a new split for the current user
     */
    suspend fun createSplit(name: String): Result<Split> {
        return try {
            val currentUser = auth.currentUser
            val userId = currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Creating split for user: $userId, email: ${currentUser.email}")
            
            val split = Split(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                createdAt = LocalDateTime.now()
            )
            
            val splitMap = hashMapOf(
                "id" to split.id,
                "userId" to split.userId,
                "name" to split.name,
                "createdAt" to split.createdAt.toEpochSecond(ZoneOffset.UTC)
            )
            
            splitsCollection.document(split.id).set(splitMap).await()
            Log.d(TAG, "Split created successfully: ${split.id} for user: $userId")
            Result.success(split)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating split", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets all splits for the current user
     */
    suspend fun getUserSplits(): Result<List<Split>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Getting splits for user: $userId")
            
            // Query without orderBy to avoid needing a composite index
            // We'll sort in memory instead
            val documents = splitsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            Log.d(TAG, "Found ${documents.size()} split documents")
            
            if (documents.isEmpty) {
                Log.w(TAG, "No splits found for user $userId")
                // Try to get all splits to see what's in the database
                val allDocs = splitsCollection.get().await()
                Log.d(TAG, "Total splits in database: ${allDocs.size()}")
                Log.d(TAG, "Current user email: ${auth.currentUser?.email}")
                allDocs.documents.forEach { doc ->
                    val docUserId = doc.getString("userId")
                    val matches = docUserId == userId
                    Log.d(TAG, "Split doc - ID: ${doc.id}, userId: $docUserId, name: ${doc.getString("name")}, matches: $matches")
                }
            } else {
                Log.d(TAG, "Query returned ${documents.size()} documents for user $userId")
            }
            
            val splits = documents.documents.mapNotNull { document ->
                try {
                    val split = Split(
                        id = document.getString("id") ?: return@mapNotNull null,
                        userId = document.getString("userId") ?: return@mapNotNull null,
                        name = document.getString("name") ?: return@mapNotNull null,
                        createdAt = document.getLong("createdAt")?.let {
                            LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
                        } ?: LocalDateTime.now()
                    )
                    Log.d(TAG, "Parsed split: ${split.name} (${split.id})")
                    split
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing split document: ${document.id}", e)
                    null
                }
            }
            
            // Sort in memory by createdAt descending
            val sortedSplits = splits.sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Returning ${sortedSplits.size} splits")
            Result.success(sortedSplits)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user splits: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Creates a split day
     */
    suspend fun createSplitDay(splitDay: SplitDay): Result<SplitDay> {
        return try {
            val splitDayMap = hashMapOf(
                "id" to splitDay.id,
                "splitId" to splitDay.splitId,
                "dayOfWeek" to splitDay.dayOfWeek,
                "name" to splitDay.name,
                "isRestDay" to splitDay.isRestDay
            )
            
            splitDaysCollection.document(splitDay.id).set(splitDayMap).await()
            Log.d(TAG, "Split day created successfully: ${splitDay.id}")
            Result.success(splitDay)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating split day", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets split days for a specific split
     */
    suspend fun getSplitDays(splitId: String): Result<List<SplitDay>> {
        return try {
            // Query without orderBy to avoid needing a composite index
            val documents = splitDaysCollection
                .whereEqualTo("splitId", splitId)
                .get()
                .await()
            
            val splitDays = documents.documents.mapNotNull { document ->
                try {
                    SplitDay(
                        id = document.getString("id") ?: return@mapNotNull null,
                        splitId = document.getString("splitId") ?: return@mapNotNull null,
                        dayOfWeek = document.getLong("dayOfWeek")?.toInt() ?: return@mapNotNull null,
                        name = document.getString("name") ?: return@mapNotNull null,
                        isRestDay = document.getBoolean("isRestDay") ?: false
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing split day document", e)
                    null
                }
            }
            
            // Sort in memory by dayOfWeek ascending
            val sortedSplitDays = splitDays.sortedBy { it.dayOfWeek }
            
            Result.success(sortedSplitDays)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting split days", e)
            Result.failure(e)
        }
    }
    
    /**
     * Creates an exercise for a split day
     */
    suspend fun createExercise(exercise: Exercise): Result<Exercise> {
        return try {
            val exerciseMap = hashMapOf(
                "id" to exercise.id,
                "splitDayId" to exercise.splitDayId,
                "name" to exercise.name,
                "defaultSets" to exercise.defaultSets,
                "restTimeSec" to exercise.restTimeSec,
                "note" to exercise.note,
                "exerciseOrder" to exercise.exerciseOrder,
                "muscleGroups" to exercise.muscleGroups
            )
            
            exercisesCollection.document(exercise.id).set(exerciseMap).await()
            Log.d(TAG, "Exercise created successfully: ${exercise.id}")
            Result.success(exercise)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating exercise", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets exercises for a specific split day
     */
    suspend fun getExercisesForSplitDay(splitDayId: String): Result<List<Exercise>> {
        return try {
            // Query without orderBy to avoid needing a composite index
            val documents = exercisesCollection
                .whereEqualTo("splitDayId", splitDayId)
                .get()
                .await()
            
            val exercises = documents.documents.mapNotNull { document ->
                try {
                    Exercise(
                        id = document.getString("id") ?: return@mapNotNull null,
                        splitDayId = document.getString("splitDayId") ?: return@mapNotNull null,
                        name = document.getString("name") ?: return@mapNotNull null,
                        defaultSets = document.getLong("defaultSets")?.toInt() ?: return@mapNotNull null,
                        restTimeSec = document.getLong("restTimeSec")?.toInt() ?: return@mapNotNull null,
                        note = document.getString("note"),
                        exerciseOrder = document.getLong("exerciseOrder")?.toInt() ?: return@mapNotNull null,
                        muscleGroups = document.getString("muscleGroups") ?: return@mapNotNull null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing exercise document", e)
                    null
                }
            }
            
            // Sort in memory by exerciseOrder ascending
            val sortedExercises = exercises.sortedBy { it.exerciseOrder }
            
            Result.success(sortedExercises)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting exercises for split day", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets a complete split with all its days and exercises
     */
    suspend fun getSplitWithDays(splitId: String): Result<SplitWithDays?> {
        return try {
            val splitDoc = splitsCollection.document(splitId).get().await()
            if (!splitDoc.exists()) {
                return Result.success(null)
            }
            
            val split = Split(
                id = splitDoc.getString("id") ?: return Result.success(null),
                userId = splitDoc.getString("userId") ?: return Result.success(null),
                name = splitDoc.getString("name") ?: return Result.success(null),
                createdAt = splitDoc.getLong("createdAt")?.let {
                    LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
                } ?: LocalDateTime.now()
            )
            
            val splitDaysResult = getSplitDays(splitId)
            val splitDays = splitDaysResult.getOrNull() ?: emptyList()
            
            val splitDaysWithExercises = splitDays.map { splitDay ->
                val exercisesResult = getExercisesForSplitDay(splitDay.id)
                val exercises = exercisesResult.getOrNull() ?: emptyList()
                SplitDayWithExercises(splitDay, exercises)
            }
            
            Result.success(SplitWithDays(split, splitDaysWithExercises))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting split with days", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deletes a split and all associated data
     */
    suspend fun deleteSplit(splitId: String): Result<Unit> {
        return try {
            // Delete all exercises for this split
            val splitDaysResult = getSplitDays(splitId)
            val splitDays = splitDaysResult.getOrNull() ?: emptyList()
            
            for (splitDay in splitDays) {
                val exercisesResult = getExercisesForSplitDay(splitDay.id)
                val exercises = exercisesResult.getOrNull() ?: emptyList()
                
                for (exercise in exercises) {
                    exercisesCollection.document(exercise.id).delete().await()
                }
                
                splitDaysCollection.document(splitDay.id).delete().await()
            }
            
            // Delete the split itself
            splitsCollection.document(splitId).delete().await()
            Log.d(TAG, "Split deleted successfully: $splitId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting split", e)
            Result.failure(e)
        }
    }
}

