package com.rejowan.chargify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ChargingSession::class],
    version = 1,
    exportSchema = false
)
abstract class ChargifyDatabase : RoomDatabase() {
    abstract fun chargingSessionDao(): ChargingSessionDao
}
