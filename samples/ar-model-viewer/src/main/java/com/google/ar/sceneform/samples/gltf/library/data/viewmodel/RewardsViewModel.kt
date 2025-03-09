package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.sceneform.samples.gltf.R
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.MiniGameDao
import com.google.ar.sceneform.samples.gltf.library.data.local.dao.PointsDao
import com.google.ar.sceneform.samples.gltf.library.data.local.database.AppDatabase
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.MiniGameEntity
import com.google.ar.sceneform.samples.gltf.library.screens.RewardItemData
import com.unity3d.player.UnityPlayerGameActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

//Forewarning! Many Debug Logs and Comments! The developer is learning slow.

class RewardsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val miniGameDao: MiniGameDao = db.miniGameDao()
    private val pointsDao: PointsDao = db.brainPointsDao()


    //  Unlock a mini-game and deduct points
    fun unlockMiniGameAndDeductPoints(gameId: String, cost: Int, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MiniGameDebug", "Attempting to unlock $gameId")

            val currentPoints = pointsDao.getPoints()
            Log.d("MiniGameDebug", "Current points: $currentPoints, Cost: $cost")

            if (currentPoints >= cost) {
                pointsDao.updatePoints(currentPoints - cost)
                Log.d("MiniGameDebug", "Deducted $cost points. New balance: ${currentPoints - cost}")

                val miniGame = miniGameDao.getMiniGameById(gameId)

                if (miniGame == null) {
                    Log.d("MiniGameDebug", "Inserting new game: $gameId")
                    miniGameDao.insertGame(MiniGameEntity(gameId, "Unknown Game", true))
                } else if (!miniGame.isUnlocked) {
                    Log.d("MiniGameDebug", "Updating unlock status: $gameId")
                    miniGameDao.updateUnlockStatus(gameId, true)
                } else {
                    Log.d("MiniGameDebug", "Game $gameId is already unlocked!")
                }

                val updatedMiniGame = miniGameDao.getMiniGameById(gameId)
                Log.d("MiniGameDebug", "Updated MiniGame ($gameId): $updatedMiniGame")

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } else {
                Log.d("MiniGameDebug", "Not enough points to unlock $gameId")
            }
        }
    }

    //  Fetch all mini-games (now non-suspend)
    fun getAllMiniGames(): List<MiniGameEntity> {
        return runBlocking(Dispatchers.IO) {
            miniGameDao.getAllMiniGames()
        }
    }

    //  Fetch mini-game rewards safely
    fun getMiniGameRewards(): List<RewardItemData> {
        val miniGames = getAllMiniGames()
        return miniGames.map { game ->
            RewardItemData(
                id = game.gameId,
                name = game.name,
                description = "Unlock to play ${game.name}",
                imageResId = R.drawable.question_icon,
                cost = if (game.gameId == "11") 50 else 75,
                isUnlocked = game.isUnlocked,
            ) {
                if (game.isUnlocked) {
                    Log.d("MiniGameDebug", "Launching ${game.name}")
                    val intent = Intent(getApplication(), UnityPlayerGameActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    getApplication<Application>().startActivity(intent)
                } else {
                    Log.d("MiniGameDebug", "Not enough points to unlock")
                }
            }
        }
    }
}
