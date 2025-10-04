package com.calometer.shared.domain

import com.calometer.shared.core.FoodItem
import com.calometer.shared.data.NutritionRepository

class SearchFoodsUseCase(private val repository: NutritionRepository) {
    suspend operator fun invoke(query: String, limit: Int = 20): List<FoodItem> =
        repository.searchFoods(query, limit)
}
