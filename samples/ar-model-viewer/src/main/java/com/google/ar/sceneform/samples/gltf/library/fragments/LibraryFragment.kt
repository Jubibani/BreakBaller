import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.ModelDao
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private lateinit var arFragment: ArFragment
    private lateinit var modelDao: ModelDao
    private var modelName: String? = null
    private var model: ModelRenderable? = null
    private var isModelPlaced = false
    private var mediaPlayer: MediaPlayer? = null
    private var currentModel: ModelEntity? = null

    private var isInfoVisible = false
    private var infoNode: Node? = null
    private lateinit var onSound: MediaPlayer
    private lateinit var offSound: MediaPlayer
    private lateinit var infoButton: FloatingActionButton
    private var anchorNode: AnchorNode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        modelName = arguments?.getString("modelName")
        modelDao = AppDatabase.getDatabase(requireContext(), lifecycleScope).modelDao()

        arFragment = childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        infoButton = view.findViewById(R.id.infoButton)
        infoButton.setOnClickListener { toggleInfo() }
        infoButton.visibility = View.GONE  // Initially hidden

        modelName?.let { loadModel(it) }
        setupTapToRender()

        onSound = MediaPlayer.create(requireContext(), R.raw.on)
        offSound = MediaPlayer.create(requireContext(), R.raw.off)

        return view
    }

    private fun loadModel(modelName: String) {
        lifecycleScope.launch {
            currentModel = modelDao.getModelByName(modelName)
            currentModel?.let { model ->
                val modelPath = model.modelPath
                val soundResId = model.interactionSoundResId

                mediaPlayer = MediaPlayer.create(requireContext(), soundResId)

                ModelRenderable.builder()
                    .setSource(requireContext(), Uri.parse(modelPath))
                    .setIsFilamentGltf(true)
                    .build()
                    .thenAccept { modelRenderable ->
                        this@LibraryFragment.model = modelRenderable
                        Log.d("LibraryFragment", "Model loaded: $modelName")
                    }
                    .exceptionally { throwable ->
                        Log.e("LibraryFragment", "Error loading model: ", throwable)
                        null
                    }
            }
        }
    }

    private fun setupTapToRender() {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            if (isModelPlaced) return@setOnTapArPlaneListener

            val model = this.model ?: return@setOnTapArPlaneListener

            val anchor = hitResult.createAnchor()
            placeModel(anchor, model)

            isModelPlaced = true
            infoButton.visibility = View.VISIBLE
        }
    }

    private fun placeModel(anchor: Anchor, modelRenderable: ModelRenderable) {
        anchorNode = AnchorNode(anchor)
        anchorNode?.setParent(arFragment.arSceneView.scene)

        val transformableNode = TransformableNode(arFragment.transformationSystem)
        transformableNode.setParent(anchorNode)
        transformableNode.renderable = modelRenderable

        // Ensure the model is placed on the plane
        transformableNode.localPosition = Vector3(0f, 0f, 0f)

        transformableNode.select()

        // Play sound when the model is rendered
        mediaPlayer?.start()

        transformableNode.setOnTapListener { _, _ ->
            mediaPlayer?.start()
        }

        // Create info node after placing the model
        createInfoNode()
    }

    private fun toggleInfo() {
        isInfoVisible = !isInfoVisible
        if (isInfoVisible) {
            showInfo()
            onSound.start()
        } else {
            hideInfo()
            offSound.start()
        }
    }

    private fun showInfo() {
        infoNode?.isEnabled = true
    }

    private fun hideInfo() {
        infoNode?.isEnabled = false
    }

    private fun createInfoNode() {
        currentModel?.let { model ->
            Log.d("LibraryFragment", "Creating info node for ${model.name} with layout ${model.layoutResId}")
            ViewRenderable.builder()
                .setView(context, model.layoutResId)
                .build()
                .thenAccept { viewRenderable: ViewRenderable ->
                    infoNode = Node().apply {
                        setParent(anchorNode)
                        // Position the info above the model
                        localPosition = Vector3(0f, 1f, 0f)
                        localScale = Vector3(0.5f, 0.5f, 0.5f)
                        renderable = viewRenderable
                        isEnabled = false
                    }
                }
                .exceptionally { throwable: Throwable ->
                    Log.e("LibraryFragment", "Error creating info view: ", throwable)
                    null
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        onSound.release()
        offSound.release()
    }
}