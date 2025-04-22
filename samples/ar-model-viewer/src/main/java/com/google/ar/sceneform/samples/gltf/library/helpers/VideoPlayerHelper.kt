package com.google.ar.sceneform.samples.gltf.library.helpers

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer


class VideoPlayerHelper(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null

    fun initializePlayer(videoUri: String): ExoPlayer {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
            prepare()
        }
        return exoPlayer!!
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}