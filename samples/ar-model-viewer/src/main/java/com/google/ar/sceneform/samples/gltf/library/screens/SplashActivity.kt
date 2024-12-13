package com.google.ar.sceneform.samples.gltf.library.screens

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import com.google.ar.sceneform.samples.gltf.R


// Remove the import for MainActivity if it's not used

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                visible = true
                delay(3000) // Duration of the splash screen
                visible = false
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
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