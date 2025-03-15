package com.google.ar.sceneform.samples.gltf.library.helpers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService

class VoskSpeechRecognitionHelper(private val context: Context) {

    private lateinit var model: Model
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null
    private val handler = Handler(Looper.getMainLooper())
    private var modelInitializedCallback: (() -> Unit)? = null
    private var progressCallback: ((Int) -> Unit)? = null

    fun initializeModel(model: Model) {
        val startTime = System.currentTimeMillis() // Start time

        Log.d("VoskSpeechRecognition", "Initializing model...")

        this.model = model
        recognizer = Recognizer(model, 16000.0f)

        val endTime = System.currentTimeMillis()
        val loadTime = (endTime - startTime) / 1000.0 // Calculate seconds

        Log.d("VoskSpeechRecognition", "Model initialized in $loadTime seconds")

        // Ensure callback is invoked after model is set
        handler.post {
            modelInitializedCallback?.invoke()
        }
    }

    fun setModelInitializedCallback(callback: () -> Unit) {
        modelInitializedCallback = callback
        if (isModelInitialized()) {
            handler.post { callback() } // Ensure it runs on UI thread
        }
    }

    fun setProgressCallback(callback: (Int) -> Unit) {
        progressCallback = callback
    }

    fun isModelInitialized(): Boolean {
        return this::model.isInitialized && recognizer != null
    }

    fun startListening() {
        recognizer?.let {
            if (speechService == null) {
                speechService = SpeechService(it, 16000.0f)
                speechService?.startListening(object : RecognitionListener {
                    override fun onPartialResult(hypothesis: String?) {
                        Log.d("VoskSpeechRecognition", "Partial result: $hypothesis")
                    }

                    override fun onResult(hypothesis: String?) {
                        Log.d("VoskSpeechRecognition", "Result: $hypothesis")
                    }

                    override fun onFinalResult(hypothesis: String?) {
                        Log.d("VoskSpeechRecognition", "Final result: $hypothesis")
                    }

                    override fun onError(exception: Exception?) {
                        Log.e("VoskSpeechRecognition", "Error: ${exception?.message}")
                    }

                    override fun onTimeout() {
                        Log.d("VoskSpeechRecognition", "Timeout")
                    }
                })
            }
        } ?: run {
            Log.e("VoskSpeechRecognition", "Recognizer is not initialized")
        }
    }

    fun stopListening() {
        speechService?.stop()
        speechService = null
    }

    fun destroy() {
        speechService?.shutdown()
        model.close()
    }
}