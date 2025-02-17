package com.google.ar.sceneform.samples.gltf.library.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity



@Dao
interface ModelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ModelEntity)

    @Query("SELECT * FROM models WHERE name = :name")
    suspend fun getModelByName(name: String): ModelEntity?

    @Query("SELECT * FROM models")
    fun getAllModels(): LiveData<List<ModelEntity>>

    @Query("SELECT name FROM models")
    fun getAllModelNames(): LiveData<List<String>>

}
