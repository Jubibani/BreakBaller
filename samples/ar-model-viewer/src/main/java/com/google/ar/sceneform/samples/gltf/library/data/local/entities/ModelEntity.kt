package com.google.ar.sceneform.samples.gltf.library.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,  // e.g., "Amphibian"
    val modelPath: String, // e.g., "models/amphibian.glb"
    val soundPath: String, // e.g., "R.raw.froggy"
    val description: String  // Optional, for displaying info
)
