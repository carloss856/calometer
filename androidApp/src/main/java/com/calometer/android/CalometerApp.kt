package com.calometer.android

import android.app.Application
import com.calometer.shared.ai.InMemoryFoodCatalogProvider
import com.calometer.shared.core.FoodItem
import com.calometer.shared.core.Id
import com.calometer.shared.core.Nutrients
import com.calometer.shared.core.Serving
import com.calometer.shared.core.initializeSharedKoin
import com.calometer.shared.core.koin
import com.calometer.shared.data.NutritionRepository
import com.calometer.shared.database.AndroidDriverFactory
import com.calometer.shared.features.steps.AndroidStepCounter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.get

class CalometerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val catalog = InMemoryFoodCatalogProvider().apply {
            seed(
                listOf(
                    FoodItem(
                        id = Id("apple"),
                        name = "Apple",
                        defaultServing = Serving(182.0, "g"),
                        perServing = Nutrients(95.0, 0.5, 25.0, 0.3),
                    ),
                    FoodItem(
                        id = Id("oats"),
                        name = "Oats",
                        defaultServing = Serving(40.0, "g"),
                        perServing = Nutrients(150.0, 5.0, 27.0, 3.0),
                    ),
                ),
            )
        }
        initializeSharedKoin(
            driverFactory = AndroidDriverFactory(this),
            stepCounter = AndroidStepCounter(this),
            catalogProvider = catalog,
        )
        CoroutineScope(Dispatchers.Default).launch {
            koin.get<NutritionRepository>().upsertFoods(catalog.preloadDefaults())
        }
    }
}
