package com.example.liftium.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// 1. Users Table
data class User(
    val id: String, // UUID - Firebase Auth UID
    val email: String,
    val createdAt: LocalDateTime,
    val userName: String // Display name from Firebase Auth
)

// 2. Splits Table
data class Split(
    val id: String, // UUID
    val userId: String, // FK → users.id
    val name: String, // e.g. "Push/Pull/Legs", "Bro Split"
    val createdAt: LocalDateTime
)

// 3. Split Days Table
data class SplitDay(
    val id: String, // UUID
    val splitId: String, // FK → splits.id
    val dayOfWeek: Int, // 0 (Sunday) to 6 (Saturday)
    val name: String, // e.g. "Push", "Legs", "Rest"
    val isRestDay: Boolean // defines if its a rest day or not
)

// 4. Exercises Table
data class Exercise(
    val id: String, // UUID
    val splitDayId: String, // FK → split_days.id
    val name: String, // e.g. "Bench Press"
    val defaultSets: Int, // e.g. 3 sets
    val restTimeSec: Int, // rest time between sets
    val note: String? = null, // Optional notes e.g "adjust seat to 45 degrees"
    val exerciseOrder: Int, // Order of the Exercise within the training day
    val muscleGroups: String // Muscle group targeted in the exercise
)

// 5. Sessions Table
data class Session(
    val id: String, // UUID
    val userId: String, // FK → users.id
    val splitDayId: String, // FK → split_days.id
    val date: LocalDate, // e.g. 2025-04-12
    val createdAt: LocalDateTime, // Start of the workout session
    val completedAt: LocalDateTime? = null, // When workout was finished
    val isCompleted: Boolean = false // Whether workout is completed
)

// 6. Sets (Workout Logs) Table
data class WorkoutSet(
    val id: String = java.util.UUID.randomUUID().toString(), // UUID
    val sessionId: String, // FK → sessions.id
    val exerciseId: String, // FK → exercises.id
    val setNumber: Int, // Set index (1, 2, 3…)
    val reps: Int, // Actual reps performed
    val weight: Double // Actual weight used
)

// Helper data classes for UI and business logic
data class SessionWithDetails(
    val session: Session,
    val splitDay: SplitDay,
    val split: Split,
    val exercises: List<ExerciseWithSets>
)

data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: List<WorkoutSet>
)

data class SplitWithDays(
    val split: Split,
    val splitDays: List<SplitDayWithExercises>
)

data class SplitDayWithExercises(
    val splitDay: SplitDay,
    val exercises: List<Exercise>
)

// Progress and statistics data classes
data class ProgressStats(
    val totalWorkouts: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalVolume: Double, // total weight lifted
    val favoriteExercise: String,
    val averageWorkoutDuration: Int // in minutes
)

data class ExerciseProgress(
    val exerciseId: String,
    val exerciseName: String,
    val maxWeight: Double,
    val maxReps: Int,
    val totalVolume: Double,
    val lastPerformed: LocalDate?
)

// Enums for muscle groups (can be extended)
enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    LEGS("Legs"),
    QUADS("Quadriceps"),
    HAMSTRINGS("Hamstrings"),
    GLUTES("Glutes"),
    CALVES("Calves"),
    CORE("Core"),
    FULL_BODY("Full Body")
}

// 7. Visual Progress Photos Table
data class ProgressPhoto(
    val id: String, // UUID
    val userId: String, // FK → users.id
    val imagePath: String, // Path to the stored image (for now just placeholder)
    val weight: Double?, // User's weight at the time of photo (optional)
    val notes: String? = null, // Optional notes about the photo
    val date: LocalDate, // Date when photo was taken
    val createdAt: LocalDateTime // When the record was created
)

// Helper data classes for Visual Progress
data class ProgressPhotoWithDetails(
    val photo: ProgressPhoto,
    val formattedDate: String,
    val formattedWeight: String
)

// Day of week enum for better type safety
enum class DayOfWeek(val value: Int, val displayName: String) {
    SUNDAY(0, "Sunday"),
    MONDAY(1, "Monday"),
    TUESDAY(2, "Tuesday"),
    WEDNESDAY(3, "Wednesday"),
    THURSDAY(4, "Thursday"),
    FRIDAY(5, "Friday"),
    SATURDAY(6, "Saturday");
    
    companion object {
        fun fromInt(value: Int): DayOfWeek {
            return entries.find { it.value == value } ?: SUNDAY
        }
    }
}

data class RecentWorkoutData(
    val id: String,
    val name: String,
    val date: String, // "Today", "Yesterday", etc.
    val progress: Int, // 0-100
    val exercises: Int,
    val totalSets: Int,
    val mainFocus: String,
    val tags: List<WorkoutTag> = emptyList(),
    val trophy: WorkoutTrophy? = null
)

data class WorkoutTag(
    val text: String,
    val type: TagType
)

enum class TagType {
    PUSH_UP, PULL_UP, CUSTOM
}

data class WorkoutTrophy(
    val type: TrophyType
)

enum class TrophyType {
    GOLD, SILVER, BRONZE
}
