package com.calometer.shared.features.steps

import com.calometer.shared.core.StepSnapshot
import com.calometer.shared.domain.GetDailyStepsUseCase
import com.calometer.shared.domain.UpsertStepsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class StepsStore(
    private val getDailySteps: GetDailyStepsUseCase,
    private val upsertSteps: UpsertStepsUseCase,
    private val stepCounter: StepCounter,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _today = MutableStateFlow<StepSnapshot?>(null)
    val today: StateFlow<StepSnapshot?> = _today

    init {
        scope.launch {
            stepCounter.observeSteps().collect { snapshot ->
                upsertSteps(snapshot)
                if (snapshot.date == currentDate()) {
                    _today.value = snapshot
                }
            }
        }
    }

    fun refreshToday() {
        val date = currentDate()
        scope.launch {
            _today.value = getDailySteps(date)
        }
    }

    private fun currentDate() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}
