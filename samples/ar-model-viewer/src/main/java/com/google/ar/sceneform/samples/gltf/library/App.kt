package com.google.ar.sceneform.samples.gltf.library

import android.app.Application
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase

class App : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the Room database
        database = AppDatabase.getDatabase(this)
    }
}