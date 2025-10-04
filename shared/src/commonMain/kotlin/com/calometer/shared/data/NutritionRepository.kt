package com.calometer.shared.data

import com.calometer.shared.core.FoodItem
import com.calometer.shared.core.MealEntry
import com.calometer.shared.core.Nutrients
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun observeMeals(): Flow<List<MealEntry>>
    suspend fun getMeal(id: String): MealEntry?
    suspend fun saveMeal(meal: MealEntry)
    suspend fun deleteMeal(id: String)
    suspend fun searchFoods(query: String, limit: Int = 20): List<FoodItem>
    suspend fun upsertFoods(items: List<FoodItem>)
}

fun Nutrients.sumWith(other: Nutrients): Nutrients = Nutrients(
    kcal = kcal + other.kcal,
    protein = protein + other.protein,
    carbs = carbs + other.carbs,
    fat = fat + other.fat,
)
