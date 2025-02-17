package com.google.ar.sceneform.samples.gltf.library

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
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
    private val TOAST_COOLDOWN_MS = 20000 // 9 seconds cooldown


    private lateinit var modelDao: ModelDao
    private val recognizableModelNames = mutableListOf<String>()

    private val models = mutableMapOf<String, Renderable?>()
    private val modelViews = mutableMapOf<String, ViewRenderable?>()
    private val modelInfoMap = mutableMapOf<String, ModelEntity>()  // Store fetched models

    // 🔥 Instead of lateinit, initialize LiveData properly
    private val modelLiveData: LiveData<List<ModelEntity>> by lazy {
        AppDatabase.getDatabase(requireContext(), lifecycleScope).modelDao().getAllModels()
    }



    //sounds
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var ping: MediaPlayer
    private lateinit var on: MediaPlayer
    private lateinit var off: MediaPlayer

    private var pingJob: Job? = null
    private var lastPingTime = 0L
    private val PING_DEBOUNCE_TIME = 2000L // 2 seconds


    //hiding
    private lateinit var infoButton: FloatingActionButton
    private var isInfoVisible = false

    //restart
    private lateinit var restartButton: FloatingActionButton


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

            recognizableModelNames.clear() // 🛠️ Update recognizable models
            recognizableModelNames.addAll(modelsList.map { it.name })

            preloadModels()

            preloadModels()
        }




        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(context, R.raw.popup)
        ping = MediaPlayer.create(context, R.raw.sonarping)
        on = MediaPlayer.create(context, R.raw.on)
        off = MediaPlayer.create(context, R.raw.off)

        //initialize views
        infoButton = view.findViewById(R.id.infoButton)
        // Set up infoButton click listener
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
        val minProcessingIntervalMs = 1000 // Process at most every


        arSceneView.scene.addOnUpdateListener { frameTime ->
            val currentTimeMs = System.currentTimeMillis()
            if (currentTimeMs - lastProcessingTimeMs < minProcessingIntervalMs) {
                return@addOnUpdateListener
            }

            val frame = arSceneView.arFrame ?: return@addOnUpdateListener

            lifecycleScope.launch {
                try {
                    val image = frame.acquireCameraImage()
                    if (image != null) {
                        val inputImage = InputImage.fromMediaImage(image, 0)
                        textRecognizer.process(inputImage)
                            .addOnSuccessListener { visionText ->
                                for (modelName in recognizableModelNames) { // ✅ Use the updated list
                                    if (visionText.text.contains(modelName, ignoreCase = true)) {
                                        if (!isModelPlaced) {
                                            vibrate()
                                            startRepeatingPing() // Start repeating ping
                                            if (currentTimeMs - lastToastTime > TOAST_COOLDOWN_MS) {
                                                showToast("'$modelName' detected! Find a Surface to Render the model.")
                                                Log.d("TextRecognition", "Model detected: $modelName")
                                                lastToastTime = currentTimeMs
                                            }
                                            renderModelOnSurface(modelName)
                                            break
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
                    }
                } catch (e: Exception) {
                    Log.e("TextRecognition", "Error processing frame", e)
                }
            }

        }
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



    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        on.release()
        off.release()
    }
}

//Tap Functionality

//    private fun onTapPlane(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
//        if (model == null || modelView == null) {
//            Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Create the Anchor.
//        scene.addChild(AnchorNode(hitResult.createAnchor()).apply {
//            // Create the transformable model and add it to the anchor.
//            addChild(TransformableNode(arFragment.transformationSystem).apply {
//                renderable = model
//                renderableInstance.setCulling(false)
//                renderableInstance.animate(true).start()
//                // Add the View
//                addChild(Node().apply {
//                    // Define the relative position
//                    localPosition = Vector3(0.0f, 1f, 0.0f)
//                    localScale = Vector3(0.7f, 0.7f, 0.7f)
//                    renderable = modelView
//                })
//            })
//        })
//    }