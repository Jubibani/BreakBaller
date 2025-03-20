package com.google.ar.sceneform.samples.gltf.library

import android.app.Application
import android.util.Log
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.helpers.VoskSpeechRecognitionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.io.File

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

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_COMPLETE || level == TRIM_MEMORY_UI_HIDDEN) {
            deleteModelDirectory()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        deleteModelDirectory()
    }

    private fun deleteModelDirectory() {
        val modelDir = File(cacheDir, "vosk-model-small-en-us-0.15")
        if (modelDir.exists()) {
            val deleted = modelDir.deleteRecursively()
            if (deleted) {
                Log.d("App", "Model directory and its contents deleted successfully")
            } else {
                Log.e("App", "Failed to delete model directory and its contents")
            }
        } else {
            Log.d("App", "Model directory does not exist")
        }
    }
}