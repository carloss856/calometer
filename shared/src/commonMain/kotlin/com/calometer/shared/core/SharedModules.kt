package com.calometer.shared.core

import com.calometer.shared.ai.FoodCatalogProvider
import com.calometer.shared.ai.SimpleMealNlpParser
import com.calometer.shared.database.CalometerDatabase
import com.calometer.shared.database.DriverFactory
import com.calometer.shared.data.NutritionRepository
import com.calometer.shared.data.SqlDelightNutritionRepository
import com.calometer.shared.data.SqlDelightStepsRepository
import com.calometer.shared.data.SqlDelightWorkoutRepository
import com.calometer.shared.data.StepsRepository
import com.calometer.shared.data.WorkoutRepository
import com.calometer.shared.domain.GetDailyStepsUseCase
import com.calometer.shared.domain.ParseMealFromTextUseCase
import com.calometer.shared.domain.SaveMealUseCase
import com.calometer.shared.domain.SaveWorkoutSessionUseCase
import com.calometer.shared.domain.SearchFoodsUseCase
import com.calometer.shared.domain.UpsertRoutineUseCase
import com.calometer.shared.domain.UpsertStepsUseCase
import com.calometer.shared.features.nutrition.NutritionStore
import com.calometer.shared.features.steps.StepCounter
import com.calometer.shared.features.steps.StepsStore
import com.calometer.shared.features.workouts.WorkoutStore
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

lateinit var koin: Koin
    private set

fun initializeSharedKoin(
    driverFactory: DriverFactory,
    stepCounter: StepCounter,
    catalogProvider: FoodCatalogProvider,
): KoinApplication {
    Napier.base(DebugAntilog())
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val commonModule = module {
        single { driverFactory.createDriver() }
        single { CalometerDatabase(get()) }
        single<NutritionRepository> { SqlDelightNutritionRepository(get()) }
        single<WorkoutRepository> { SqlDelightWorkoutRepository(get()) }
        single<StepsRepository> { SqlDelightStepsRepository(get()) }
        single { SimpleMealNlpParser { query -> catalogProvider.findBestMatch(query) } }
        single { ParseMealFromTextUseCase(get()) }
        single { SaveMealUseCase(get()) }
        single { SearchFoodsUseCase(get()) }
        single { UpsertRoutineUseCase(get()) }
        single { SaveWorkoutSessionUseCase(get()) }
        single { GetDailyStepsUseCase(get()) }
        single { UpsertStepsUseCase(get()) }
        single { stepCounter }
        single { appScope }
        single { NutritionStore(get(), get(), get()) }
        single { WorkoutStore(get(), get(), get()) }
        single { StepsStore(get(), get(), get(), get()) }
    }
    val app = startKoin { modules(commonModule) }
    koin = app.koin
    return app
}
