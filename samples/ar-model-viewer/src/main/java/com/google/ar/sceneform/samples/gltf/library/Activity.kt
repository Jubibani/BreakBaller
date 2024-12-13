package com.google.ar.sceneform.samples.gltf.library

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.screens.MainActivity

class Activity : AppCompatActivity(R.layout.activity) {
    //sounds
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.back)


//        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar).apply {
//            title = ""
//        })

        supportFragmentManager.commit {
            add(R.id.containerFragment, MainFragment::class.java, Bundle())
        }

        findViewById<FloatingActionButton>(R.id.backButton).setOnClickListener {
            mediaPlayer.start()
            // Navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}