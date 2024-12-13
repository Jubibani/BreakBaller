package com.google.ar.sceneform.samples.gltf.library

import android.content.Context
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
import androidx.lifecycle.lifecycleScope
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
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.gorisse.thomas.sceneform.scene.await
import kotlinx.coroutines.launch
import java.util.Locale

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var arFragment: ArFragment
    private val arSceneView get() = arFragment.arSceneView
    private val scene get() = arSceneView.scene

    private val models = mutableMapOf<String, Renderable?>()
    private var modelView: ViewRenderable? = null

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var isModelPlaced = false
    private var lastToastTime = 0L
    private val TOAST_COOLDOWN_MS = 3000 // 3 seconds cooldown

    private val recognizableModels = listOf("Amphibian", "Bacteria", "Digestive", "Platypus", "Heart")



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arFragment = (childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment).apply {
            setOnSessionConfigurationListener { session, config ->
                // Modify the AR session configuration here
            }
            setOnViewCreatedListener { arSceneView ->
                arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
                setupTextRecognition(arSceneView)
            }
        }

        lifecycleScope.launchWhenCreated {
            loadModels()
        }
    }

    private suspend fun loadModels() {
        for (modelName in recognizableModels) {
            models[modelName] = ModelRenderable.builder()
                .setSource(context, Uri.parse("models/${modelName.lowercase(Locale.ROOT)}.glb"))
                .setIsFilamentGltf(true)
                .await()
        }
        modelView = ViewRenderable.builder()
            .setView(context, R.layout.view_renderable_infos)
            .await()
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
                                for (modelName in recognizableModels) {
                                    if (visionText.text.contains(modelName, ignoreCase = true)) {
                                        if (!isModelPlaced) {
                                            vibrate()
                                            if (currentTimeMs - lastToastTime > TOAST_COOLDOWN_MS) {
                                                showToast("'$modelName' detected! Scanning for a surface...")
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
        if (models[modelName] == null || modelView == null) {
            vibrate()
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
                    placeModel(anchor, modelName)
                    isModelPlaced = true
                    break
                }
            }
        }
    }

    private fun placeModel(anchor: Anchor, modelName: String) {
        val model = models[modelName] ?: return

        scene.addChild(AnchorNode(anchor).apply {
            addChild(TransformableNode(arFragment.transformationSystem).apply {
                renderable = model
                renderableInstance.setCulling(false)
                renderableInstance.animate(true).start()
                addChild(Node().apply {
                    localPosition = Vector3(0.0f, 1f, 0.0f)
                    localScale = Vector3(0.7f, 0.7f, 0.7f)
                    renderable = modelView
                })
            })
        })
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