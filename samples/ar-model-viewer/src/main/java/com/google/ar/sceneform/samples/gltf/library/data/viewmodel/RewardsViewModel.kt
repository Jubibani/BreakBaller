package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

import android.app.Application
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

    private val db = AppDatabase.getDatabase(application, viewModelScope) // Fix: Use viewModelScope
    private val miniGameDao: MiniGameDao = db.miniGameDao()
    private val pointsDao: PointsDao = db.brainPointsDao() // Fix: Correct function name

    fun unlockMiniGameAndDeductPoints(selectedItem: RewardItemData, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPoints = withContext(Dispatchers.IO) { pointsDao.getPoints() } // Fix: Avoid main thread call

            if (currentPoints >= selectedItem.cost) {
                pointsDao.updatePoints(currentPoints - selectedItem.cost)

                if (selectedItem.name == "11") { // Mini-game ID
                    val miniGame = miniGameDao.getMiniGameById("11")
                    if (miniGame == null) { // Fix: Check for null instead of 0
                        miniGameDao.insertGame(MiniGameEntity("11", "Mini-Game", true))
                    } else if (!miniGame.isUnlocked) {
                        miniGameDao.updateUnlockStatus("11", true)
                    }
                }
            }
            onComplete()
        }
    }
}
