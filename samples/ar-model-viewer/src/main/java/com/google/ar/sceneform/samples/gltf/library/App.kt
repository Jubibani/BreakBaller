package com.google.ar.sceneform.samples.gltf.library

import android.app.Application
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.helpers.VoskSpeechRecognitionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    private val applicationScope: CoroutineScope = MainScope()

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(applicationContext, applicationScope)
    }

    lateinit var voskSpeechRecognitionHelper: VoskSpeechRecognitionHelper
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        voskSpeechRecognitionHelper = VoskSpeechRecognitionHelper(this)
    }
}