package com.google.ar.sceneform.samples.gltf.library.screens

import com.google.ar.sceneform.samples.gltf.R
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3

class MagnifyingActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private var modelRenderable: ModelRenderable? = null
    private var magnifyingGlassNode: Node? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magnifying)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        // Load the 3D model
        ModelRenderable.builder()
            .setSource(this, Uri.parse("file:///android_asset/models/realmagnifying.glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable -> modelRenderable = renderable }
            .exceptionally { throwable ->
                Log.e("MagnifyingActivity", "Unable to load Renderable.", throwable)
                null
            }

        val magnifyingGlassButton: ImageButton = findViewById(R.id.magnifyingGlassButton)
        magnifyingGlassButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d("MagnifyingActivity", "Button pressed: showing magnifying glass")
                    showMagnifyingGlass()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.d("MagnifyingActivity", "Button released: hiding magnifying glass")
                    hideMagnifyingGlass()
                    true
                }
                else -> false
            }
        }
    }

    private fun showMagnifyingGlass() {
        if (magnifyingGlassNode == null) {
            magnifyingGlassNode = Node().apply {
                setParent(arFragment.arSceneView.scene.camera)
                localPosition = Vector3(0.0f, -0.1f, -0.3f) // Lower the magnifying glass by setting y to -0.1f
                renderable = modelRenderable
            }
        }
    }

    private fun hideMagnifyingGlass() {
        magnifyingGlassNode?.setParent(null)
        magnifyingGlassNode = null
    }
}