package com.calometer.shared.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.calometer.shared.core.StepSnapshot
import com.calometer.shared.core.StepSource
import com.calometer.shared.database.CalometerDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDate.Companion.fromEpochDays
import kotlinx.datetime.toEpochDays

class SqlDelightStepsRepository(
    private val database: CalometerDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : StepsRepository {

    override fun observeSteps(range: ClosedRange<LocalDate>): Flow<List<StepSnapshot>> = database.steps_dailyQueries
        .selectStepsRange(range.start.toEpochDays().toLong(), range.endInclusive.toEpochDays().toLong())
        .asFlow()
        .mapToList(dispatcher)
        .map { rows ->
            rows.map { row ->
                StepSnapshot(
                    date = LocalDate.fromEpochDays(row.date.toInt()),
                    steps = row.steps,
                    source = StepSource.valueOf(row.source),
                )
            }
        }

    override suspend fun getSteps(date: LocalDate): StepSnapshot? = withContext(dispatcher) {
        database.steps_dailyQueries.selectStepsByDate(date.toEpochDays().toLong()).executeAsOneOrNull()?.let { row ->
            StepSnapshot(
                date = LocalDate.fromEpochDays(row.date.toInt()),
                steps = row.steps,
                source = StepSource.valueOf(row.source),
            )
        }
    }

    override suspend fun upsert(snapshot: StepSnapshot) = withContext(dispatcher) {
        database.steps_dailyQueries.upsertSteps(
            date = snapshot.date.toEpochDays().toLong(),
            steps = snapshot.steps,
            source = snapshot.source.name,
        )
    }
}
