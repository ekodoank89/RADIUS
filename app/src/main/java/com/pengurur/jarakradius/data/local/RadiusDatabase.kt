package com.pengurur.jarakradius.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MarkerEntity::class], version = 1, exportSchema = false)
abstract class RadiusDatabase : RoomDatabase() {

    abstract fun markerDao(): MarkerDao

    companion object {
        @Volatile
        private var INSTANCE: RadiusDatabase? = null

        fun getInstance(context: Context): RadiusDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RadiusDatabase::class.java,
                    "radius.db"
                ).build().also { INSTANCE = it }
            }
    }
}
