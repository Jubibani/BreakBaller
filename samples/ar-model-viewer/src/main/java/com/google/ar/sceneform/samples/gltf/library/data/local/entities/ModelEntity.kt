package com.google.ar.sceneform.samples.gltf.library.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey val name: String,
    val modelPath: String,  // Path to .glb file ("models/heart.glb")
    val layoutResId: Int,   // Corresponding UI layout (R.layout.heart_info)
    val interactionPrompt: String,
    val interactionSoundResId: Int,
    val interactionVideoResId: Int? = null // Optional video reference
)

@Entity(tableName = "brain_points")
data class BrainPointsEntity(
    @PrimaryKey val id: Int = 1, // Single row (singleton table)
    val points: Int
)

@Entity(tableName = "mini_games")
data class MiniGameEntity(
    @PrimaryKey val gameId: String,  // Unique ID for each mini-game
    val name: String,                // Name of the mini-game
    val isUnlocked: Boolean          // Unlock status (true if purchased)
)
