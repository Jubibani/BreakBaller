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
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.airbnb.lottie.LottieAnimationView
import java.util.Locale

class SpeechRecognitionHelper(private val context: Context, private val soundwaveAnimationView: LottieAnimationView) {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val _spokenText = MutableLiveData<String>()
    val spokenText: LiveData<String> get() = _spokenText
    private val _isSpeaking = MutableLiveData<Boolean>()
    val isSpeaking: LiveData<Boolean> get() = _isSpeaking
    private var onResultsListener: ((String) -> Unit)? = null
    private var referenceText: String? = null
    private var isListening = false
    private val handler = Handler(Looper.getMainLooper())
    private var lastListeningTime: Long = 0
    private var previousRecognizedText: String? = null

    init {
        setupRecognitionListener()
    }

    private fun setupRecognitionListener() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechRecognition", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                _isSpeaking.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("SpeechRecognition", "End of speech")
                _isSpeaking.value = false
                if (isListening) {
                    handler.postDelayed({ startListening() }, 500)
                }
            }

            override fun onError(error: Int) {
                Log.e("SpeechRecognition", "Error: $error")
                _isSpeaking.value = false
                if (isListening) {
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                            handler.postDelayed({ startListening() }, 500)
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

                    // Control the animation based on the recognized text
                    if (!result.isNullOrBlank() && result != previousRecognizedText) {
                        soundwaveAnimationView.playAnimation() // Start animation only when new text is recognized
                        Log.d("SpeechRecognition", "Playing animation for new text: $result")
                        previousRecognizedText = result
                    } else {
                        soundwaveAnimationView.pauseAnimation() // Pause animation if no valid speech is detected
                        Log.d("SpeechRecognition", "Pausing animation, no new valid speech detected")
                    }
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
            if (currentTime - lastListeningTime < 1000) {
                return
            }
            lastListeningTime = currentTime

            val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            isListening = true
            soundwaveAnimationView.visibility = View.VISIBLE // Show animation view
            speechRecognizer.startListening(recognitionIntent)
        } else {
            Log.e("SpeechRecognition", "Permissions not granted")
        }
    }

    fun stopListening() {
        isListening = false
        soundwaveAnimationView.visibility = View.GONE // Hide animation view
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

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }
}