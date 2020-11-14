package com.scaxias.enterprise.trackingrun.db.run

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.scaxias.enterprise.trackingrun.db.run.converters.RunTypeConverters
import com.scaxias.enterprise.trackingrun.db.run.dao.RunDAO
import com.scaxias.enterprise.trackingrun.db.run.entities.Run

@Database(
        entities = [Run::class],
        version = 1
)
@TypeConverters(RunTypeConverters::class)
abstract class RunDatabase: RoomDatabase() {

    abstract fun gerRunDao(): RunDAO
}
