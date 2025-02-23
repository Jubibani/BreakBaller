import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.gorisse.thomas.sceneform.scene.await
import kotlinx.coroutines.launch

class LibraryFragment : Fragment(R.layout.fragment_main) {

    private lateinit var arFragment: ArFragment
    private val arSceneView get() = arFragment.arSceneView
    private val scene get() = arSceneView.scene

    private var model: Renderable? = null
    private var modelView: ViewRenderable? = null

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var on: MediaPlayer
    private lateinit var off: MediaPlayer

    private lateinit var infoButton: FloatingActionButton
    private var isInfoVisible = false

    private var anchorNode: AnchorNode? = null
    private var infoNode: Node? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(context, R.raw.popup)
        on = MediaPlayer.create(context, R.raw.on)
        off = MediaPlayer.create(context, R.raw.off)

        // Initialize views
        infoButton = view.findViewById(R.id.infoButton)
        infoButton.setOnClickListener {
            toggleInfoVisibility()
        }

        arFragment = (childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment).apply {
            setOnSessionConfigurationListener { session, config ->
                // Modify the AR session configuration here if needed
            }
            setOnViewCreatedListener { arSceneView ->
                arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL)
            }
            setOnTapArPlaneListener(::onTapPlane)
        }

        lifecycleScope.launch {
            loadModels()
        }
    }

    private suspend fun loadModels() {
        model = ModelRenderable.builder()
            .setSource(context, Uri.parse("models/your_model.glb"))
            .setIsFilamentGltf(true)
            .await()
        modelView = ViewRenderable.builder()
            .setView(context, R.layout.your_model_info)
            .await()
    }

    private fun onTapPlane(hitResult: HitResult, plane: Plane, motionEvent: MotionEvent) {
        if (model == null || modelView == null) return

        // Create the Anchor
        val anchor = hitResult.createAnchor()
        anchorNode = AnchorNode(anchor).apply {
            setParent(arFragment.arSceneView.scene)
        }

        // Create the transformable model and add it to the anchor
        val modelNode = TransformableNode(arFragment.transformationSystem).apply {
            setParent(anchorNode)
            renderable = model
            select()
        }

        // Create the info node and add it to the model node
        infoNode = Node().apply {
            setParent(modelNode)
            renderable = modelView
            isEnabled = isInfoVisible
            localPosition = Vector3(0f, 1f, 0f)  // Adjust this to position the info relative to the model
        }

        playRenderSound()
        vibrate()
    }

    private fun toggleInfoVisibility() {
        isInfoVisible = !isInfoVisible
        infoNode?.isEnabled = isInfoVisible
        if (isInfoVisible) {
            onSound()
        } else {
            offSound()
        }
        vibrate()
    }

    private fun playRenderSound() {
        mediaPlayer.start()
    }

    private fun onSound() {
        on.start()
    }

    private fun offSound() {
        off.start()
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        on.release()
        off.release()
    }
}
