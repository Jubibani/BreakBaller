package com.google.ar.sceneform.samples.gltf.library.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.sceneform.samples.gltf.R

class SpeechRecognitionActivity : AppCompatActivity() {

    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var resultTextView: TextView
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_recognition)

        initializeViews()
        setupSpeechRecognition()
        setupListeners()
    }

    private fun initializeViews() {
        resultTextView = findViewById(R.id.resultTextView)
        startButton = findViewById(R.id.startButton)
    }

    private fun setupSpeechRecognition() {
        speechRecognitionManager = SpeechRecognitionManager(this)
        speechRecognitionManager.onResultsListener = { result ->
            resultTextView.text = result
        }
    }

    private fun setupListeners() {
        startButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_RECORD_AUDIO)
            } else {
                startSpeechRecognition()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognitionManager.destroy()
    }

    companion object {
        private const val PERMISSION_REQUEST_RECORD_AUDIO = 1
    }
}