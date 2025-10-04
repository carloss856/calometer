package com.calometer.shared.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.calometer.shared.core.Exercise
import com.calometer.shared.core.Id
import com.calometer.shared.core.Routine
import com.calometer.shared.core.RoutineBlock
import com.calometer.shared.core.RoutineItem
import com.calometer.shared.core.WorkoutSession
import com.calometer.shared.core.WorkoutSet
import com.calometer.shared.database.CalometerDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDate.Companion.fromEpochDays
import kotlinx.datetime.toEpochDays

class SqlDelightWorkoutRepository(
    private val database: CalometerDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : WorkoutRepository {

    override fun observeRoutines(): Flow<List<Routine>> = database.routinesQueries
        .selectRoutines()
        .asFlow()
        .mapToList(dispatcher)
        .map { routineRows ->
            routineRows.map { routineRow ->
                val blocks = database.routine_blocksQueries.selectRoutineBlocks(routineRow.id).executeAsList().map { block ->
                    val items = database.routine_itemsQueries.selectRoutineItems(block.id).executeAsList().map { item ->
                        RoutineItem(
                            exerciseId = Id(item.exercise_id),
                            sets = item.sets,
                            reps = item.reps,
                            durationSec = item.duration_sec,
                            restSec = item.rest_sec,
                        )
                    }
                    RoutineBlock(title = block.title, items = items)
                }
                Routine(
                    id = Id(routineRow.id),
                    name = routineRow.name,
                    blocks = blocks,
                )
            }
        }

    override fun observeSessions(): Flow<List<WorkoutSession>> = database.workout_sessionsQueries
        .selectWorkoutSessions()
        .asFlow()
        .mapToList(dispatcher)
        .map { sessions ->
            sessions.map { session ->
                val sets = database.workout_setsQueries.selectWorkoutSets(session.id).executeAsList().map { setRow ->
                    WorkoutSet(
                        exerciseId = Id(setRow.exercise_id),
                        setIndex = setRow.set_index,
                        reps = setRow.reps,
                        durationSec = setRow.duration_sec,
                        weight = setRow.weight,
                        rpe = setRow.rpe,
                    )
                }
                WorkoutSession(
                    id = Id(session.id),
                    routineId = session.routine_id?.let(::Id),
                    date = LocalDate.fromEpochDays(session.date.toInt()),
                    entries = sets,
                )
            }
        }

    override suspend fun upsertExercise(exercise: Exercise) = withContext(dispatcher) {
        database.exercisesQueries.insertExercise(
            id = exercise.id.raw,
            name = exercise.name,
            type = exercise.type.name,
            default_unit = exercise.defaultUnit,
        )
    }

    override suspend fun upsertRoutine(routine: Routine) = withContext(dispatcher) {
        database.transaction {
            database.routinesQueries.insertRoutine(routine.id.raw, routine.name)
            database.routine_itemsQueries.deleteRoutineItemsByRoutine(routine.id.raw)
            database.routine_blocksQueries.deleteRoutineBlocks(routine.id.raw)
            routine.blocks.forEach { block ->
                database.routine_blocksQueries.insertRoutineBlock(routine.id.raw, block.title)
                val blockId = database.routine_blocksQueries.lastInsertedId().executeAsOne()
                block.items.forEach { item ->
                    database.routine_itemsQueries.insertRoutineItem(
                        block_id = blockId,
                        exercise_id = item.exerciseId.raw,
                        sets = item.sets,
                        reps = item.reps,
                        duration_sec = item.durationSec,
                        rest_sec = item.restSec,
                    )
                }
            }
        }
    }

    override suspend fun deleteRoutine(id: String) = withContext(dispatcher) {
        database.routinesQueries.deleteRoutine(id)
    }

    override suspend fun saveSession(session: WorkoutSession) = withContext(dispatcher) {
        database.transaction {
            database.workout_sessionsQueries.insertWorkoutSession(
                id = session.id.raw,
                routine_id = session.routineId?.raw,
                date = session.date.toEpochDays().toLong(),
            )
            database.workout_setsQueries.deleteWorkoutSets(session.id.raw)
            session.entries.forEach { set ->
                database.workout_setsQueries.insertWorkoutSet(
                    session_id = session.id.raw,
                    exercise_id = set.exerciseId.raw,
                    set_index = set.setIndex,
                    reps = set.reps,
                    duration_sec = set.durationSec,
                    weight = set.weight,
                    rpe = set.rpe,
                )
            }
        }
    }
}
