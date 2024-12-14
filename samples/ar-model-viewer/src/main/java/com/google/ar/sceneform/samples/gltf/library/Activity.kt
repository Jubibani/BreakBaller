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
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var refreshButton: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.back)
        refreshButton = MediaPlayer.create(this, R.raw.refresh)


        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(R.id.containerFragment, MainFragment::class.java, Bundle())
            }
        }

        findViewById<FloatingActionButton>(R.id.backButton).setOnClickListener {
            mediaPlayer.start()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<FloatingActionButton>(R.id.refreshButton).setOnClickListener {
            refreshButton.start()
            restartFragment()
        }
    }

    private fun restartFragment() {
        supportFragmentManager.commit {
            replace(R.id.containerFragment, MainFragment::class.java, Bundle())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        refreshButton.release()
    }
}