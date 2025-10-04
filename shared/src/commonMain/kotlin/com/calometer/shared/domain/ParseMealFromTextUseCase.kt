package com.calometer.shared.domain

import com.calometer.shared.ai.MealNlpParser
import com.calometer.shared.ai.ParsedMeal

class ParseMealFromTextUseCase(
    private val parser: MealNlpParser,
) {
    suspend operator fun invoke(text: String): ParsedMeal = parser.parse(text)
}
