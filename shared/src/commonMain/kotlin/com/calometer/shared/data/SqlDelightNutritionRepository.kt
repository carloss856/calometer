package com.calometer.shared.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.calometer.shared.core.FoodItem
import com.calometer.shared.core.Id
import com.calometer.shared.core.MealEntry
import com.calometer.shared.core.MealLine
import com.calometer.shared.core.Nutrients
import com.calometer.shared.core.Serving
import com.calometer.shared.database.CalometerDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class SqlDelightNutritionRepository(
    private val database: CalometerDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NutritionRepository {

    override fun observeMeals(): Flow<List<MealEntry>> = database.mealsQueries
        .selectAll()
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.map { row ->
            val items = database.meal_linesQueries.selectByMeal(row.id).executeAsList().map { line ->
                MealLine(
                    foodId = Id(line.food_id),
                    quantity = line.quantity,
                    unit = line.unit,
                    computed = Nutrients(line.kcal, line.protein, line.carbs, line.fat),
                )
            }
            MealEntry(
                id = Id(row.id),
                timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                items = items,
                note = row.note,
            )
        } }

    override suspend fun getMeal(id: String): MealEntry? = withContext(dispatcher) {
        database.mealsQueries.selectById(id).executeAsOneOrNull()?.let { row ->
            val items = database.meal_linesQueries.selectByMeal(row.id).executeAsList().map { line ->
                MealLine(
                    foodId = Id(line.food_id),
                    quantity = line.quantity,
                    unit = line.unit,
                    computed = Nutrients(line.kcal, line.protein, line.carbs, line.fat),
                )
            }
            MealEntry(
                id = Id(row.id),
                timestamp = Instant.fromEpochMilliseconds(row.timestamp),
                items = items,
                note = row.note,
            )
        }
    }

    override suspend fun saveMeal(meal: MealEntry) = withContext(dispatcher) {
        database.transaction {
            database.mealsQueries.insertMeal(
                id = meal.id.raw,
                timestamp = meal.timestamp.toEpochMilliseconds(),
                note = meal.note,
            )
            database.meal_linesQueries.deleteByMeal(meal.id.raw)
            meal.items.forEach { item ->
                database.meal_linesQueries.insertMealLine(
                    meal_id = meal.id.raw,
                    food_id = item.foodId.raw,
                    quantity = item.quantity,
                    unit = item.unit,
                    kcal = item.computed.kcal,
                    protein = item.computed.protein,
                    carbs = item.computed.carbs,
                    fat = item.computed.fat,
                )
            }
        }
    }

    override suspend fun deleteMeal(id: String) = withContext(dispatcher) {
        database.mealsQueries.deleteMeal(id)
    }

    override suspend fun searchFoods(query: String, limit: Int): List<FoodItem> = withContext(dispatcher) {
        database.foodsQueries.searchFoods("%${query.lowercase()}%", limit.toLong()).executeAsList().map { row ->
            FoodItem(
                id = Id(row.id),
                name = row.name,
                brand = row.brand,
                defaultServing = Serving(row.default_amount, row.default_unit),
                perServing = Nutrients(row.kcal, row.protein, row.carbs, row.fat),
            )
        }
    }

    override suspend fun upsertFoods(items: List<FoodItem>) = withContext(dispatcher) {
        database.transaction {
            items.forEach { item ->
                database.foodsQueries.insertFood(
                    id = item.id.raw,
                    name = item.name,
                    brand = item.brand,
                    default_amount = item.defaultServing.amount,
                    default_unit = item.defaultServing.unit,
                    kcal = item.perServing.kcal,
                    protein = item.perServing.protein,
                    carbs = item.perServing.carbs,
                    fat = item.perServing.fat,
                )
            }
        }
    }
}
