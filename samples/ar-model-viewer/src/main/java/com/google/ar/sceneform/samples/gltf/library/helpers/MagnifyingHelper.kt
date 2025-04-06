package com.google.ar.sceneform.samples.gltf.library.helpers

import android.net.Uri
import android.util.Log
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

class MagnifyingHelper(private val arFragment: ArFragment) {

    var modelRenderable: ModelRenderable? = null
        private set

    fun loadModel(modelUri: Uri) {
        ModelRenderable.builder()
            .setSource(arFragment.context, modelUri)
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable -> modelRenderable = renderable }
            .exceptionally { throwable ->
                Log.e("MagnifyingHelper", "Unable to load Renderable.", throwable)
                null
            }
    }
}