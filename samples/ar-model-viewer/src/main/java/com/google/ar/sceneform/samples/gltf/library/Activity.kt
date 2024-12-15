package com.google.ar.sceneform.samples.gltf.library

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.screens.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Activity : AppCompatActivity(R.layout.activity) {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var refreshButton: MediaPlayer
    private lateinit var switchButton: SwitchMaterial
    private lateinit var modeTextView: TextView
    private var isARMode = true
    private val mainFragment by lazy { MainFragment() }
    private val reciteFragment by lazy { ReciteFragment() }
    private var updateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.back)
        refreshButton = MediaPlayer.create(this, R.raw.refresh)

        switchButton = findViewById(R.id.switchButton)
        modeTextView = findViewById(R.id.modeTextView)

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            isARMode = !isChecked
            updateModeUI()
            debouncedUpdateScreen()
        }

        // Initialize the UI
        updateModeUI()

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(R.id.containerFragment, mainFragment)
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
            if (isARMode) {
                restartFragment()
            }
        }
    }

    private fun updateModeUI() {
        if (isARMode) {
            switchButton.setTextColor(ContextCompat.getColor(this, R.color.ar_mode_color))
            modeTextView.text = "Self-Paced Mode"
        } else {
            switchButton.setTextColor(ContextCompat.getColor(this, R.color.recitation_mode_color))
            modeTextView.text = "Recitation Mode"
        }
    }


    private fun debouncedUpdateScreen() {
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            delay(300) // Debounce for 300ms
            updateScreen()
        }
    }

    private fun updateScreen() {
        val newFragment: Fragment = if (isARMode) mainFragment else reciteFragment
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.containerFragment, newFragment)
            commitNow()
        }
        findViewById<FloatingActionButton>(R.id.refreshButton).isEnabled = isARMode
    }

    private fun restartFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.containerFragment, MainFragment())
            commitNow()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        refreshButton.release()
        updateJob?.cancel()
    }
}
