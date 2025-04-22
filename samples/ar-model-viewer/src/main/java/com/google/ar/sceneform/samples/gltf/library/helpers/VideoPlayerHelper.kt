package com.google.ar.sceneform.samples.gltf.library.helpers

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player

class VideoPlayerHelper(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null

    fun initializePlayer(videoResId: Int): ExoPlayer {
        val videoUri = Uri.parse("android.resource://${context.packageName}/$videoResId")
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val duration = duration
                        if (duration > 0) {
                            seekTo(duration / 2) // Seek to the middle
                            playWhenReady = true // Start playing after seeking
                            removeListener(this) // Remove the listener once we've sought
                        }
                    }
                }
            })
        }
        return exoPlayer!!
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}