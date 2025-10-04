package com.calometer.shared.domain

import com.calometer.shared.core.StepSnapshot
import com.calometer.shared.data.StepsRepository
import kotlinx.datetime.LocalDate

class GetDailyStepsUseCase(private val repository: StepsRepository) {
    suspend operator fun invoke(date: LocalDate): StepSnapshot? = repository.getSteps(date)
}
