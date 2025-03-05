package com.google.ar.sceneform.samples.gltf.library.data.repository

import android.util.Log
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.PointsDao
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.BrainPointsEntity
import kotlinx.coroutines.flow.Flow

class PointsRepository(private val pointsDao: PointsDao) {

    // Convert Room's Flow to Live updates
    val pointsFlow: Flow<BrainPointsEntity?> = pointsDao.getPointsFlow()

    suspend fun getPoints(): Int {
        val existingPoints = pointsDao.getPoints() // Check if points exist
        return existingPoints ?: 0  // Default to 0 if no record exists
    }


    suspend fun updatePoints(newPoints: Int) {
        val current = pointsDao.getPoints() ?: 0 // Handle null case

        if (current == 0) {  // If no existing points, insert new row
            Log.d("PointsDebug", "No existing points found. Inserting new row.")
            pointsDao.insertPoints(BrainPointsEntity(points = newPoints))
        } else {
            Log.d("PointsDebug", "Updating points to: $newPoints")
            pointsDao.updatePoints(newPoints)
        }

        val afterUpdate = pointsDao.getPoints() ?: 0 // Fetch again
        Log.d("PointsDebug", "After Update: $afterUpdate") //  Ensure update works
    }



}
