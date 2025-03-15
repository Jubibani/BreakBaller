package com.google.ar.sceneform.samples.gltf.library.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.google.ar.sceneform.samples.gltf.R
import kotlinx.coroutines.delay


// Remove the import for MainActivity if it's not used

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    //sounds
    private lateinit var mediaPlayer: MediaPlayer

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("jna.nosys", "true");

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.startup)


        setContent {
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                visible = true
                delay(1000) // Duration of the splash screen
                mediaPlayer.start()
                delay(3000) // Duration of the splash screen
                visible = false
                startActivity(Intent(this@SplashActivity, InitializationActivity::class.java))
              /*  startActivity(Intent(this@SplashActivity, MainActivity::class.java))*/

                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
                delay(2000)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White), // Set the background color to white
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(2500)), // Fade in over 1.5 seconds
                    exit = fadeOut(animationSpec = tween(2500)) // Fade out over 1 second
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.splashscreentext),
                        contentDescription = "Splash Screen"
                    )
                }
            }
        }
    }
}