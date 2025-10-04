package com.calometer.shared.core

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@JvmInline
value class Id(val raw: String) {
    init {
        require(raw.isNotBlank()) { "Id cannot be blank" }
    }

    override fun toString(): String = raw
}

data class Serving(val amount: Double, val unit: String)

data class Nutrients(
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
)

data class FoodItem(
    val id: Id,
    val name: String,
    val brand: String? = null,
    val defaultServing: Serving,
    val perServing: Nutrients,
)

data class MealLine(
    val foodId: Id,
    val quantity: Double,
    val unit: String,
    val computed: Nutrients,
)

data class MealEntry(
    val id: Id,
    val timestamp: Instant,
    val items: List<MealLine>,
    val note: String?,
)

enum class ExerciseType { STRENGTH, CARDIO, MOBILITY }

data class Exercise(
    val id: Id,
    val name: String,
    val type: ExerciseType,
    val defaultUnit: String?,
)

data class RoutineItem(
    val exerciseId: Id,
    val sets: Int,
    val reps: Int?,
    val durationSec: Int?,
    val restSec: Int,
)

data class RoutineBlock(val title: String?, val items: List<RoutineItem>)

data class Routine(val id: Id, val name: String, val blocks: List<RoutineBlock>)

data class WorkoutSet(
    val exerciseId: Id,
    val setIndex: Int,
    val reps: Int?,
    val durationSec: Int?,
    val weight: Double?,
    val rpe: Double?,
)

data class WorkoutSession(
    val id: Id,
    val routineId: Id?,
    val date: LocalDate,
    val entries: List<WorkoutSet>,
)

enum class StepSource { DEVICE, HEALTH_CONNECT, HEALTHKIT }

data class StepSnapshot(val date: LocalDate, val steps: Int, val source: StepSource)
