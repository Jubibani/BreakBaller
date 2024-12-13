package com.google.ar.sceneform.samples.gltf.library.gallery

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.screens.LibraryActivity

class DigestiveActivity : AppCompatActivity(R.layout.activity) {

    //sounds
    private var backSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MediaPlayer
        backSound = MediaPlayer.create(this, R.raw.back)
        backSound?.setOnCompletionListener { mp ->
            mp.release()
        }

//        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar).apply {
//            title = ""
//        })

        supportFragmentManager.commit {
            add(R.id.containerFragment, DigestiveFragment::class.java, Bundle())
        }

        // Set up the back button
        findViewById<FloatingActionButton>(R.id.backButton).setOnClickListener {
            playBackSound()
            // Navigate back to LibraryActivity
            val intent = Intent(this, LibraryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
    //sound
    private fun playBackSound() {
        backSound?.let { sound ->
            if (!sound.isPlaying) {
                sound.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backSound?.release()
        backSound = null
    }
}