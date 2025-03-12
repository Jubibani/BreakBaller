import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.Locale

class SpeechRecognitionHelper(private val context: Context) {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val _spokenText = MutableLiveData<String>()
    val spokenText: LiveData<String> get() = _spokenText
    private var onResultsListener: ((String) -> Unit)? = null
    private var referenceText: String? = null
    private var isListening = false
    private val handler = Handler(Looper.getMainLooper())
    private var lastListeningTime: Long = 0

    init {
        setupRecognitionListener()
    }

    private fun setupRecognitionListener() {
        // [FOR DEBUGGING PURPOSES ONLY]
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechRecognition", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognition", "Speech beginning")
            }

            override fun onRmsChanged(rmsdB: Float) {
           /*     Log.d("SpeechRecognition", "RMS changed: $rmsdB")*/
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            /*    Log.d("SpeechRecognition", "Buffer received")*/
            }

            override fun onEndOfSpeech() {
                Log.d("SpeechRecognition", "End of speech")
                if (isListening) {
                    Log.d("SpeechRecognition", "Restarting listening after pause")
                    handler.postDelayed({ startListening() }, 500) // Small delay to avoid rapid restarts
                }
            }

            override fun onError(error: Int) {
                Log.e("SpeechRecognition", "Error: $error")

                if (isListening) {
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                            Log.d("SpeechRecognition", "No speech detected, restarting...")
                            handler.postDelayed({ startListening() }, 500) // Small delay to avoid rapid restarts
                        }
                        else -> {
                            Log.e("SpeechRecognition", "Unhandled error: $error")
                        }
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let { result ->
                    Log.d("SpeechRecognition", "Results: $result")
                    _spokenText.value = result
                    onResultsListener?.invoke(result)
                    compareWithReferenceText(result)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let { result ->
                    Log.d("SpeechRecognition", "Partial results: $result")
                    _spokenText.value = result
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("SpeechRecognition", "Event: $eventType")
            }
        })
    }

    fun startListening() {
        if (allPermissionsGranted()) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastListeningTime < 1000) { // Prevent immediate restart
                Log.d("SpeechRecognition", "Skipping restart to avoid rapid looping")
                return
            }
            lastListeningTime = currentTime

            val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            Log.d("SpeechRecognition", "Starting to listen for speech")
            isListening = true
            speechRecognizer.startListening(recognitionIntent)
        } else {
            Log.e("SpeechRecognition", "Permissions not granted")
        }
    }

    fun stopListening() {
        Log.d("SpeechRecognition", "Stopping listening")
        isListening = false
        speechRecognizer.stopListening()
    }

    fun setReferenceText(text: String) {
        referenceText = text.lowercase(Locale.ROOT)
    }

    fun setOnResultsListener(listener: (String) -> Unit) {
        onResultsListener = listener
    }

    fun destroy() {
        isListening = false
        speechRecognizer.destroy()
    }

    private fun compareWithReferenceText(result: String) {
        // Implement comparison logic here
    }

    fun getResults(): Triple<List<String>, List<String>, List<String>> {
        // Implement logic to get mispronunciations, skippedWords, and stutteredWords
        return Triple(emptyList(), emptyList(), emptyList())
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }
}