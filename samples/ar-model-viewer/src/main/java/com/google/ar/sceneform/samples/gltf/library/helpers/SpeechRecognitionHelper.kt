package com.google.ar.sceneform.samples.gltf.library.helpers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeechRecognitionHelper(private val context: Context) {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private val _spokenText = MutableStateFlow<String>("")
    val spokenText: StateFlow<String> = _spokenText

    private var referenceText: String = ""
    private val mispronunciations = mutableListOf<String>()
    private val skippedWords = mutableListOf<String>()
    private val stutteredWords = mutableListOf<String>()

    init {
        setupRecognitionListener()
    }

    private fun setupRecognitionListener() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let { result ->
                    _spokenText.value = result
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
        speechRecognizer.startListening(recognitionIntent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun setReferenceText(text: String) {
        referenceText = text.toLowerCase()
    }

    private fun compareWithReferenceText(spokenText: String) {
        val spokenWords = spokenText.toLowerCase().split(" ")
        val referenceWords = referenceText.split(" ")

        var spokenIndex = 0
        var referenceIndex = 0

        while (spokenIndex < spokenWords.size && referenceIndex < referenceWords.size) {
            when {
                spokenWords[spokenIndex] == referenceWords[referenceIndex] -> {
                    spokenIndex++
                    referenceIndex++
                }
                spokenWords[spokenIndex].startsWith(referenceWords[referenceIndex]) -> {
                    stutteredWords.add(referenceWords[referenceIndex])
                    spokenIndex++
                }
                else -> {
                    mispronunciations.add(referenceWords[referenceIndex])
                    referenceIndex++
                }
            }
        }

        // Check for skipped words
        while (referenceIndex < referenceWords.size) {
            skippedWords.add(referenceWords[referenceIndex])
            referenceIndex++
        }
    }

    fun getResults(): Triple<List<String>, List<String>, List<String>> {
        return Triple(mispronunciations, skippedWords, stutteredWords)
    }

    fun reset() {
        mispronunciations.clear()
        skippedWords.clear()
        stutteredWords.clear()
        _spokenText.value = ""
    }

    fun destroy() {
        speechRecognizer.destroy()
    }
}