package com.google.ar.sceneform.samples.gltf.library.gallery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.screens.LibraryActivity

class HeartActivity : AppCompatActivity(R.layout.activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar).apply {
//            title = ""
//        })

        supportFragmentManager.commit {
            add(R.id.containerFragment, HeartFragment::class.java, Bundle())
        }

        // Set up the back button
        findViewById<FloatingActionButton>(R.id.backButton).setOnClickListener {
            // Navigate back to LibraryActivity
            val intent = Intent(this, LibraryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}