package com.google.ar.sceneform.samples.gltf.library.screens

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import com.google.ar.sceneform.samples.gltf.R
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity

class CustomUCropActivity : UCropActivity() {

    private lateinit var confirmSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        confirmSound = MediaPlayer.create(this, R.raw.flip) // Replace with your sound file
    }

    override fun onStop() {
        super.onStop()
        confirmSound.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            confirmSound.start() // Play sound when crop is confirmed
        }
    }
}