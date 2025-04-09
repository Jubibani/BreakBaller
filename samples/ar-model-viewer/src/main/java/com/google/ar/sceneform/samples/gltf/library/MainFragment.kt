package com.google.ar.sceneform.samples.gltf.library

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.media.Image
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.ModelDao
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity
import com.google.ar.sceneform.samples.gltf.library.helpers.MediaImageToBitmapHelper
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.gorisse.thomas.sceneform.scene.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var arFragment: ArFragment
    private val arSceneView get() = arFragment.arSceneView
    private val scene get() = arSceneView.scene

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var isModelPlaced = false
    private var lastToastTime = 0L
    private val TOAST_COOLDOWN_MS = 20000 // 20 seconds cooldown

    private lateinit var modelDao: ModelDao
    private val recognizableModelNames = mutableListOf<String>()

    private val models = mutableMapOf<String, Renderable?>()
    private val modelViews = mutableMapOf<String, ViewRenderable?>()
    private val modelInfoMap = mutableMapOf<String, ModelEntity>()  // Store fetched models


    // Instead of lateinit, initialize LiveData properly
    private val modelLiveData: LiveData<List<ModelEntity>> by lazy {
        AppDatabase.getDatabase(requireContext(), lifecycleScope).modelDao().getAllModels()
    }

    //sounds
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var ping: MediaPlayer
    private lateinit var on: MediaPlayer
    private lateinit var off: MediaPlayer
    private lateinit var riser: MediaPlayer
    private lateinit var powerdown: MediaPlayer
    private lateinit var equip: MediaPlayer
    private lateinit var unequip: MediaPlayer

    private var pingJob: Job? = null
    private var lastPingTime = 0L
    private val PING_DEBOUNCE_TIME = 2000L // 2 seconds

    //hiding
    private lateinit var infoButton: FloatingActionButton
    private var isInfoVisible = false

    //restart
    private lateinit var restartButton: FloatingActionButton

    // Declare magnifyingGlassNode and modelRenderable
    private var magnifyingGlassNode: Node? = null
    private var modelRenderable: ModelRenderable? = null
    // Display the cropped bitmap in the ImageView for debugging
    private lateinit var croppedImageView: ImageView

    //flags
    private var isTextRecognitionActive = false
    private var isRecognitionCancelled = false
    private var isRiserCancelled = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext(), CoroutineScope(Dispatchers.IO))
        modelDao = database.modelDao()

        arFragment = childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        modelLiveData.observe(viewLifecycleOwner) { modelsList ->
            if (modelsList.isEmpty()) {
                Log.e("MainFragment", "Database is empty! Models were not inserted.")
            } else {
                Log.d("MainFragment", "Loaded models: ${modelsList.map { it.name }}")
            }
            modelInfoMap.clear()
            modelsList.forEach { modelInfoMap[it.name] = it }

            recognizableModelNames.clear()
            recognizableModelNames.addAll(modelsList.map { it.name })

            preloadModels()
        }

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(context, R.raw.popup)
        ping = MediaPlayer.create(context, R.raw.sonarping)
        on = MediaPlayer.create(context, R.raw.on)
        off = MediaPlayer.create(context, R.raw.off)
        riser = MediaPlayer.create(context, R.raw.riser)
        powerdown = MediaPlayer.create(context, R.raw.powerdown)
        equip = MediaPlayer.create(context, R.raw.equip)
        unequip = MediaPlayer.create(context, R.raw.unequip)

        // Initialize views
        infoButton = view.findViewById(R.id.infoButton)
        infoButton.setOnClickListener {
            toggleInfoVisibility()
        }

        arFragment = (childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment).apply {
            setOnSessionConfigurationListener { session, config ->
                // Modify the AR session configuration here
            }
            setOnViewCreatedListener { arSceneView ->
                arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
                setupTextRecognition(arSceneView)
            }
        }

        // Magnifying glass button
        val magnifyingGlassButton: ImageButton = view.findViewById(R.id.magnifyingGlassButton)
        magnifyingGlassButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d("MainFragment", "Button pressed: showing magnifying glass")
                    showMagnifyingGlass()
                    startTextRecognition()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.d("MainFragment", "Button released: hiding magnifying glass")
                    hideMagnifyingGlass()
                    stopTextRecognition()
                    true
                }
                else -> false
            }
        }

        // Initialize croppedImageView
        croppedImageView = view.findViewById(R.id.croppedImageView)

        // Load the 3D model
        ModelRenderable.builder()
            .setSource(context, Uri.parse("file:///android_asset/models/realmagnifying.glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable -> modelRenderable = renderable }
            .exceptionally { throwable ->
                Log.e("MainFragment", "Unable to load Renderable.", throwable)
                null
            }
    }


    private fun showMagnifyingGlass() {
        equip.start()
        croppedImageView.visibility = View.VISIBLE

        croppedImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        croppedImageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = 700 // Adjust this value to move up(decrease) or down (increase)
        }


        if (magnifyingGlassNode == null) {
            magnifyingGlassNode = Node().apply {
                setParent(arFragment.arSceneView.scene.camera)
                localPosition = Vector3(0.0f, -0.1f, -0.3f)
                renderable = modelRenderable
            }
        } else {
            magnifyingGlassNode?.setParent(arFragment.arSceneView.scene.camera)
        }
        magnifyingGlassNode?.isEnabled = true
    }
    private fun hideMagnifyingGlass() {
        resetTextRecognition()
        unequip.start()
        croppedImageView.visibility = View.GONE

        magnifyingGlassNode?.setParent(null)
        magnifyingGlassNode?.isEnabled = false

    }

    private fun preloadModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            modelInfoMap.values.forEach { modelEntity ->
                models[modelEntity.name] = ModelRenderable.builder()
                    .setSource(context, Uri.parse(modelEntity.modelPath))
                    .setIsFilamentGltf(true)
                    .await()

                modelViews[modelEntity.name] = ViewRenderable.builder()
                    .setView(context, modelEntity.layoutResId)
                    .await()
            }
        }
    }


    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun setupTextRecognition(arSceneView: ArSceneView) {
        var lastProcessingTimeMs = 0L
        val minProcessingIntervalMs = 10 // Process at most every second

        arSceneView.scene.addOnUpdateListener { frameTime ->
            if (!isTextRecognitionActive || isRecognitionCancelled) return@addOnUpdateListener

            val currentTimeMs = System.currentTimeMillis()
            if (currentTimeMs - lastProcessingTimeMs < minProcessingIntervalMs) {
                return@addOnUpdateListener
            }

            val frame = arSceneView.arFrame ?: return@addOnUpdateListener

            lifecycleScope.launch {

                try {
                    val image = frame.acquireCameraImage()
                    val (croppedImage, croppedBitmap) = cropToViewfinder(image)
                    croppedImageView.setImageBitmap(croppedBitmap)

                    textRecognizer.process(croppedImage)
                        .addOnSuccessListener { visionText ->
                            val recognizedText = visionText.text.lowercase()
                            for (modelName in recognizableModelNames) {
                                if (recognizedText.contains(modelName.lowercase())) {

                                    // Monitor cancellation during riser playback
                                    if (isRecognitionCancelled) {
                                        cancelTextRecognition()
                                        return@addOnSuccessListener
                                    }
                                    // Start playing the riser sound
                                    riserSound()
                                    riser.setOnCompletionListener {

                                        if (!isModelPlaced) {
                                            powerdown.release()
                                            vibrate()
                                            startRepeatingPing() // Start repeating ping
                                            if (currentTimeMs - lastToastTime > TOAST_COOLDOWN_MS) {
                                                showToast("'$modelName' detected! Find a Surface to Render the model.")
                                                Log.d(
                                                    "TextRecognition",
                                                    "Model detected: $modelName"
                                                )
                                                lastToastTime = currentTimeMs
                                            }
                                            hideMagnifyingGlass()
                                            view?.findViewById<ImageButton>(R.id.magnifyingGlassButton)?.visibility =
                                                View.GONE
                                            renderModelOnSurface(modelName)

                                            return@setOnCompletionListener
                                        }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("TextRecognition", "Text recognition failed", exception)
                        }
                        .addOnCompleteListener {
                            image.close()
                        }

                    lastProcessingTimeMs = currentTimeMs
                } catch (e: Exception) {
                    Log.e("TextRecognition", "Error processing frame", e)
                }
            }
        }
    }


    private fun startTextRecognition() {
        isTextRecognitionActive = true
    }

    private fun stopTextRecognition() {
        isTextRecognitionActive = false
    }

    private fun resetTextRecognition() {
        isRecognitionCancelled = true
        isRiserCancelled = true

        if (::riser.isInitialized) {
            try {
                if (riser.isPlaying) {
                    riser.stop()
                }
            } catch (e: IllegalStateException) {
                Log.e("MainFragment", "MediaPlayer is in an invalid state during reset.", e)
            }
            riser.release()
        }

        // Reinitialize the MediaPlayer for future use
        riser = MediaPlayer.create(context, R.raw.riser)

        // Reset flags back to false after a short delay
        lifecycleScope.launch {
            delay(500) // Adjust delay as needed
            isRecognitionCancelled = false
            isRiserCancelled = false
        }
    }

    private fun cancelTextRecognition(){
        isRiserCancelled = true
        if (::riser.isInitialized) {
            try {
                if (riser.isPlaying) {
                    riser.stop()
                }
                if (!isModelPlaced) {
                    powerdown.start()
                }
                riser.release()
                riser = MediaPlayer.create(context, R.raw.riser) // Reinitialize
            } catch (e: IllegalStateException) {
                Log.e("TextRecognition", "MediaPlayer is in an invalid state.", e)
            }
        }
    }

    private fun cropToViewfinder(image: Image): Pair<InputImage, Bitmap> {
        val bitmap = MediaImageToBitmapHelper.convert(image)

        val width = bitmap.width
        val height = bitmap.height

        // Define zoom factor ( 6.0 means 6x zoom)
        val zoomFactor = 6.0f

        // Crop a smaller region from center to simulate zoom
        val cropSize = (Math.min(width, height) / zoomFactor).toInt()
        val centerX = width / 2
        val centerY = height / 2
        val left = (centerX - cropSize / 2).coerceAtLeast(0)
        val top = (centerY - cropSize / 2).coerceAtLeast(0)

        var croppedBitmap = Bitmap.createBitmap(bitmap, left, top, cropSize, cropSize)

        // Scale it up to simulate zoom
        val zoomedBitmap = Bitmap.createScaledBitmap(croppedBitmap, cropSize * zoomFactor.toInt(), cropSize * zoomFactor.toInt(), true)

        // Rotate after scaling
        val matrix = Matrix().apply { postRotate(90f) }
        val rotatedZoomedBitmap = Bitmap.createBitmap(
            zoomedBitmap, 0, 0,
            zoomedBitmap.width, zoomedBitmap.height,
            matrix, true
        )

        // Optional: mask into a circular preview
        val circularBitmap = Bitmap.createBitmap(rotatedZoomedBitmap.width, rotatedZoomedBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(circularBitmap)
        val paint = Paint().apply { isAntiAlias = true }
        val rect = Rect(0, 0, rotatedZoomedBitmap.width, rotatedZoomedBitmap.height)
        val rectF = RectF(rect)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(rotatedZoomedBitmap, rect, rect, paint)

        val inputImage = InputImage.fromBitmap(circularBitmap, 0)
        return Pair(inputImage, circularBitmap)
    }


    private fun renderModelOnSurface(modelName: String) {
        val modelEntity = modelInfoMap[modelName] ?: return // Get model from DB

        if (models[modelName] == null || modelViews[modelName] == null) {
            vibrate()
            pingSound()
            Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show()
            return
        }

        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            val frame = arFragment.arSceneView.arFrame ?: return@addOnUpdateListener
            val planes = frame.getUpdatedTrackables(Plane::class.java)

            for (plane in planes) {
                if (plane.trackingState == TrackingState.TRACKING && !isModelPlaced) {
                    val pose = plane.centerPose
                    val anchor = plane.createAnchor(pose)
                    placeModel(anchor, modelEntity)
                    isModelPlaced = true
                    break
                }
            }
        }
    }

    private fun placeModel(anchor: Anchor, modelEntity: ModelEntity) {
        val modelName = modelEntity.name
        val model = models[modelName] ?: return
        val modelView = modelViews[modelName] ?: return

        scene.addChild(AnchorNode(anchor).apply {
            addChild(TransformableNode(arFragment.transformationSystem).apply {
                renderable = model
                renderableInstance.setCulling(false)
                renderableInstance.animate(true).start()

                // Add InfoNode
                addChild(Node().apply {
                    name = "InfoNode"
                    localPosition = Vector3(0.0f, 1f, 0.0f)
                    localScale = Vector3(0.7f, 0.7f, 0.7f)
                    renderable = modelView
                    isEnabled = false
                })

                // Add tap listener for model interaction
                setOnTapListener { _, _ ->
                    playInteractionSound(modelEntity.interactionSoundResId)
                }
            })
        })

        playRenderSound()
        stopRepeatingPing()
        isModelPlaced = true
        infoButton.visibility = View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(200)
            }
        }
    }

    private fun toggleInfoVisibility() {
        isInfoVisible = !isInfoVisible
        scene.findByName("InfoNode")?.let { infoNode ->
            infoNode.isEnabled = isInfoVisible
            if (isInfoVisible) {
                onSound()
            } else {
                offSound()
            }
        }
        vibrate()
    }

    private fun playInteractionSound(soundResId: Int) {
        MediaPlayer.create(context, soundResId)?.apply {
            setOnCompletionListener { release() }
            start()
        }
    }

    private fun playRenderSound() {
        mediaPlayer.start()
    }

    private fun pingSound() {
        ping.start()
    }

    private fun startRepeatingPing() {
        // Cancel any existing ping job
        pingJob?.cancel()

        pingJob = lifecycleScope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastPingTime >= PING_DEBOUNCE_TIME) {
                    pingSound()
                    lastPingTime = currentTime
                }
                delay(PING_DEBOUNCE_TIME)
            }
        }
    }

    private fun stopRepeatingPing() {
        pingJob?.cancel()
        pingJob = null
        lastPingTime = 0L
    }

    private fun onSound() {
        on.start()
    }

    private fun offSound() {
        off.start()
    }

    private fun riserSound(){
        if (::riser.isInitialized) {
            try {
                riser.start()
            } catch (e: IllegalStateException) {
                Log.e("TextRecognition", "Failed to start MediaPlayer.", e)
                riser.release()
                riser = MediaPlayer.create(context, R.raw.riser) // Reinitialize
            }
        }
    }
    private fun unloadModels() {
        models.clear()
        modelViews.clear()
        modelRenderable = null
    }

    private fun switchToReciteFragment() {
        unloadModels()
        parentFragmentManager.beginTransaction()
            .replace(R.id.containerFragment, ReciteFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        on.release()
        off.release()
        riser.release()
        powerdown.release()
        equip.release()
        unequip.release()
    }


}