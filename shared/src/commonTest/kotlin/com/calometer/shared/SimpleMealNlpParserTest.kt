package com.calometer.shared

import com.calometer.shared.ai.InMemoryFoodCatalogProvider
import com.calometer.shared.ai.SimpleMealNlpParser
import com.calometer.shared.core.FoodItem
import com.calometer.shared.core.Id
import com.calometer.shared.core.Nutrients
import com.calometer.shared.core.Serving
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SimpleMealNlpParserTest {
    @Test
    fun parsesSpanishQuantities() = runTest {
        val catalog = InMemoryFoodCatalogProvider().apply {
            seed(
                listOf(
                    FoodItem(Id("pechuga"), "pechuga de pollo", defaultServing = Serving(100.0, "g"), perServing = Nutrients(165.0, 31.0, 0.0, 3.6)),
                ),
            )
        }
        val parser = SimpleMealNlpParser { catalog.findBestMatch(it) }

        val result = parser.parse("200gr pechuga de pollo")

        val line = result.lines.first()
        assertEquals(200.0, line.amount)
        assertEquals("g", line.unit)
        assertNotNull(line.computed)
        assertEquals(330.0, line.computed?.kcal, 0.01)
    }
}
