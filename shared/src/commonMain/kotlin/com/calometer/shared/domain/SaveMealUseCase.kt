package com.calometer.shared.domain

import com.calometer.shared.core.MealEntry
import com.calometer.shared.data.NutritionRepository

class SaveMealUseCase(private val repository: NutritionRepository) {
    suspend operator fun invoke(meal: MealEntry) = repository.saveMeal(meal)
}
