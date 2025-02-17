class MainFragment : Fragment(R.layout.fragment_main) {
    // ... (keep existing property declarations)

    private var lastRecognizedModel: String? = null
    private var isProcessingFrame = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // ... (keep existing initialization code)

        arFragment = (childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment).apply {
            setOnSessionConfigurationListener { session, config -> }
            setOnViewCreatedListener { arSceneView ->
                arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
                setupTextRecognition(arSceneView)
            }
        }
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun setupTextRecognition(arSceneView: ArSceneView) {
        arSceneView.scene.addOnUpdateListener { frameTime ->
            if (isProcessingFrame) return@addOnUpdateListener

            val frame = arSceneView.arFrame ?: return@addOnUpdateListener

            isProcessingFrame = true
            lifecycleScope.launch {
                try {
                    val image = frame.acquireCameraImage()
                    if (image != null) {
                        val inputImage = InputImage.fromMediaImage(image, 0)
                        textRecognizer.process(inputImage)
                            .addOnSuccessListener { visionText ->
                                for (block in visionText.textBlocks) {
                                    for (line in block.lines) {
                                        val recognizedText = line.text.trim()
                                        if (recognizableModels.contains(recognizedText) && recognizedText != lastRecognizedModel) {
                                            lastRecognizedModel = recognizedText
                                            Log.d("TextRecognition", "Model detected: $recognizedText")
                                            showToast("'$recognizedText' detected! Finding a surface to render the model.")
                                            tryPlaceModel(recognizedText, frame)
                                            break
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("TextRecognition", "Text recognition failed", e)
                            }
                            .addOnCompleteListener {
                                image.close()
                                isProcessingFrame = false
                            }
                    } else {
                        isProcessingFrame = false
                    }
                } catch (e: Exception) {
                    Log.e("TextRecognition", "Error processing frame", e)
                    isProcessingFrame = false
                }
            }
        }
    }

    private fun tryPlaceModel(modelName: String, frame: Frame) {
        val hitResult = frame.hitTest(frame.width / 2f, frame.height / 2f)
        hitResult.firstOrNull()?.let { hit ->
            if (hit.trackable is Plane && hit.trackable.trackingState == TrackingState.TRACKING) {
                placeModel(hit.createAnchor(), modelName)
            } else {
                Log.d("ModelPlacement", "No suitable surface found for $modelName")
            }
        } ?: Log.d("ModelPlacement", "No hit result for $modelName")
    }

    private fun placeModel(anchor: Anchor, modelName: String) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)

        val model = models[modelName]
        val modelView = modelViews[modelName]

        if (model == null || modelView == null) {
            Log.e("ModelRendering", "Model or ModelView for $modelName is null")
            return
        }

        TransformableNode(arFragment.transformationSystem).apply {
            setParent(anchorNode)
            renderable = model
            select()

            // Add info node as a child of the model node
            Node().apply {
                setParent(this@apply)
                renderable = modelView
                localPosition = Vector3(0f, 1f, 0f)
            }
        }

        Log.d("ModelRendering", "Model placed: $modelName")
        playSound(R.raw.popup)
        vibrate()
    }

    // ... (keep existing utility functions like showToast, playSound, vibrate, etc.)
}