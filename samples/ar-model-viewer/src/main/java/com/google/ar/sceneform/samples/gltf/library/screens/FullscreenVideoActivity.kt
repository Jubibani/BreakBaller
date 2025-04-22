package com.google.ar.sceneform.samples.gltf.library.screens

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.google.ar.sceneform.samples.gltf.R

class FullscreenVideoActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private var previewPosition: Long = 0

    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_video)

        playerView = findViewById(R.id.player_view)
        val closeButton: ImageButton = findViewById(R.id.close_button)

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Get video resource ID from intent
        val videoResId = intent.getIntExtra("videoResId", -1)
        previewPosition = intent.getLongExtra("previewPosition", 0L) // Retrieve preview position if passed

        if (videoResId == -1) {
            finish()
            return
        }

        // Prepare the video using the resource URI
        val videoUri = Uri.parse("android.resource://${packageName}/$videoResId")
        val mediaItem = MediaItem.fromUri(videoUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.seekTo(previewPosition) // Seek to the preview position if available
        player.play()

        // Set resize mode for fullscreen
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // Or RESIZE_MODE_FILL

        // Enter fullscreen mode
        enterFullscreen()

        // Handle close button click
        closeButton.setOnClickListener {
            exitFullscreen()
            setResult(RESULT_OK) // Indicate that the user exited normally
            finish()
        }
    }

    private fun enterFullscreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun exitFullscreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}