package com.listocalixto.dailycosmos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.listocalixto.dailycosmos.data.model.APODEntity

@Database(entities = [APODEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun apodDao(): APODDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            INSTANCE = INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "apod_table"
            ).build()
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }

    }
}