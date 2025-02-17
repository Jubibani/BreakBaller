import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.ModelDao
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private lateinit var arFragment: ArFragment
    private lateinit var modelDao: ModelDao
    private var modelName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        // Retrieve the model name from the Bundle
        modelName = arguments?.getString("modelName")

        // Initialize the database
        modelDao = AppDatabase.getDatabase(requireContext(), lifecycleScope).modelDao()

        // Find the AR fragment inside the layout
        arFragment = childFragmentManager.findFragmentById(R.id.library_ar_fragment) as? ArFragment
            ?: throw NullPointerException("ARFragment not found. Ensure it's properly defined in the layout.")

        // Load and render the model
        modelName?.let { loadAndRenderModel(it) }

        return view
    }
    private fun loadAndRenderModel(modelName: String) {
        lifecycleScope.launch {
            val modelDao = AppDatabase.getDatabase(requireContext(), lifecycleScope).modelDao()
            val modelEntity = modelDao.getModelByName(modelName) // Fetch model from database

            modelEntity?.let {
                val modelPath = it.modelPath
                ModelRenderable.builder()
                    .setSource(requireContext(), Uri.parse("file:///android_asset/$modelPath"))
                    .setIsFilamentGltf(true)
                    .build()
                    .thenAccept { modelRenderable ->
                        val node = Node().apply { renderable = modelRenderable }
                        arFragment?.arSceneView?.scene?.addChild(node)
                    }
                    .exceptionally { throwable ->
                        Log.e("LibraryFragment", "Error loading model: ", throwable)
                        null
                    }
            }
        }
    }

}
