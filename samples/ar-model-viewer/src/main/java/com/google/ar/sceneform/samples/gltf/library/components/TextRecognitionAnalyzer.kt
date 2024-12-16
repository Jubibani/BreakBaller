package com.google.ar.sceneform.samples.gltf.library.components

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class TextRecognitionAnalyzer(private val onTextRecognized: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val textRecognitionManager = TextRecognitionManager()

    override fun analyze(image: ImageProxy) {
        textRecognitionManager.recognizeText(
            image,
            onSuccess = { recognizedText ->
                if (recognizedText.isNotEmpty()) {
                    onTextRecognized(recognizedText)
                }
            },
            onError = { exception ->
                Log.e("TextRecognitionAnalyzer", "Text recognition failed", exception)
            }
        )
    }
}