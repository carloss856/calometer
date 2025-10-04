package com.calometer.shared.database

import android.content.Context
import app.cash.sqldelight.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.calometer.shared.database.CalometerDatabase

class AndroidDriverFactory(private val context: Context) : DriverFactory {
    override fun createDriver(): SqlDriver = AndroidSqliteDriver(CalometerDatabase.Schema, context, "calometer.db")
}
