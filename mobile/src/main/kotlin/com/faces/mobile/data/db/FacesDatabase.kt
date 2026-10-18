package com.faces.mobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [FaceEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FacesDatabase : RoomDatabase() {

    abstract fun faceDao(): FaceDao

    companion object {
        @Volatile
        private var INSTANCE: FacesDatabase? = null

        fun getInstance(context: Context): FacesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FacesDatabase::class.java,
                    "faces_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.faceDao().insertFaces(FaceEntity.createStockPresets())
                    }
                }
            }
        }
    }
}
