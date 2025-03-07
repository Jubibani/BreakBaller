package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.MiniGameDao
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.PointsDao
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.MiniGameEntity
import com.google.ar.sceneform.samples.gltf.library.screens.RewardItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RewardsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val miniGameDao: MiniGameDao = db.miniGameDao()
    private val pointsDao: PointsDao = db.brainPointsDao()

    fun unlockMiniGameAndDeductPoints(selectedItem: RewardItemData, onComplete: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPoints = pointsDao.getPoints()

            if (currentPoints >= selectedItem.cost) {
                pointsDao.updatePoints(currentPoints - selectedItem.cost)

                val miniGame = miniGameDao.getMiniGameById("11")
                if (miniGame == null) {
                    miniGameDao.insertGame(MiniGameEntity("11", "Mini-Game", true))
                } else if (!miniGame.isUnlocked) {
                    miniGameDao.updateUnlockStatus("11", true)
                }

                // Ensure the database updates are applied
                val updatedMiniGame = miniGameDao.getMiniGameById("11")
                Log.d("MiniGameDebug", "Updated MiniGame: $updatedMiniGame")

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

}

