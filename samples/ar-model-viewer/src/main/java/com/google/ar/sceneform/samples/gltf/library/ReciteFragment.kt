package com.google.ar.sceneform.samples.gltf.library

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.helpers.SpeechRecognitionHelper
import com.google.ar.sceneform.samples.gltf.library.screens.CustomUCropActivity
import com.google.ar.sceneform.samples.gltf.library.screens.PracticeActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ReciteFragment : Fragment() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private var camera: androidx.camera.core.Camera? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var capturedBitmap: Bitmap? = null
    private lateinit var textRecognizer: com.google.mlkit.vision.text.TextRecognizer
    private lateinit var textOverlay: TextOverlay
    private var recognizedText: Text? = null

    //close
    private lateinit var closeButton: FloatingActionButton

    //sounds
    private lateinit var refreshSound: MediaPlayer
    private lateinit var cameraSound: MediaPlayer
    private lateinit var disregardSound: MediaPlayer

    //switch button
    private lateinit var switchButton: SwitchMaterial
    //speech recognition
    private lateinit var speechRecognitionHelper: SpeechRecognitionHelper
    private lateinit var startRecitingButton: FloatingActionButton

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            Toast.makeText(context, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recite, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            previewView = view.findViewById(R.id.previewView) ?: throw NullPointerException("PreviewView not found")
            captureButton = view.findViewById(R.id.captureButton) ?: throw NullPointerException("Capture button not found")
            imageView = view.findViewById(R.id.imageView) ?: throw NullPointerException("ImageView not found")
            textOverlay = view.findViewById(R.id.textOverlay) ?: throw NullPointerException("TextOverlay not found")

            cameraExecutor = Executors.newSingleThreadExecutor()
            textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            speechRecognitionHelper = SpeechRecognitionHelper(requireContext())

            // Initialize sounds
            cameraSound = MediaPlayer.create(requireContext(), R.raw.camera)
            disregardSound = MediaPlayer.create(requireContext(), R.raw.disregard)

            //close
            closeButton = view.findViewById(R.id.closeButton)
            refreshSound = MediaPlayer.create(requireContext(), R.raw.refresh)
            closeButton.setOnClickListener {

                resetCamera()
                hideCloseButton()
                showRecitationButtons() // Show all buttons for recitation mode
            }

            //switch
            switchButton = requireActivity().findViewById(R.id.switchButton)

            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
            }

            // take camera and perform text recognition and speech recognition
            captureButton.setOnClickListener {
                cameraSound.start() // Play camera sound
                takePhoto()
                startReciting()
            } ?: throw NullPointerException("Start reciting button not found")

            setupTouchListeners()

        } catch (e: NullPointerException) {
            Log.e("ReciteFragment", "Error initializing views: ${e.message}")
            Toast.makeText(context, "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("ReciteFragment", "Unexpected error: ${e.message}")
            Toast.makeText(context, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupTouchListeners() {
        previewView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                focusOnPoint(event.x, event.y)
            }
            true
        }

        imageView.setOnTouchListener { _, event ->
            if (capturedBitmap != null && recognizedText != null) {
                val x = event.x.toInt()
                val y = event.y.toInt()
                highlightTextAtPoint(x, y)
            }
            true
        }
    }

    private fun focusOnPoint(x: Float, y: Float) {
        val factory = previewView.meteringPointFactory
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        processImageProxy(imageProxy)
                    })
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(context, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun resetCamera() {
        disregardSound.start()

        // Delay the execution of the following code by 500 milliseconds
        Handler(Looper.getMainLooper()).postDelayed({
            imageView.visibility = View.GONE
            previewView.visibility = View.VISIBLE
            capturedBitmap = null
            recognizedText = null
            textOverlay.recognizedText = null  // Reset the recognized text in TextOverlay
            textOverlay.invalidate()  // Force redraw of the TextOverlay
            startCamera()  // Restart the camera
        }, 300) // 500 milliseconds delay
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Process the detected text in real-time
                    Log.d("TextRecognition", "Detected text: ${visionText.text}")
                }
                .addOnFailureListener { e ->
                    Log.e("TextRecognition", "Error recognizing text", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmap()
                    val rotatedBitmap = rotateBitmapIfRequired(bitmap)
                    val uri = saveBitmapToCache(rotatedBitmap)
                    startCropActivity(uri)
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Photo capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun startCropActivity(uri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "croppedImage.jpg"))
        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(100)
            setFreeStyleCropEnabled(true) // Enable free style cropping

            // Set the colors
            setToolbarColor(Color.parseColor("#451f7a")) // Dark Purple
            setStatusBarColor(Color.parseColor("#451f7a")) // Dark Purple
            setActiveControlsWidgetColor(Color.parseColor("#edb705")) // Gold
            setToolbarWidgetColor(Color.parseColor("#FFFFFF")) // White
            setRootViewBackgroundColor(Color.parseColor("#070308")) // Dark Grey
        }
        val intent = UCrop.of(uri, destinationUri)
            .withOptions(options)
            .getIntent(requireContext())
            .setClass(requireContext(), CustomUCropActivity::class.java)
        cropActivityResultLauncher.launch(intent)
    }

    private val cropActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let {
                val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(it))
                capturedBitmap = bitmap
                imageView.setImageBitmap(capturedBitmap)
                imageView.visibility = View.VISIBLE
                previewView.visibility = View.GONE

                // Hide all buttons except close button
                captureButton.visibility = View.GONE
                switchButton.visibility = View.GONE
                showCloseButton()

                performOCROnCapturedImage()
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            Toast.makeText(context, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateBitmapIfRequired(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            postRotate(90f) // Always rotate by 90 degrees
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }



    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "capturedImage.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(file)
    }

    // Add this function to show all buttons for recitation mode
    private fun showRecitationButtons() {
        captureButton.visibility = View.VISIBLE
        switchButton.visibility = View.VISIBLE
        // Add any other buttons that should be visible in recitation mode
    }

    //speech recognition
    private fun startReciting() {
        recognizedText?.let { text ->
            speechRecognitionHelper.setReferenceText(text.text)
            speechRecognitionHelper.startListening()
            Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
        }
    }


    private fun stopReciting() {
        speechRecognitionHelper.stopListening()
        val (mispronunciations, skippedWords, stutteredWords) = speechRecognitionHelper.getResults()
        // Start PracticeActivity with results
        val intent = Intent(requireContext(), PracticeActivity::class.java).apply {
            putStringArrayListExtra("mispronunciations", ArrayList(mispronunciations))
            putStringArrayListExtra("skippedWords", ArrayList(skippedWords))
            putStringArrayListExtra("stutteredWords", ArrayList(stutteredWords))
        }
        startActivity(intent)
    }

    private fun showCloseButton() {
        closeButton.visibility = View.VISIBLE
    }

    private fun hideCloseButton() {
        closeButton.visibility = View.GONE
    }

    private fun performOCROnCapturedImage() {
        val bitmap = capturedBitmap ?: return
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true) // Create a mutable copy
        val image = InputImage.fromBitmap(mutableBitmap, 0)
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                recognizedText = visionText
                drawTextBoundingBoxes(mutableBitmap, visionText)
                startReciting() // Start reciting after OCR is complete
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Text recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun drawTextBoundingBoxes(bitmap: Bitmap, visionText: Text) {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                line.boundingBox?.let { box ->
                    canvas.drawRect(box, paint)
                }
            }
        }

        imageView.setImageBitmap(bitmap)
    }

    private fun highlightTextAtPoint(x: Int, y: Int) {
        val visionText = recognizedText ?: return
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                if (line.boundingBox?.contains(x, y) == true) {
                    highlightText(line)
                    Toast.makeText(context, "Selected: ${line.text}", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
    }

    private fun highlightText(textLine: Text.Line) {
        val highlightedBitmap = capturedBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(highlightedBitmap)
        val paint = Paint().apply {
            color = Color.YELLOW
            alpha = 100
            style = Paint.Style.FILL
        }

        textLine.boundingBox?.let { box ->
            canvas.drawRect(box, paint)
        }

        imageView.setImageBitmap(highlightedBitmap)
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSound.release()
        disregardSound.release()
        refreshSound.release()
        cameraExecutor.shutdown()
        speechRecognitionHelper.destroy()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}