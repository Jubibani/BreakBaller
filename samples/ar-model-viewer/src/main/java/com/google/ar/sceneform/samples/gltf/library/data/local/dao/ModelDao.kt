package com.google.ar.sceneform.samples.gltf.library.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity


@Dao
interface ModelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) //This prevents UNIQUE constraint failed errors when inserting duplicate models.
    suspend fun insertModel(model: ModelEntity)


    @Query("SELECT * FROM models WHERE name = :modelName LIMIT 1")
    suspend fun getModelByName(modelName: String): ModelEntity?

    @Query("SELECT * FROM models")
    fun getAllModels(): LiveData<List<ModelEntity>>

    @Delete
    suspend fun deleteModel(model: ModelEntity)
}
