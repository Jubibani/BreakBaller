package com.google.ar.sceneform.samples.gltf.library.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey val name: String,
    val modelPath: String,  // Path to .glb file (e.g., "models/heart.glb")
    val layoutResId: Int,   // Corresponding UI layout (e.g., R.layout.heart_info)
    val interactionPrompt: String,
    val interactionSoundResId: Int
)
