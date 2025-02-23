package com.google.ar.sceneform.samples.gltf.library.data.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.ModelDao
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val modelDao: ModelDao = AppDatabase.getDatabase(application, viewModelScope).modelDao()

    // LiveData that LibraryActivity observes
    val allModels: LiveData<List<ModelEntity>> = modelDao.getAllModels()

}
