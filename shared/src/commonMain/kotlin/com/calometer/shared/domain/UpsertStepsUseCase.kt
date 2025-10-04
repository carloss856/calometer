package com.calometer.shared.domain

import com.calometer.shared.core.StepSnapshot
import com.calometer.shared.data.StepsRepository

class UpsertStepsUseCase(private val repository: StepsRepository) {
    suspend operator fun invoke(snapshot: StepSnapshot) = repository.upsert(snapshot)
}
