package com.google.ar.sceneform.samples.gltf.library.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.sceneform.samples.gltf.R

class CameraSpeechActivity : AppCompatActivity() {

    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var cameraManager: CameraManager
    private lateinit var resultTextView: TextView
    private lateinit var startButton: Button
    private lateinit var captureButton: Button
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_speech)

        initializeViews()
        setupSpeechRecognition()
        setupCamera()
        setupListeners()
    }

    private fun initializeViews() {
        resultTextView = findViewById(R.id.resultTextView)
        startButton = findViewById(R.id.startButton)
        captureButton = findViewById(R.id.captureButton)
        previewView = findViewById(R.id.previewView)
    }

    private fun setupSpeechRecognition() {
        speechRecognitionManager = SpeechRecognitionManager(this)
        speechRecognitionManager.onResultsListener = { result ->
            resultTextView.text = result
        }
    }

    private fun setupCamera() {
        cameraManager = CameraManager(this)
    }

    private fun setupListeners() {
        startButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_RECORD_AUDIO)
            } else {
                startSpeechRecognition()
            }
        }

        captureButton.setOnClickListener {
            cameraManager.takePhoto { bitmap ->
                // Handle the captured photo
                // You can perform OCR on this bitmap if needed
            }
        }
    }

    private fun startSpeechRecognition() {
        speechRecognitionManager.startListening()
        startButton.text = "Stop Listening"
        startButton.setOnClickListener {
            stopSpeechRecognition()
        }
    }

    private fun stopSpeechRecognition() {
        speechRecognitionManager.stopListening()
        startButton.text = "Start Listening"
        startButton.setOnClickListener {
            startSpeechRecognition()
        }
    }

    private fun startCamera() {
        cameraManager.startCamera(this, previewView) { recognizedText ->
            // Handle the recognized text from OCR
            runOnUiThread {
                resultTextView.text = "OCR Result: $recognizedText"
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSpeechRecognition()
                }
            }
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognitionManager.destroy()
        cameraManager.shutdown()
    }

    companion object {
        private const val PERMISSION_REQUEST_RECORD_AUDIO = 1
        private const val PERMISSION_REQUEST_CAMERA = 2
    }
}