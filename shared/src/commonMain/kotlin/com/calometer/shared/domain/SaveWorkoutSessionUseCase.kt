package com.calometer.shared.domain

import com.calometer.shared.core.WorkoutSession
import com.calometer.shared.data.WorkoutRepository

class SaveWorkoutSessionUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke(session: WorkoutSession) = repository.saveSession(session)
}
