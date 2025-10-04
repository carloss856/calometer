package com.calometer.shared.core

object UnitConverters {
    private val volumeToMl = mapOf(
        "ml" to 1.0,
        "l" to 1000.0,
        "cup" to 240.0,
        "tsp" to 5.0,
        "tbsp" to 15.0,
    )
    private val weightToGrams = mapOf(
        "g" to 1.0,
        "kg" to 1000.0,
        "oz" to 28.3495,
        "lb" to 453.592,
    )

    fun normalizeUnit(unit: String): String = when (unit.lowercase()) {
        "grams", "gram", "gr", "g" -> "g"
        "kilogram", "kg" -> "kg"
        "milliliter", "milliliters", "ml" -> "ml"
        "liter", "litre", "liters", "l" -> "l"
        "cups", "cup" -> "cup"
        "tablespoon", "tablespoons", "tbsp" -> "tbsp"
        "teaspoon", "teaspoons", "tsp" -> "tsp"
        else -> unit.lowercase()
    }

    fun toGrams(amount: Double, unit: String): Double? = weightToGrams[normalizeUnit(unit)]?.let { amount * it }

    fun toMilliliters(amount: Double, unit: String): Double? = volumeToMl[normalizeUnit(unit)]?.let { amount * it }
}
