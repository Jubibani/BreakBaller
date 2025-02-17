package com.google.ar.sceneform.samples.gltf.library.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.ModelDao
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.google.ar.sceneform.samples.gltf.R

@Database(entities = [ModelEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun modelDao(): ModelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "model_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(ModelDatabaseCallback(scope)) // Add database callback here
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Callback to populate database on creation
    private class ModelDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.modelDao())
                }
            }
        }

        suspend fun populateDatabase(modelDao: ModelDao) {
            modelDao.insertModel(ModelEntity("Amphibian", "models/amphibian.glb", R.layout.amphibian_infos, "Tap to learn more!", R.raw.froggy))
            modelDao.insertModel(ModelEntity("Bacteria", "models/bacteria.glb", R.layout.bacteria_infos, "Tap to explore bacterial structures!", R.raw.bacteriasound))
            modelDao.insertModel(ModelEntity("Digestive", "models/digestive.glb", R.layout.digestive_infos, "Tap to see the digestive process!", R.raw.digestsound))
            modelDao.insertModel(ModelEntity("Platypus", "models/platypus.glb", R.layout.platypus_infos, "Tap to discover platypus facts!", R.raw.platypusound))
            modelDao.insertModel(ModelEntity("Heart", "models/heart.glb", R.layout.heart_info, "Tap to see the heart in action!", R.raw.heartsound))
        }
    }
}
