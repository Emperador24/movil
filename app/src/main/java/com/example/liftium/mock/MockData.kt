package com.example.liftium.mock

import com.example.liftium.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

object MockData {
    
    // 1. Current User
    val currentUser = User(
        id = "550e8400-e29b-41d4-a716-446655440000", // UUID format
        email = "isaac@example.com",
        createdAt = LocalDateTime.of(2024, 1, 15, 10, 30),
        userName = "Isaac"
    )
    
    // 2. User's Split
    val pushPullLegsSplit = Split(
        id = "550e8400-e29b-41d4-a716-446655440001",
        userId = currentUser.id,
        name = "Push/Pull/Legs",
        createdAt = LocalDateTime.of(2024, 1, 16, 9, 0)
    )
    
    // 3. Split Days
    val pushDay = SplitDay(
        id = "550e8400-e29b-41d4-a716-446655440002",
        splitId = pushPullLegsSplit.id,
        dayOfWeek = 1, // Monday
        name = "Push",
        isRestDay = false
    )
    
    val pullDay = SplitDay(
        id = "550e8400-e29b-41d4-a716-446655440003",
        splitId = pushPullLegsSplit.id,
        dayOfWeek = 2, // Tuesday
        name = "Pull",
        isRestDay = false
    )
    
    val legDay = SplitDay(
        id = "550e8400-e29b-41d4-a716-446655440004",
        splitId = pushPullLegsSplit.id,
        dayOfWeek = 3, // Wednesday
        name = "Legs",
        isRestDay = false
    )
    
    val restDay = SplitDay(
        id = "550e8400-e29b-41d4-a716-446655440005",
        splitId = pushPullLegsSplit.id,
        dayOfWeek = 4, // Thursday
        name = "Rest",
        isRestDay = true
    )
    
    // 4. Exercises for Push Day
    val benchPress = Exercise(
        id = "550e8400-e29b-41d4-a716-446655440010",
        splitDayId = pushDay.id,
        name = "Bench Press",
        defaultSets = 3,
        restTimeSec = 180, // 3 minutes
        note = "Keep shoulders retracted and feet planted",
        exerciseOrder = 1,
        muscleGroups = MuscleGroup.CHEST.displayName
    )
    
    val overheadPress = Exercise(
        id = "550e8400-e29b-41d4-a716-446655440011",
        splitDayId = pushDay.id,
        name = "Overhead Press",
        defaultSets = 3,
        restTimeSec = 120, // 2 minutes
        note = null,
        exerciseOrder = 2,
        muscleGroups = MuscleGroup.SHOULDERS.displayName
    )
    
    val tricepDips = Exercise(
        id = "550e8400-e29b-41d4-a716-446655440012",
        splitDayId = pushDay.id,
        name = "Tricep Dips",
        defaultSets = 3,
        restTimeSec = 90,
        note = "Lean forward slightly for chest emphasis",
        exerciseOrder = 3,
        muscleGroups = MuscleGroup.TRICEPS.displayName
    )
    
    // 5. Exercises for Pull Day
    val pullUps = Exercise(
        id = "550e8400-e29b-41d4-a716-446655440020",
        splitDayId = pullDay.id,
        name = "Pull-ups",
        defaultSets = 3,
        restTimeSec = 120,
        note = "Full range of motion, control the negative",
        exerciseOrder = 1,
        muscleGroups = MuscleGroup.BACK.displayName
    )
    
    val barbellRows = Exercise(
        id = "550e8400-e29b-41d4-a716-446655440021",
        splitDayId = pullDay.id,
        name = "Barbell Rows",
        defaultSets = 3,
        restTimeSec = 30,
        note = "Pull to lower chest, squeeze shoulder blades",
        exerciseOrder = 2,
        muscleGroups = MuscleGroup.BACK.displayName
    )
    
    val bicepCurls = Exercise(
        id = "550e8400-e29b-41d4-a716-446655440022",
        splitDayId = pullDay.id,
        name = "Bicep Curls",
        defaultSets = 3,
        restTimeSec = 20,
        note = null,
        exerciseOrder = 3,
        muscleGroups = MuscleGroup.BICEPS.displayName
    )
    
    // 6. Exercises for Leg Day
    val squats = Exercise(
        id = "550e8400-e29b-41d4-a716-446655440030",
        splitDayId = legDay.id,
        name = "Squats",
        defaultSets = 3,
        restTimeSec = 180,
        note = "Go to parallel or below, drive through heels",
        exerciseOrder = 1,
        muscleGroups = MuscleGroup.LEGS.displayName
    )
    
