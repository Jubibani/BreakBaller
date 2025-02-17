package com.google.ar.sceneform.samples.gltf.library.data.repository

import androidx.lifecycle.LiveData
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.ModelDao
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity

class ModelRepository(private val modelDao: ModelDao) {

    val allModels: LiveData<List<ModelEntity>> = modelDao.getAllModels()

    suspend fun insert(model: ModelEntity) {
        modelDao.insertModel(model)
    }

    suspend fun getModelByName(name: String): ModelEntity? {
        return modelDao.getModelByName(name)
    }


}
