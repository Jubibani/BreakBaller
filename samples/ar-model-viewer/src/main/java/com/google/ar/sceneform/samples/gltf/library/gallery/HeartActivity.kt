package com.google.ar.sceneform.samples.gltf.library.gallery

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.screens.LibraryActivity

class HeartActivity : AppCompatActivity(R.layout.activity) {
    private var mediaPlayer: MediaPlayer? = null
    private  var refreshButton: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.back)
        refreshButton = MediaPlayer.create(this, R.raw.refresh)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(R.id.containerFragment, HeartFragment::class.java, Bundle())
            }
        }

        setupBackButton()
        setupRefreshButton()
    }

    private fun setupBackButton() {
        findViewById<FloatingActionButton>(R.id.backButton).setOnClickListener {
            playSound()
            navigateToLibrary()
        }
    }

    private fun setupRefreshButton() {
        findViewById<FloatingActionButton>(R.id.refreshButton).setOnClickListener {
            playRefreshSound()
            restartFragment()
        }
    }

    private fun navigateToLibrary() {
        val intent = Intent(this, LibraryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun restartFragment() {
        supportFragmentManager.commit {
            replace(R.id.containerFragment, HeartFragment::class.java, Bundle())
        }
    }

    private fun playSound() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
            }
        }
    }

    private fun playRefreshSound() {
        refreshButton?.let { player ->
            if (!player.isPlaying) {
                player.start()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        refreshButton?.release()
        mediaPlayer = null
    }
}