    val deadlifts = Exercise(
        id = "550e8400-e29b-41d4-a716-446655440031",
        splitDayId = legDay.id,
        name = "Deadlifts",
        defaultSets = 3,
        restTimeSec = 180,
        note = "Keep bar close to body, neutral spine",
        exerciseOrder = 2,
        muscleGroups = "${MuscleGroup.HAMSTRINGS.displayName}, ${MuscleGroup.GLUTES.displayName}"
    )
    
    // 7. Recent Sessions
    val recentPushSession = Session(
        id = "550e8400-e29b-41d4-a716-446655440099",
        userId = currentUser.id,
        splitDayId = pushDay.id,
        date = LocalDate.now().minusDays(1),
        createdAt = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0)
    )
    
    val recentPullSession = Session(
        id = "550e8400-e29b-41d4-a716-446655440100",
        userId = currentUser.id,
        splitDayId = pullDay.id,
        date = LocalDate.now().minusDays(2),
        createdAt = LocalDateTime.now().minusDays(2).withHour(14).withMinute(30)
    )
    
    val recentLegSession = Session(
        id = "550e8400-e29b-41d4-a716-446655440101",
        userId = currentUser.id,
        splitDayId = legDay.id,
        date = LocalDate.now().minusDays(4),
        createdAt = LocalDateTime.now().minusDays(4).withHour(16).withMinute(0)
    )
    
    // 8. Workout Sets for Recent Sessions
    val pushSessionSets = listOf(
        // Bench Press sets
        WorkoutSet("550e8400-e29b-41d4-a716-446655440190", recentPushSession.id, benchPress.id, 1, 8, 185.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440191", recentPushSession.id, benchPress.id, 2, 8, 185.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440192", recentPushSession.id, benchPress.id, 3, 6, 195.0),
        
        // Overhead Press sets
        WorkoutSet("550e8400-e29b-41d4-a716-446655440193", recentPushSession.id, overheadPress.id, 1, 10, 95.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440194", recentPushSession.id, overheadPress.id, 2, 8, 95.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440195", recentPushSession.id, overheadPress.id, 3, 6, 105.0),
        
        // Tricep Dips sets (bodyweight - 0 weight)
        WorkoutSet("550e8400-e29b-41d4-a716-446655440196", recentPushSession.id, tricepDips.id, 1, 12, 0.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440197", recentPushSession.id, tricepDips.id, 2, 10, 0.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440198", recentPushSession.id, tricepDips.id, 3, 8, 0.0)
    )
    
    val pullSessionSets = listOf(
        // Pull-ups sets
        WorkoutSet("550e8400-e29b-41d4-a716-446655440200", recentPullSession.id, pullUps.id, 1, 12, 0.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440201", recentPullSession.id, pullUps.id, 2, 10, 0.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440202", recentPullSession.id, pullUps.id, 3, 8, 0.0),
        
        // Barbell Rows sets
        WorkoutSet("550e8400-e29b-41d4-a716-446655440203", recentPullSession.id, barbellRows.id, 1, 8, 115.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440204", recentPullSession.id, barbellRows.id, 2, 8, 115.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440205", recentPullSession.id, barbellRows.id, 3, 6, 125.0),
        
        // Bicep Curls sets
        WorkoutSet("550e8400-e29b-41d4-a716-446655440206", recentPullSession.id, bicepCurls.id, 1, 12, 25.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440207", recentPullSession.id, bicepCurls.id, 2, 10, 25.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440208", recentPullSession.id, bicepCurls.id, 3, 8, 30.0)
    )
    
    val legSessionSets = listOf(
        // Squats sets
        WorkoutSet("550e8400-e29b-41d4-a716-446655440210", recentLegSession.id, squats.id, 1, 15, 185.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440211", recentLegSession.id, squats.id, 2, 12, 185.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440212", recentLegSession.id, squats.id, 3, 10, 195.0),
        
        // Deadlifts sets
        WorkoutSet("550e8400-e29b-41d4-a716-446655440213", recentLegSession.id, deadlifts.id, 1, 8, 225.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440214", recentLegSession.id, deadlifts.id, 2, 8, 225.0),
        WorkoutSet("550e8400-e29b-41d4-a716-446655440215", recentLegSession.id, deadlifts.id, 3, 6, 245.0)
    )
    
    // Helper data structures
    val allSplitDays = listOf(pushDay, pullDay, legDay, restDay)
    val allExercises = listOf(benchPress, overheadPress, tricepDips, pullUps, barbellRows, bicepCurls, squats, deadlifts)
    val allSessions = listOf(recentPushSession, recentPullSession, recentLegSession)
    val allSets = pushSessionSets + pullSessionSets + legSessionSets
    
    // Composite data for UI
    val splitWithDays = SplitWithDays(
        split = pushPullLegsSplit,
        splitDays = listOf(
            SplitDayWithExercises(pushDay, listOf(benchPress, overheadPress, tricepDips)),
            SplitDayWithExercises(pullDay, listOf(pullUps, barbellRows, bicepCurls)),
            SplitDayWithExercises(legDay, listOf(squats, deadlifts)),
            SplitDayWithExercises(restDay, emptyList())
        )
    )
    
    val recentSessionsWithDetails = listOf(
        SessionWithDetails(
            session = recentPullSession,
            splitDay = pullDay,
            split = pushPullLegsSplit,
            exercises = listOf(
                ExerciseWithSets(pullUps, pullSessionSets.filter { it.exerciseId == pullUps.id }),
                ExerciseWithSets(barbellRows, pullSessionSets.filter { it.exerciseId == barbellRows.id }),
                ExerciseWithSets(bicepCurls, pullSessionSets.filter { it.exerciseId == bicepCurls.id })
            )
        ),
        SessionWithDetails(
            session = recentLegSession,
            splitDay = legDay,
            split = pushPullLegsSplit,
            exercises = listOf(
                ExerciseWithSets(squats, legSessionSets.filter { it.exerciseId == squats.id }),
                ExerciseWithSets(deadlifts, legSessionSets.filter { it.exerciseId == deadlifts.id })
            )
        )
    )
    
    val progressStats = ProgressStats(
        totalWorkouts = 42,
        currentStreak = 7,
        longestStreak = 12,
        totalVolume = 15420.5,
        favoriteExercise = "Bench Press",
        averageWorkoutDuration = 65
    )
    
    val motivationalMessages = listOf(
        "Today is a great day for a workout!",
        "Your only limit is your mind!",
        "Strong is the new beautiful!",
        "Every workout counts!",
        "Push your limits today!",
        "Consistency is key!",
        "You're stronger than yesterday!"
    )
    
    // 9. Visual Progress Photos
    val progressPhotos = listOf(
        ProgressPhoto(
            id = "550e8400-e29b-41d4-a716-446655440300",
            userId = currentUser.id,
            imagePath = "placeholder_photo_1", // Placeholder for actual image path
            weight = 185.5,
            notes = "Starting my fitness journey!",
            date = LocalDate.now().minusDays(90),
            createdAt = LocalDateTime.now().minusDays(90).withHour(8).withMinute(0)
        ),
        ProgressPhoto(
            id = "550e8400-e29b-41d4-a716-446655440301",
            userId = currentUser.id,
            imagePath = "placeholder_photo_2",
            weight = 182.0,
            notes = "One month in - feeling stronger",
            date = LocalDate.now().minusDays(60),
            createdAt = LocalDateTime.now().minusDays(60).withHour(7).withMinute(30)
        ),
        ProgressPhoto(
            id = "550e8400-e29b-41d4-a716-446655440302",
            userId = currentUser.id,
            imagePath = "placeholder_photo_3",
            weight = 178.5,
            notes = "Two months - seeing definition!",
            date = LocalDate.now().minusDays(30),
            createdAt = LocalDateTime.now().minusDays(30).withHour(9).withMinute(15)
        ),
        ProgressPhoto(
            id = "550e8400-e29b-41d4-a716-446655440303",
            userId = currentUser.id,
            imagePath = "placeholder_photo_4",
            weight = 176.0,
            notes = "Three months - best shape yet!",
            date = LocalDate.now().minusDays(7),
            createdAt = LocalDateTime.now().minusDays(7).withHour(8).withMinute(45)
        ),
        ProgressPhoto(
            id = "550e8400-e29b-41d4-a716-446655440304",
            userId = currentUser.id,
            imagePath = "placeholder_photo_5",
            weight = null, // No weight recorded
            notes = "Post-workout pump check",
            date = LocalDate.now().minusDays(2),
            createdAt = LocalDateTime.now().minusDays(2).withHour(18).withMinute(30)
        ),
        ProgressPhoto(
            id = "550e8400-e29b-41d4-a716-446655440305",
            userId = currentUser.id,
            imagePath = "placeholder_photo_6",
            weight = 175.5,
            notes = "Current progress - feeling amazing!",
            date = LocalDate.now(),
            createdAt = LocalDateTime.now().withHour(7).withMinute(0)
        )
    )
    
    // Helper functions
    fun getRandomMotivationalMessage(): String {
        return motivationalMessages.random()
    }
    
    fun getTodaysSplitDay(): SplitDay {
        // For demo purposes, cycle through the workout days (skip rest day)
        val today = LocalDate.now().dayOfWeek.value // 1-7 (Monday=1, Sunday=7)
        val workoutDays = allSplitDays.filter { !it.isRestDay } // pushDay, pullDay, legDay
        val dayIndex = (today - 1) % workoutDays.size // Cycle through 0, 1, 2
        return workoutDays[dayIndex]
    }
    
    fun hasWorkoutToday(): Boolean {
        val todaysSplitDay = getTodaysSplitDay()
        return !todaysSplitDay.isRestDay
    }
    
    fun getTodaysWorkoutStatus(): String {
        return if (hasWorkoutToday()) "Available" else "Rest Day"
    }
    
    fun getTodaysWorkoutName(): String {
        val todaysSplitDay = getTodaysSplitDay()
        return todaysSplitDay.name
    }
    
    fun hasRecentWorkouts(): Boolean {
        return allSessions.isNotEmpty()
    }
    
    fun getExerciseProgress(exerciseId: String): ExerciseProgress? {
        val exercise = allExercises.find { it.id == exerciseId }
        val exerciseSets = allSets.filter { it.exerciseId == exerciseId }
        
        return if (exercise != null && exerciseSets.isNotEmpty()) {
            val maxWeight = exerciseSets.maxOfOrNull { it.weight } ?: 0.0
            val maxReps = exerciseSets.maxOfOrNull { it.reps } ?: 0
            val totalVolume = exerciseSets.sumOf { it.weight * it.reps }
            val lastSession = allSessions.filter { session ->
                exerciseSets.any { it.sessionId == session.id }
            }.maxByOrNull { it.date }
            
            ExerciseProgress(
                exerciseId = exerciseId,
                exerciseName = exercise.name,
                maxWeight = maxWeight,
                maxReps = maxReps,
                totalVolume = totalVolume,
                lastPerformed = lastSession?.date
            )
        } else null
    }
    
    fun getProgressPhotosWithDetails(): List<ProgressPhotoWithDetails> {
        return progressPhotos.sortedByDescending { it.date }.map { photo ->
            ProgressPhotoWithDetails(
                photo = photo,
                formattedDate = formatDate(photo.date),
                formattedWeight = photo.weight?.let { "${it.toInt()} lbs" } ?: "No weight"
            )
        }
    }
    
    fun getLatestProgressPhoto(): ProgressPhoto? {
        return progressPhotos.maxByOrNull { it.date }
    }
    
    fun getTotalProgressPhotos(): Int {
        return progressPhotos.size
    }
    
    private fun formatDate(date: LocalDate): String {
        val now = LocalDate.now()
        val daysDiff = now.toEpochDay() - date.toEpochDay()
        
        return when {
            daysDiff == 0L -> "Today"
            daysDiff == 1L -> "Yesterday"
            daysDiff < 7 -> "${daysDiff.toInt()} days ago"
            daysDiff < 30 -> "${(daysDiff / 7).toInt()} weeks ago"
            daysDiff < 365 -> "${(daysDiff / 30).toInt()} months ago"
            else -> "${(daysDiff / 365).toInt()} years ago"
        }
    }

    private val _recentWorkoutsData = listOf(
        RecentWorkoutData(
            id = "recent_1",
            name = "Pull",
            date = "Today",
            progress = 89,
            exercises = 2,
            totalSets = 4,
            mainFocus = "Biceps",
            tags = listOf(
                WorkoutTag("w", TagType.CUSTOM),
                WorkoutTag("z", TagType.CUSTOM)
            ),
            trophy = WorkoutTrophy(TrophyType.GOLD)
        ),
        RecentWorkoutData(
            id = "recent_2",
            name = "Legs",
            date = "Today",
            progress = 78,
            exercises = 3,
            totalSets = 6,
            mainFocus = "Cuadriceps",
            tags = listOf(
                WorkoutTag("Push up", TagType.PUSH_UP),
                WorkoutTag("Pull ups", TagType.PULL_UP),
                WorkoutTag("aaa", TagType.CUSTOM)
            ),
            trophy = null
        )
    )

    fun getRecentWorkoutsData(): List<RecentWorkoutData> {
        return _recentWorkoutsData
    }
}
