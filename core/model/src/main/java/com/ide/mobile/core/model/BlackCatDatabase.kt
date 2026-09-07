package com.ide.mobile.core.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class BlackCatDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: BlackCatDatabase? = null

        fun getDatabase(context: Context): BlackCatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlackCatDatabase::class.java,
                    "black_cat_ide_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
