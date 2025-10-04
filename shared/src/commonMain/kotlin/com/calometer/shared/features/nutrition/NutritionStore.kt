package com.calometer.shared.features.nutrition

import com.calometer.shared.ai.ParsedLine
import com.calometer.shared.core.Id
import com.calometer.shared.core.MealEntry
import com.calometer.shared.core.MealLine
import com.calometer.shared.core.Nutrients
import com.calometer.shared.domain.ParseMealFromTextUseCase
import com.calometer.shared.domain.SaveMealUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

sealed interface NutritionEvent {
    data class Parsed(val lines: List<ParsedLine>) : NutritionEvent
    data object Saved : NutritionEvent
    data class Error(val message: String) : NutritionEvent
}

data class NutritionState(
    val input: String = "",
    val parsedLines: List<ParsedLine> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class NutritionStore(
    private val parseMeal: ParseMealFromTextUseCase,
    private val saveMeal: SaveMealUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _state = MutableStateFlow(NutritionState())
    val state: StateFlow<NutritionState> = _state

    fun onInputChanged(text: String) {
        _state.value = _state.value.copy(input = text)
    }

    fun parse() {
        val text = _state.value.input
        if (text.isBlank()) return
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            runCatching { parseMeal(text) }
                .onSuccess { parsed ->
                    _state.value = _state.value.copy(parsedLines = parsed.lines, isLoading = false)
                }
                .onFailure {
                    _state.value = _state.value.copy(errorMessage = it.message, isLoading = false)
                }
        }
    }

    fun confirmAndSave(note: String? = null) {
        val lines = _state.value.parsedLines
        if (lines.isEmpty()) return
        val meal = MealEntry(
            id = Id("meal-${Clock.System.now().toEpochMilliseconds()}"),
            timestamp = Clock.System.now(),
            items = lines.filter { it.matchedFoodId != null && it.computed != null }.map { line ->
                MealLine(
                    foodId = line.matchedFoodId!!,
                    quantity = line.amount ?: 1.0,
                    unit = line.unit ?: "unidad",
                    computed = line.computed ?: Nutrients(0.0, 0.0, 0.0, 0.0),
                )
            },
            note = note,
        )
        scope.launch {
            saveMeal(meal)
            _state.value = NutritionState()
        }
    }
}
