package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.ar.sceneform.samples.gltf.library.data.repository.ModelRepository

class ModelViewModelFactory(private val repository: ModelRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
