package com.google.ar.sceneform.samples.gltf.library.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun RefinedTextRecognitionScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    if (hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CameraPreview(
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor,
                onKeywordsDetected = { detectedKeyword ->
                    // Show a Toast notification for the keyword
                    Toast.makeText(context, "Detected: $detectedKeyword", Toast.LENGTH_SHORT).show()
                }
            )
        }
    } else {
        // Display a message when there’s no camera permission
        androidx.compose.material3.Text(
            text = "Camera permission is required to use text recognition.",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(16.dp)
        )
    }
}

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    onKeywordsDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Set up the preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Set up high-resolution ImageAnalysis for small text and fonts
                val textAnalyzer = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1920, 1080)) // High resolution for better recognition
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutor,
                            KeywordTextAnalyzer(
                                keywords = listOf("amphibians", "bacteria", "platypus", "digestive", "expose"),
                                onKeywordDetected = onKeywordsDetected
                            )
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    // Bind the preview and analysis use cases to the camera lifecycle
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        textAnalyzer
                    )

                    // Enable tap-to-focus functionality
                    enableTapToFocus(previewView, camera.cameraControl)
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@androidx.camera.core.ExperimentalGetImage
private class KeywordTextAnalyzer(
    private val keywords: List<String>,
    private val onKeywordDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Keep track of previously detected keywords to avoid duplicate notifications
    private val detectedKeywords = mutableSetOf<String>()

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val detectedText = visionText.text.lowercase()
                val currentKeywords = mutableSetOf<String>()

                // Check for keywords in the detected text
                for (keyword in keywords) {
                    if (keyword in detectedText) {
                        currentKeywords.add(keyword)
                        // Notify only if the keyword is newly detected
                        if (keyword !in detectedKeywords) {
                            onKeywordDetected(keyword)
                        }
                    }
                }

                // Update the set of detected keywords
                detectedKeywords.clear()
                detectedKeywords.addAll(currentKeywords)
            }
            .addOnFailureListener { e ->
                Log.e("KeywordTextAnalyzer", "Text recognition failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}

private fun enableTapToFocus(previewView: PreviewView, cameraControl: CameraControl) {
    previewView.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            val factory = previewView.meteringPointFactory
            val point = factory.createPoint(event.x, event.y)
            val action = FocusMeteringAction.Builder(point).build()
            cameraControl.startFocusAndMetering(action)
        }
        return@setOnTouchListener true
    }
}