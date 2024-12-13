package com.google.ar.sceneform.samples.gltf.library.screens

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.theme.AugmentEDTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {


    //sounds
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.ui)

        setContent {
            AugmentEDTheme {
                MainScreen(
                    isArSupported = true,
                    sensorX = 0f,
                    sensorY = 0f,

                    //sound
                    playButtonSound = { playButtonSound() }  // Pass this function to MainScreen
                )
            }
        }

    }

    //sound
    private fun playButtonSound() {
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}

val MinecraftFontFamily = FontFamily(
    Font(R.font.minecraftregular, FontWeight.Normal),
    Font(R.font.minecraftbold, FontWeight.Bold)
)

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    isArSupported: Boolean,
    sensorX: Float,
    sensorY: Float,
    playButtonSound: () -> Unit //sound
) {
    val context = LocalContext.current
    var showTextRecognition by remember { mutableStateOf(false) } // State to toggle screens

    // Background animation colors
    val infiniteTransition = rememberInfiniteTransition()
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF0D1B2A), // Deep Space Blue
        targetValue = Color(0xFF1B263B), // Darker Blue
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF1B263B), // Darker Blue
        targetValue = Color(0xFF415A77), // Aurora Blue
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFF415A77), // Aurora Blue
        targetValue = Color(0xFF778DA9), // Light Aurora
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Full-screen background with gradient
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(color1, color2, color3)))
    ) {
        ParticleBackground() // Your particle background effect

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App title animations
            AnimatedText(
                text = "Augment-ED",
                fontSize = 45,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = MinecraftFontFamily,
                startColor = Color(0xFFD4AF37),
                endColor = Color(0xFFE5C158)
            )
            AnimatedText(
                text = "Welcome!",
                fontSize = 24,
                fontWeight = FontWeight.Normal,
                fontFamily = MinecraftFontFamily,
                startColor = Color.White,
                endColor = Color(0xFF778DA9)
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Toggle between AR and Text Recognition screens
            if (showTextRecognition) {
                // Display the Text Recognition Screen
                Box(modifier = Modifier.fillMaxSize()) {
                    RefinedTextRecognitionScreen()
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Back to Main AR Menu Button
                AnimatedMaterialIconButton(
                    text = "Back to Main Menu",
                    icon = Icons.Filled.ArrowBack,
                    onClick = { showTextRecognition = false } // Switch back to the main menu
                )
            } else {
                // Main menu with AR and Library buttons
                if (isArSupported) {
                    AnimatedMaterialIconButton(
                        text = "Scan",
                        icon = Icons.Filled.QrCodeScanner,
                        fontFamily = MinecraftFontFamily,
                        onClick = {
                            playButtonSound()
                            // Start HelloAR activity (AR scanning)
                            val intent = Intent(context, com.google.ar.sceneform.samples.gltf.library.Activity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                AnimatedMaterialIconButton(
                    text = "Library",
                    icon = Icons.Filled.LibraryBooks,
                    fontFamily = MinecraftFontFamily,
                    onClick = {
                        playButtonSound()
                        val intent = Intent(context, LibraryActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))

                AnimatedMaterialIconButton(
                    text = "Practice",
                    icon = Icons.Filled.School,
                    fontFamily = MinecraftFontFamily,
                    onClick = {
                        playButtonSound()
                        // Start a practice mode (implement or customize this later)
                    }
                )
            }
        }
    }
}

@Composable
fun ParticleBackground() {
    val maxParticles = 500 // Limit the number of particles
    val particles = remember { mutableStateListOf<Particle>() }
    val random = remember { Random.Default }

    // Add new particles periodically, but respect the maximum
    LaunchedEffect(Unit) {
        while (true) {
            if (particles.size < maxParticles) {
                particles.add(Particle(random))
            }
            delay(50L) // Adjust for particle generation frequency
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            // Update particle properties
            particle.update()

            // Remove particles if they complete their lifecycle
            if (particle.alpha <= 0f || particle.size <= 0f) {
                iterator.remove()
            } else {
                // Draw the particle
                drawCircle(
                    color = Color.White.copy(alpha = particle.alpha),
                    radius = particle.size,
                    center = particle.position
                )
            }
        }
    }
}

// Particle class to manage properties
data class Particle(
    val random: Random,
    var position: Offset = Offset(random.nextFloat() * 1080, random.nextFloat() * 1920),
    var alpha: Float = random.nextFloat(),
    var size: Float = random.nextFloat() * 3f + 1f,
    var velocity: Offset = Offset(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f)
) {
    fun update() {
        position += velocity
        alpha -= 0.01f // Fade out gradually
        size = maxOf(size - 0.05f, 0f) // Shrink size
    }
}


@Composable
fun AnimatedMaterialIconButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    fontFamily: FontFamily = FontFamily.Default,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val containerColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFD4AF37),
        targetValue = Color(0xFFE5C158),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = containerColor
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnimatedText(
    text: String,
    fontSize: Int,
    fontWeight: FontWeight,
    fontFamily: FontFamily,
    startColor: Color,
    endColor: Color,
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Animate text color
    val animatedColor by infiniteTransition.animateColor(
        initialValue = startColor,
        targetValue = endColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animate scale for pulsing effect
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(
        text = text,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        color = animatedColor,
        modifier = Modifier.scale(scale)
    )
}
