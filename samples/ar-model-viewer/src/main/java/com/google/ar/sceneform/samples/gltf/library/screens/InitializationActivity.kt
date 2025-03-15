package com.google.ar.sceneform.samples.gltf.library.screens

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.App
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class InitializationActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("jna.nosys", "true")
        System.setProperty("jna.boot.library.path", applicationInfo.nativeLibraryDir)

        setContentView(R.layout.activity_initialization)
        progressBar = findViewById(R.id.progressBar)

        Log.d("InitializationActivity", "Starting model file copy")
        copyModelFiles()

        val modelPath = File(getExternalFilesDir(null), "vosk-model-small-en-us-0.15").absolutePath
        if (File(modelPath).exists()) {
            Log.d("InitializationActivity", "Model files found at $modelPath")
            initializeModel(modelPath)
        } else {
            Log.e("InitializationActivity", "Model files not found at $modelPath")
        }

        val voskSpeechRecognitionHelper = (application as App).voskSpeechRecognitionHelper

        voskSpeechRecognitionHelper.setProgressCallback { progress ->
            progressBar.progress = progress
        }

        voskSpeechRecognitionHelper.setModelInitializedCallback {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Model loaded successfully", Toast.LENGTH_SHORT).show()
            Log.d("InitializationActivity", "Model loaded successfully")
            startMainActivity()
        }

        if (!voskSpeechRecognitionHelper.isModelInitialized()) {
            progressBar.visibility = View.VISIBLE
            Log.d("InitializationActivity", "Waiting for model to be initialized")
        } else {
            Log.d("InitializationActivity", "Model already initialized")
            startMainActivity()
        }
    }

    @OptIn(UnstableApi::class) private fun initializeModel(modelPath: String) {
        try {
            Log.d("InitializationActivity", "Initializing model at $modelPath")
            val model = Model(modelPath)
            val recognizer = Recognizer(model, 16000.0f)
            (application as App).voskSpeechRecognitionHelper.initializeModel(model)
            Log.d("InitializationActivity", "Model initialized successfully")
        } catch (e: Exception) {
            Log.e("InitializationActivity", "Error initializing model: ${e.message}")
            e.printStackTrace()
        }
    }

    @OptIn(UnstableApi::class) private fun copyModelFiles() {
        val assetManager = assets
        val modelDir = File(getExternalFilesDir(null), "vosk-model-small-en-us-0.15")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }

        try {
            copyAssetFolder(assetManager, "model/vosk-model-small-en-us-0.15", modelDir)
        } catch (e: IOException) {
            Log.e("InitializationActivity", "Error copying model files: ${e.message}")
            e.printStackTrace()
        }
    }

    // Recursively copy all files and subdirectories
    private fun copyAssetFolder(assetManager: AssetManager, assetPath: String, destDir: File) {
        val files = assetManager.list(assetPath) ?: return
        for (filename in files) {
            val file = File(destDir, filename)
            if (assetManager.list("$assetPath/$filename")?.isNotEmpty() == true) {
                file.mkdirs()
                copyAssetFolder(assetManager, "$assetPath/$filename", file)
            } else {
                val inputStream = assetManager.open("$assetPath/$filename")
                val outputStream = FileOutputStream(file)
                copyFile(inputStream, outputStream)
                inputStream.close()
                outputStream.close()
            }
        }
    }

    private fun copyFile(inStream: InputStream, outStream: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (inStream.read(buffer).also { read = it } != -1) {
            outStream.write(buffer, 0, read)
        }
    }

    @OptIn(UnstableApi::class) private fun startMainActivity() {
        Log.d("InitializationActivity", "Starting MainActivity")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}