package com.calometer.shared.features.steps

import com.calometer.shared.core.StepSnapshot
import com.calometer.shared.core.StepSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class IosStepCounter : StepCounter {
    override fun observeSteps(): Flow<StepSnapshot> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return flowOf(StepSnapshot(date = now, steps = 0, source = StepSource.HEALTHKIT))
    }
}
