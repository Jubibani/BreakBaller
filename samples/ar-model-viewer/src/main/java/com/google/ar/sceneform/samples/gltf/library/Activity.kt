package com.google.ar.sceneform.samples.gltf.library

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
    private lateinit var backButton: MediaPlayer
    private lateinit var refreshSound: MediaPlayer
    private lateinit var switchSound: MediaPlayer

    private lateinit var switchButton: SwitchMaterial
    private lateinit var modeTextView: TextView
    private lateinit var refreshButton: FloatingActionButton
    private var isARMode = true
    private val mainFragment by lazy { MainFragment() }
    private val reciteFragment by lazy { ReciteFragment() }
    private var updateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backButton = MediaPlayer.create(this, R.raw.back)
        refreshSound = MediaPlayer.create(this, R.raw.refresh)
        switchSound = MediaPlayer.create(this, R.raw.on)
        switchButton = findViewById(R.id.switchButton)
        modeTextView = findViewById(R.id.modeTextView)
        refreshButton = findViewById(R.id.refreshButton)

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            isARMode = !isChecked
            updateModeUI()
            debouncedUpdateScreen()
            switchSound.start()
        }

        // Initialize the UI
        updateModeUI()

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(R.id.containerFragment, mainFragment)
            }
        }

        findViewById<FloatingActionButton>(R.id.backButton).setOnClickListener {
            backButton.start()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        refreshButton.setOnClickListener {
            refreshSound.start()
            if (isARMode) {
                restartFragment()
            }
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)
                if (f is ReciteFragment) {
                    refreshButton.visibility = View.GONE
                } else {
                    refreshButton.visibility = View.VISIBLE
                }
            }
        }, true)
    }

    private fun updateModeUI() {
        if (isARMode) {
            switchButton.thumbTintList = ContextCompat.getColorStateList(this, R.color.purple_500)
            switchButton.trackTintList = ContextCompat.getColorStateList(this, R.color.purple_500)
            modeTextView.text = "Self-Paced Mode"
        } else {
            switchButton.thumbTintList = ContextCompat.getColorStateList(this, R.color.gold)
            switchButton.trackTintList = ContextCompat.getColorStateList(this, R.color.gold)
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
        refreshButton.isEnabled = isARMode
    }

    private fun restartFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.containerFragment, MainFragment())
            commitNow()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backButton.release()
        refreshSound.release()
        switchSound.release()
        updateJob?.cancel()
    }
}