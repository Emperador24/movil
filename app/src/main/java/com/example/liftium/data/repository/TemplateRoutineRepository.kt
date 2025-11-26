package com.example.liftium.data.repository

import com.example.liftium.model.*
import java.util.UUID

/**
 * Repository for standard/template workout routines that users can select from.
 * These are pre-defined routines that get copied to user's account when selected.
 */
object TemplateRoutineRepository {
    
    data class TemplateRoutine(
        val name: String,
        val description: String,
        val splitDays: List<TemplateSplitDay>
    )
    
    data class TemplateSplitDay(
        val name: String,
        val dayOfWeek: Int? = null, // null means user will assign
        val isRestDay: Boolean = false,
        val exercises: List<TemplateExercise>
    )
    
    data class TemplateExercise(
        val name: String,
        val defaultSets: Int,
        val restTimeSec: Int,
        val note: String?,
        val muscleGroups: String
    )
    
    /**
     * Get all available template routines
     */
    fun getTemplateRoutines(): List<TemplateRoutine> {
        return listOf(
            // Push/Pull/Legs Split
            TemplateRoutine(
                name = "Push/Pull/Legs",
                description = "Classic 3-day split focusing on push movements, pull movements, and legs",
                splitDays = listOf(
                    TemplateSplitDay(
                        name = "Push",
                        isRestDay = false,
                        exercises = listOf(
                            TemplateExercise(
                                name = "Bench Press",
                                defaultSets = 4,
                                restTimeSec = 180,
                                note = "Keep shoulders retracted and feet planted",
                                muscleGroups = MuscleGroup.CHEST.displayName
                            ),
                            TemplateExercise(
                                name = "Overhead Press",
                                defaultSets = 3,
                                restTimeSec = 120,
                                note = "Press straight up, maintain tight core",
                                muscleGroups = MuscleGroup.SHOULDERS.displayName
                            ),
                            TemplateExercise(
                                name = "Incline Dumbbell Press",
                                defaultSets = 3,
                                restTimeSec = 90,
                                note = "Focus on upper chest",
                                muscleGroups = MuscleGroup.CHEST.displayName
                            ),
                            TemplateExercise(
                                name = "Tricep Dips",
                                defaultSets = 3,
                                restTimeSec = 90,
                                note = "Lean forward slightly for chest emphasis",
                                muscleGroups = MuscleGroup.TRICEPS.displayName
                            ),
                            TemplateExercise(
                                name = "Lateral Raises",
                                defaultSets = 3,
                                restTimeSec = 60,
                                note = "Control the movement, slight bend in elbows",
                                muscleGroups = MuscleGroup.SHOULDERS.displayName
                            )
                        )
                    ),
                    TemplateSplitDay(
                        name = "Pull",
                        isRestDay = false,
                        exercises = listOf(
                            TemplateExercise(
                                name = "Pull-ups",
                                defaultSets = 4,
                                restTimeSec = 120,
                                note = "Full range of motion, control the negative",
                                muscleGroups = MuscleGroup.BACK.displayName
                            ),
                            TemplateExercise(
                                name = "Barbell Rows",
                                defaultSets = 4,
                                restTimeSec = 120,
                                note = "Pull to lower chest, squeeze shoulder blades",
                                muscleGroups = MuscleGroup.BACK.displayName
                            ),
                            TemplateExercise(
                                name = "Face Pulls",
                                defaultSets = 3,
                                restTimeSec = 60,
                                note = "Pull to face level, external rotation",
                                muscleGroups = "${MuscleGroup.SHOULDERS.displayName}, ${MuscleGroup.BACK.displayName}"
                            ),
                            TemplateExercise(
                                name = "Bicep Curls",
                                defaultSets = 3,
                                restTimeSec = 60,
                                note = "Keep elbows stationary, full range of motion",
                                muscleGroups = MuscleGroup.BICEPS.displayName
                            ),
                            TemplateExercise(
                                name = "Hammer Curls",
                                defaultSets = 3,
                                restTimeSec = 60,
                                note = "Neutral grip, targets brachialis",
                                muscleGroups = MuscleGroup.BICEPS.displayName
                            )
                        )
                    ),
                    TemplateSplitDay(
                        name = "Legs",
                        isRestDay = false,
                        exercises = listOf(
                            TemplateExercise(
                                name = "Squats",
                                defaultSets = 4,
                                restTimeSec = 180,
                                note = "Go to parallel or below, drive through heels",
                                muscleGroups = MuscleGroup.LEGS.displayName
                            ),
                            TemplateExercise(
                                name = "Romanian Deadlifts",
                                defaultSets = 4,
                                restTimeSec = 120,
                                note = "Keep bar close to body, neutral spine",
                                muscleGroups = "${MuscleGroup.HAMSTRINGS.displayName}, ${MuscleGroup.GLUTES.displayName}"
                            ),
                            TemplateExercise(
                                name = "Leg Press",
                                defaultSets = 3,
                                restTimeSec = 90,
                                note = "Full range of motion, don't lock knees",
                                muscleGroups = MuscleGroup.LEGS.displayName
                            ),
                            TemplateExercise(
                                name = "Leg Curls",
                                defaultSets = 3,
                                restTimeSec = 60,
                                note = "Control the negative, squeeze at top",
                                muscleGroups = MuscleGroup.HAMSTRINGS.displayName
                            ),
                            TemplateExercise(
                                name = "Calf Raises",
                                defaultSets = 4,
                                restTimeSec = 45,
                                note = "Full stretch at bottom, squeeze at top",
                                muscleGroups = MuscleGroup.CALVES.displayName
                            )
                        )
                    )
                )
            ),
            
            // Upper/Lower Split
            TemplateRoutine(
                name = "Upper/Lower",
                description = "4-day split alternating between upper and lower body workouts",
                splitDays = listOf(
                    TemplateSplitDay(
                        name = "Upper Body",
                        isRestDay = false,
                        exercises = listOf(
                            TemplateExercise(
                                name = "Bench Press",
                                defaultSets = 4,
                                restTimeSec = 180,
                                note = "Compound movement for chest",
                                muscleGroups = MuscleGroup.CHEST.displayName
                            ),
                            TemplateExercise(
                                name = "Barbell Rows",
                                defaultSets = 4,
                                restTimeSec = 120,
                                note = "Compound movement for back",
                                muscleGroups = MuscleGroup.BACK.displayName
                            ),
                            TemplateExercise(
                                name = "Overhead Press",
                                defaultSets = 3,
                                restTimeSec = 120,
                                note = "Shoulders and triceps",
                                muscleGroups = MuscleGroup.SHOULDERS.displayName
                            ),
                            TemplateExercise(
                                name = "Lat Pulldowns",
                                defaultSets = 3,
                                restTimeSec = 90,
                                note = "Back width",
                                muscleGroups = MuscleGroup.BACK.displayName
                            ),
                            TemplateExercise(
                                name = "Dumbbell Curls",
                                defaultSets = 3,
                                restTimeSec = 60,
                                note = "Biceps isolation",
                                muscleGroups = MuscleGroup.BICEPS.displayName
                            ),
                            TemplateExercise(
                                name = "Tricep Pushdowns",
                                defaultSets = 3,
                                restTimeSec = 60,
                                note = "Triceps isolation",
                                muscleGroups = MuscleGroup.TRICEPS.displayName
                            )
                        )
                    ),
                    TemplateSplitDay(
                        name = "Lower Body",
                        isRestDay = false,
                        exercises = listOf(
                            TemplateExercise(
                                name = "Squats",
                                defaultSets = 4,
                                restTimeSec = 180,
                                note = "King of leg exercises",
                                muscleGroups = MuscleGroup.LEGS.displayName
                            ),
                            TemplateExercise(
                                name = "Deadlifts",
                                defaultSets = 3,
                                restTimeSec = 180,
                                note = "Full body compound",
                                muscleGroups = "${MuscleGroup.HAMSTRINGS.displayName}, ${MuscleGroup.GLUTES.displayName}, ${MuscleGroup.BACK.displayName}"
                            ),
                            TemplateExercise(
                                name = "Lunges",
                                defaultSets = 3,
                                restTimeSec = 90,
                                note = "Unilateral leg work",
                                muscleGroups = MuscleGroup.LEGS.displayName
                            ),
                            TemplateExercise(
                                name = "Leg Curls",
                                defaultSets = 3,
                                restTimeSec = 60,
                                note = "Hamstring isolation",
                                muscleGroups = MuscleGroup.HAMSTRINGS.displayName
                            ),
                            TemplateExercise(
                                name = "Calf Raises",
                                defaultSets = 4,
                                restTimeSec = 45,
                                note = "Calf development",
                                muscleGroups = MuscleGroup.CALVES.displayName
                            )
                        )
                    )
                )
            ),
            
            // Full Body
            TemplateRoutine(
                name = "Full Body",
                description = "3-day per week full body routine for beginners or time-efficient training",
                splitDays = listOf(
                    TemplateSplitDay(
                        name = "Full Body",
                        isRestDay = false,
                        exercises = listOf(
                            TemplateExercise(
                                name = "Squats",
                                defaultSets = 3,
                                restTimeSec = 180,
                                note = "Lower body compound",
                                muscleGroups = MuscleGroup.LEGS.displayName
                            ),
                            TemplateExercise(
                                name = "Bench Press",
                                defaultSets = 3,
                                restTimeSec = 120,
                                note = "Upper body push",
                                muscleGroups = MuscleGroup.CHEST.displayName
                            ),
                            TemplateExercise(
                                name = "Barbell Rows",
                                defaultSets = 3,
                                restTimeSec = 120,
                                note = "Upper body pull",
                                muscleGroups = MuscleGroup.BACK.displayName
                            ),
                            TemplateExercise(
                                name = "Overhead Press",
                                defaultSets = 3,
                                restTimeSec = 90,
                                note = "Shoulders",
                                muscleGroups = MuscleGroup.SHOULDERS.displayName
                            ),
                            TemplateExercise(
                                name = "Romanian Deadlifts",
                                defaultSets = 3,
                                restTimeSec = 120,
                                note = "Posterior chain",
                                muscleGroups = "${MuscleGroup.HAMSTRINGS.displayName}, ${MuscleGroup.GLUTES.displayName}"
                            ),
                            TemplateExercise(
                                name = "Planks",
                                defaultSets = 3,
                                restTimeSec = 60,
                                note = "Core stability",
                                muscleGroups = MuscleGroup.CORE.displayName
                            )
                        )
                    )
                )
            )
        )
    }
    
