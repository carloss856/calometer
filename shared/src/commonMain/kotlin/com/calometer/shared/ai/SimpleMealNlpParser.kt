package com.calometer.shared.ai

import com.calometer.shared.core.Id
import com.calometer.shared.core.Nutrients
import com.calometer.shared.core.Serving
import com.calometer.shared.core.UnitConverters

/**
 * Simple heuristic parser for the MVP. It splits lines by commas and attempts to extract
 * quantities expressed as numbers followed by units (g, ml, cup...).
 */
class SimpleMealNlpParser(
    private val foodCatalog: suspend (String) -> CatalogMatch?,
) : MealNlpParser {

    override suspend fun parse(input: String): ParsedMeal {
        val lines = input.split(",", "\n").mapNotNull { raw ->
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return@mapNotNull null
            val tokens = TOKEN_REGEX.find(trimmed)
            val amount = tokens?.groups?.get("amount")?.value?.replace(',', '.')?.toDoubleOrNull()
            val unit = tokens?.groups?.get("unit")?.value?.let(UnitConverters::normalizeUnit)
            val name = tokens?.groups?.get("name")?.value?.trim()?.ifEmpty { null } ?: trimmed
            val match = foodCatalog(name)?.takeIf { it.confidence > 0.3 }
            val computed = if (amount != null && unit != null && match != null) {
                val normalizedAmount = normalize(amount, unit, match.serving)
                normalizedAmount?.let {
                    val factor = it / match.serving.amount
                    Nutrients(
                        kcal = match.nutrients.kcal * factor,
                        protein = match.nutrients.protein * factor,
                        carbs = match.nutrients.carbs * factor,
                        fat = match.nutrients.fat * factor,
                    )
                }
            } else null
            ParsedLine(
                raw = trimmed,
                foodName = name,
                amount = amount,
                unit = unit,
                matchedFoodId = match?.id,
                computed = computed,
                confidence = match?.confidence ?: 0.2,
            )
        }
        return ParsedMeal(lines)
    }

    private fun normalize(amount: Double, unit: String, serving: Serving): Double? = when (unit) {
        serving.unit -> amount
        "g", "kg" -> UnitConverters.toGrams(amount, unit)
        "ml", "l", "cup", "tbsp", "tsp" -> UnitConverters.toMilliliters(amount, unit)
        else -> null
    }

    data class CatalogMatch(
        val id: Id,
        val nutrients: Nutrients,
        val serving: Serving,
        val confidence: Double,
    )

    companion object {
        private val TOKEN_REGEX = Regex(
            pattern = "(?i)(?<amount>\\d+(?:[.,]\\d+)?)\\s*(?<unit>g|gramos|ml|l|cup|taza|cups|tbsp|cda|tsp|cdta|oz|lb)?\\s*(?<name>.+)",
        )
    }
}
