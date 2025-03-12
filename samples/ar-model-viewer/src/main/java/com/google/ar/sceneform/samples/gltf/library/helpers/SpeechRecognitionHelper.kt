// SpeechRecognitionHelper.kt
package com.google.ar.sceneform.samples.gltf.library.helpers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.Locale

class SpeechRecognitionHelper(private val context: Context) {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val _spokenText = MutableLiveData<String>()
    val spokenText: LiveData<String> get() = _spokenText
    private var onResultsListener: ((String) -> Unit)? = null
    private var referenceText: String? = null

    init {
        setupRecognitionListener()
    }

    private fun setupRecognitionListener() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let { result ->
                    _spokenText.value = result
                    onResultsListener?.invoke(result)
                    compareWithReferenceText(result)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let { result ->
                    _spokenText.value = result
                }
            }

            // Implement other RecognitionListener methods as needed
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening() {
        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer.startListening(recognitionIntent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun setReferenceText(text: String) {
        referenceText = text.lowercase(Locale.ROOT)
    }

    fun setOnResultsListener(listener: (String) -> Unit) {
        onResultsListener = listener
    }

    fun destroy() {
        speechRecognizer.destroy()
    }

    private fun compareWithReferenceText(result: String) {
        // Implement comparison logic here
    }

    fun getResults(): Triple<List<String>, List<String>, List<String>> {
        // Implement logic to get mispronunciations, skippedWords, and stutteredWords
        return Triple(emptyList(), emptyList(), emptyList())
    }
}