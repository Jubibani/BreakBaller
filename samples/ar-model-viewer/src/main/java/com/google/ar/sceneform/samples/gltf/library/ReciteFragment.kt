// ReciteFragment.kt
package com.google.ar.sceneform.samples.gltf.library

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.FocusMeteringAction
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.components.CustomUCropActivity
import com.google.ar.sceneform.samples.gltf.library.helpers.CameraHelper
import com.google.ar.sceneform.samples.gltf.library.helpers.VoskSpeechRecognitionHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream

class ReciteFragment : Fragment() {
    private lateinit var cameraHelper: CameraHelper
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var recognizedTextView: TextView
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

    //sound waves
    private lateinit var soundwaveAnimationView: LottieAnimationView
    private var previousRecognizedText: String? = null

    //speech recognition
    private lateinit var voskSpeechRecognitionHelper: VoskSpeechRecognitionHelper

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.RECORD_AUDIO] == true) {
            startCamera() // Start camera if both permissions are granted
        } else {
            Toast.makeText(context, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
        }
    }

    // Call this function where you need to request permissions
    private fun requestPermissions() {
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        soundwaveAnimationView = view.findViewById(R.id.soundwaveAnimationView)

        // Access voskSpeechRecognitionHelper from App
        val app = requireActivity().application as App
        voskSpeechRecognitionHelper = app.voskSpeechRecognitionHelper

        // Check if the model is initialized
        if (voskSpeechRecognitionHelper.isModelInitialized()) {
            // Start listening
            voskSpeechRecognitionHelper.startListening()
        } else {
            // Set a callback to start listening once the model is initialized
            voskSpeechRecognitionHelper.setModelInitializedCallback {
                voskSpeechRecognitionHelper.startListening()
            }
        }

        try {
            previewView = view.findViewById(R.id.previewView) ?: throw NullPointerException("PreviewView not found")
            captureButton = view.findViewById(R.id.captureButton) ?: throw NullPointerException("Capture button not found")
            imageView = view.findViewById(R.id.imageView) ?: throw NullPointerException("ImageView not found")
            textOverlay = view.findViewById(R.id.textOverlay) ?: throw NullPointerException("TextOverlay not found")
            recognizedTextView = view.findViewById(R.id.recognizedTextView) ?: throw NullPointerException("RecognizedTextView not found")

            textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            cameraHelper = CameraHelper(requireContext())

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
                voskSpeechRecognitionHelper.stopListening() // Stop listening when close button is pressed
            }

            //switch
            switchButton = requireActivity().findViewById(R.id.switchButton)

            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requestPermissions()
            }

            // take camera and perform text recognition and speech recognition
            captureButton.setOnClickListener {
                cameraSound.start() // Play camera sound
                takePhoto()
            } ?: throw NullPointerException("Start reciting button not found")

            setupTouchListeners()

            // Observe the recognized text and update the UI
            voskSpeechRecognitionHelper.spokenText.observe(viewLifecycleOwner, Observer { recognizedText ->
                Log.d("SpeechRecognition", "Updating UI with text: $recognizedText")
                recognizedTextView.text = recognizedText ?: "" // Display recognized speech
            })

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
        cameraHelper.camera?.cameraControl?.startFocusAndMetering(action)
    }

    private fun startCamera() {
        cameraHelper.startCamera(viewLifecycleOwner, previewView) { recognizedText ->
            Log.d("TextRecognition", "Detected text: $recognizedText")
        }
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

    private fun takePhoto() {
        cameraHelper.takePhoto { bitmap ->
            val rotatedBitmap = rotateBitmapByFixedAngle(bitmap)
            val uri = saveBitmapToCache(rotatedBitmap)
            startCropActivity(uri)
        }
    }

    private fun rotateBitmapByFixedAngle(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            postRotate(90f) // Always rotate by 90 degrees
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // Handle image orientation
                val rotatedBitmap = rotateBitmapByExifData(it, bitmap)

                // Recycle previous bitmap if exists
                capturedBitmap?.recycle()
                capturedBitmap = rotatedBitmap

                // Adjust ImageView scaleType
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
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

    private fun rotateBitmapByExifData(uri: Uri, bitmap: Bitmap): Bitmap {
        val exifInterface = ExifInterface(requireContext().contentResolver.openInputStream(uri)!!)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
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

    private fun showCloseButton() {
        closeButton.visibility = View.VISIBLE
    }

    private fun hideCloseButton() {
        closeButton.visibility = View.GONE
    }

    private fun startReciting() {
        recognizedText?.let { text ->
            voskSpeechRecognitionHelper.startListening()
            Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performOCROnCapturedImage() {
        val bitmap = capturedBitmap ?: return
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val image = InputImage.fromBitmap(mutableBitmap, 0)
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                recognizedText = visionText
                drawTextBoundingBoxes(mutableBitmap, visionText)
                startReciting() // Start speech recognition after OCR is complete
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

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onPause() {
        super.onPause()
        voskSpeechRecognitionHelper.stopListening() // Stop listening when fragment is paused
    }

    override fun onStop() {
        super.onStop()
        voskSpeechRecognitionHelper.stopListening() // Stop listening when fragment is stopped
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSound.release()
        disregardSound.release()
        refreshSound.release()
        cameraHelper.shutdown()
        voskSpeechRecognitionHelper.destroy()
        soundwaveAnimationView.cancelAnimation()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
}