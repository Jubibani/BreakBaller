package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

//Forewarning! Many Debug Logs and Comments! The developer is learning slow.

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
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

class RewardsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val miniGameDao: MiniGameDao = db.miniGameDao()
    private val pointsDao: PointsDao = db.brainPointsDao()

    // Observe unlocked games with LiveData
    val miniGameRewards: LiveData<List<RewardItemData>> = miniGameDao.getAllMiniGamesFlow()
        .asLiveData()
        .map { miniGames ->
            miniGames.map { game ->
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

    // Unlock a mini-game and deduct points
    fun unlockMiniGameAndDeductPoints(gameId: String, cost: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("MiniGameDebug", "Attempting to unlock $gameId")
            val currentPoints = pointsDao.getPoints()
            Log.d("MiniGameDebug", "Current points: $currentPoints, Cost: $cost")

            if (currentPoints >= cost) {
                pointsDao.updatePoints(currentPoints - cost)
                Log.d("MiniGameDebug", "Deducted $cost points. New balance: ${currentPoints - cost}")

                val miniGame = miniGameDao.getMiniGameById(gameId)
                if (miniGame == null || !miniGame.isUnlocked) {
                    Log.d("MiniGameDebug", "Unlocking game: $gameId")
                    miniGameDao.insertGame(MiniGameEntity(gameId, "Unknown Game", true))
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
