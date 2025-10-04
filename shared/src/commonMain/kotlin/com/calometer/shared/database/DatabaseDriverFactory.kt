package com.calometer.shared.database

import app.cash.sqldelight.db.SqlDriver

interface DriverFactory {
    fun createDriver(): SqlDriver
}
