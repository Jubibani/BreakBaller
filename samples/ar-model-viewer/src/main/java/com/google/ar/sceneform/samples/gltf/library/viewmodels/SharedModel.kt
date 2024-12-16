package com.google.ar.sceneform.samples.gltf.library.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _difficultWords = MutableLiveData<List<String>>()
    val difficultWords: LiveData<List<String>> = _difficultWords

    fun updateDifficultWords(words: List<String>) {
        _difficultWords.value = words
    }
}