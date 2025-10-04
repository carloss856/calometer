package com.calometer.shared.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.native.driver.NativeSqliteDriver
import com.calometer.shared.database.CalometerDatabase

class IosDriverFactory : DriverFactory {
    override fun createDriver(): SqlDriver = NativeSqliteDriver(CalometerDatabase.Schema, "calometer.db")
}
