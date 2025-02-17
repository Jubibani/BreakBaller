package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity
import com.google.ar.sceneform.samples.gltf.library.data.repository.ModelRepository
import kotlinx.coroutines.launch

class ModelViewModel(private val repository: ModelRepository) : ViewModel() {

    val allModels: LiveData<List<ModelEntity>> = repository.allModels

    fun getModelByName(name: String): LiveData<ModelEntity?> {
        val result = MutableLiveData<ModelEntity?>()
        viewModelScope.launch {
            result.postValue(repository.getModelByName(name))
        }
        return result
    }

}
