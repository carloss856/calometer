package com.calometer.shared.domain

import com.calometer.shared.core.Routine
import com.calometer.shared.data.WorkoutRepository

class UpsertRoutineUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke(routine: Routine) = repository.upsertRoutine(routine)
}
