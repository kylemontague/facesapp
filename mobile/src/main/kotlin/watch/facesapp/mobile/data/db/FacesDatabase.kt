package watch.facesapp.mobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [FaceEntity::class, FaceWearLogEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FacesDatabase : RoomDatabase() {

    abstract fun faceDao(): FaceDao

    companion object {
        @Volatile
        private var INSTANCE: FacesDatabase? = null

        fun getInstance(context: Context): FacesDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    FacesDatabase::class.java,
                    "faces_database"
                )
                    .addCallback(DatabaseCallback(appContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedDatabase(context)
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                seedDatabase(context)
            }

            private fun seedDatabase(context: Context) {
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(context)
                    val presets = FaceEntity.createStockPresets()
                    database.faceDao().insertFaces(presets)
                    // Seed initial wear log for the active stock face
                    presets.firstOrNull { it.isCurrentOnWatch }?.let { activeFace ->
                        database.faceDao().insertWearLog(
                            FaceWearLogEntity(
                                faceId = activeFace.id,
                                wornAt = activeFace.lastWornAt ?: System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
    }
}
