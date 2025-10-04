package com.calometer.shared.features.workouts

import com.calometer.shared.core.Routine
import com.calometer.shared.core.WorkoutSession
import com.calometer.shared.domain.SaveWorkoutSessionUseCase
import com.calometer.shared.domain.UpsertRoutineUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutStore(
    private val upsertRoutine: UpsertRoutineUseCase,
    private val saveSession: SaveWorkoutSessionUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _routineDraft = MutableStateFlow<Routine?>(null)
    val routineDraft: StateFlow<Routine?> = _routineDraft

    fun setRoutineDraft(routine: Routine) {
        _routineDraft.value = routine
    }

    fun saveRoutine() {
        val routine = _routineDraft.value ?: return
        scope.launch { upsertRoutine(routine) }
    }

    fun logSession(session: WorkoutSession) {
        scope.launch { saveSession(session) }
    }
}
