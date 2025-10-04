package com.calometer.shared.data

import com.calometer.shared.core.Exercise
import com.calometer.shared.core.Routine
import com.calometer.shared.core.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeRoutines(): Flow<List<Routine>>
    fun observeSessions(): Flow<List<WorkoutSession>>
    suspend fun upsertExercise(exercise: Exercise)
    suspend fun upsertRoutine(routine: Routine)
    suspend fun deleteRoutine(id: String)
    suspend fun saveSession(session: WorkoutSession)
}
