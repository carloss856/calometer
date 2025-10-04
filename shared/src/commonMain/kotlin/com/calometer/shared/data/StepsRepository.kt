package com.calometer.shared.data

import com.calometer.shared.core.StepSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface StepsRepository {
    fun observeSteps(range: ClosedRange<LocalDate>): Flow<List<StepSnapshot>>
    suspend fun getSteps(date: LocalDate): StepSnapshot?
    suspend fun upsert(snapshot: StepSnapshot)
}
