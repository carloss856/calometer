package com.calometer.shared.core

import com.calometer.shared.ai.FoodCatalogProvider
import com.calometer.shared.data.NutritionRepository
import com.calometer.shared.database.IosDriverFactory
import com.calometer.shared.features.steps.IosStepCounter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.get

fun initializeIosDependencies(catalogProvider: FoodCatalogProvider) {
    initializeSharedKoin(
        driverFactory = IosDriverFactory(),
        stepCounter = IosStepCounter(),
        catalogProvider = catalogProvider,
    )
    CoroutineScope(Dispatchers.Default).launch {
        koin.get<NutritionRepository>().upsertFoods(catalogProvider.preloadDefaults())
    }
}
