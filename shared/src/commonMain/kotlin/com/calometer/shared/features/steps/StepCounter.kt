package com.calometer.shared.features.steps

import com.calometer.shared.core.StepSnapshot
import kotlinx.coroutines.flow.Flow

interface StepCounter {
    fun observeSteps(): Flow<StepSnapshot>
}
