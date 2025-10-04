package com.calometer.shared.ai

import com.calometer.shared.core.FoodItem
import com.calometer.shared.core.Id
import com.calometer.shared.core.Nutrients
import com.calometer.shared.core.Serving

interface FoodCatalogProvider {
    suspend fun findBestMatch(query: String): SimpleMealNlpParser.CatalogMatch?
    suspend fun preloadDefaults(): List<FoodItem>
}

class InMemoryFoodCatalogProvider(
    private val items: MutableList<FoodItem> = mutableListOf(),
) : FoodCatalogProvider {
    override suspend fun findBestMatch(query: String): SimpleMealNlpParser.CatalogMatch? {
        val normalized = query.lowercase()
        val match = items.maxByOrNull { candidate ->
            val name = candidate.name.lowercase()
            val score = when {
                name == normalized -> 1.0
                name.contains(normalized) -> 0.8
                else -> 0.0
            }
            score
        } ?: return null
        val confidence = if (match.name.equals(query, ignoreCase = true)) 0.9 else 0.6
        return SimpleMealNlpParser.CatalogMatch(
            id = match.id,
            nutrients = match.perServing,
            serving = match.defaultServing,
            confidence = confidence,
        )
    }

    override suspend fun preloadDefaults(): List<FoodItem> = items

    fun seed(items: List<FoodItem>) {
        this.items.clear()
        this.items.addAll(items)
    }
}
