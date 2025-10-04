package com.calometer.shared

import com.calometer.shared.core.Id
import com.calometer.shared.core.MealEntry
import com.calometer.shared.core.MealLine
import com.calometer.shared.core.Nutrients
import com.calometer.shared.core.FoodItem
import com.calometer.shared.data.NutritionRepository
import com.calometer.shared.domain.SaveMealUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeNutritionRepository : NutritionRepository {
    var savedMeals: MutableList<MealEntry> = mutableListOf()

    override fun observeMeals() = throw NotImplementedError()
    override suspend fun getMeal(id: String) = null
    override suspend fun saveMeal(meal: MealEntry) { savedMeals += meal }
    override suspend fun deleteMeal(id: String) = Unit
    override suspend fun searchFoods(query: String, limit: Int) = emptyList<FoodItem>()
    override suspend fun upsertFoods(items: List<FoodItem>) = Unit
}

class SaveMealUseCaseTest {
    @Test
    fun savesMeal() = runTest {
        val repository = FakeNutritionRepository()
        val useCase = SaveMealUseCase(repository)
        val meal = MealEntry(
            id = Id("1"),
            timestamp = Clock.System.now(),
            items = listOf(
                MealLine(Id("food"), 1.0, "unit", Nutrients(100.0, 10.0, 10.0, 5.0)),
            ),
            note = null,
        )

        useCase(meal)

        assertEquals(1, repository.savedMeals.size)
    }
}