    /**
     * Get available routine names for selection
     */
    fun getAvailableRoutineNames(): List<String> {
        return listOf("Push", "Pull", "Legs", "Upper Body", "Lower Body", "Full Body", "Core", "Cardio")
    }
    
    /**
     * Copy a template routine to a user's account
     */
    suspend fun copyTemplateToUser(
        templateName: String,
        splitName: String,
        dayAssignments: Map<Int, String>, // dayOfWeek to routine name
        splitRepository: SplitRepository
    ): Result<SplitWithDays> {
        return try {
            // Find the template
            val template = getTemplateRoutines().find { it.name == templateName }
                ?: return Result.failure(Exception("Template not found: $templateName"))
            
            // Create the split
            val splitResult = splitRepository.createSplit(splitName)
            val split = splitResult.getOrElse { 
                return Result.failure(it)
            }
            
            val createdSplitDays = mutableListOf<SplitDayWithExercises>()
            
            // Create split days based on user assignments
            for ((dayOfWeek, routineName) in dayAssignments) {
                val templateDay = template.splitDays.find { it.name == routineName }
                
                if (templateDay != null && !templateDay.isRestDay) {
                    val splitDayId = UUID.randomUUID().toString()
                    val splitDay = SplitDay(
                        id = splitDayId,
                        splitId = split.id,
                        dayOfWeek = dayOfWeek,
                        name = templateDay.name,
                        isRestDay = false
                    )
                    
                    splitRepository.createSplitDay(splitDay).getOrElse {
                        return Result.failure(it)
                    }
                    
                    // Create exercises for this day
                    val createdExercises = mutableListOf<Exercise>()
                    templateDay.exercises.forEachIndexed { index, templateExercise ->
                        val exercise = Exercise(
                            id = UUID.randomUUID().toString(),
                            splitDayId = splitDayId,
                            name = templateExercise.name,
                            defaultSets = templateExercise.defaultSets,
                            restTimeSec = templateExercise.restTimeSec,
                            note = templateExercise.note,
                            exerciseOrder = index + 1,
                            muscleGroups = templateExercise.muscleGroups
                        )
                        
                        splitRepository.createExercise(exercise).getOrElse {
                            return Result.failure(it)
                        }
                        
                        createdExercises.add(exercise)
                    }
                    
                    createdSplitDays.add(SplitDayWithExercises(splitDay, createdExercises))
                } else {
                    // Create a rest day
                    val splitDay = SplitDay(
                        id = UUID.randomUUID().toString(),
                        splitId = split.id,
                        dayOfWeek = dayOfWeek,
                        name = routineName,
                        isRestDay = true
                    )
                    
                    splitRepository.createSplitDay(splitDay).getOrElse {
                        return Result.failure(it)
                    }
                    
                    createdSplitDays.add(SplitDayWithExercises(splitDay, emptyList()))
                }
            }
            
            Result.success(SplitWithDays(split, createdSplitDays))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

