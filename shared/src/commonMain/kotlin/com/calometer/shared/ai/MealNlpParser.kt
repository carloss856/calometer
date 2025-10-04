package com.calometer.shared.ai

import com.calometer.shared.core.Id
import com.calometer.shared.core.Nutrients

interface MealNlpParser {
    suspend fun parse(input: String): ParsedMeal
}

data class ParsedLine(
    val raw: String,
    val foodName: String?,
    val amount: Double?,
    val unit: String?,
    val matchedFoodId: Id?,
    val computed: Nutrients?,
    val confidence: Double,
)

data class ParsedMeal(val lines: List<ParsedLine>)
