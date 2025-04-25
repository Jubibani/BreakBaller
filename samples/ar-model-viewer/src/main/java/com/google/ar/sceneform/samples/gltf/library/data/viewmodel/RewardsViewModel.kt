package com.google.ar.sceneform.samples.gltf.library.data.viewmodel

//Forewarning! Many Debug Logs and Comments! The developer is learning slow.

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RewardsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val miniGameDao: MiniGameDao = db.miniGameDao()
    private val pointsDao: PointsDao = db.brainPointsDao()

    //  StateFlow for unlocked mini-games (Auto-updates UI)
    private val _rewardItems = MutableStateFlow<List<RewardItemData>>(emptyList())
    val rewardItems: StateFlow<List<RewardItemData>> = _rewardItems.asStateFlow()

    init {
        refreshRewards() // Load rewards on start
    }

    //  Refresh rewards from DB
/*    private fun refreshRewards() {
        viewModelScope.launch(Dispatchers.IO) {
            val miniGames = miniGameDao.getAllMiniGames()
            Log.d("DatabaseDebug", "Fetched from DB: $miniGames") //  CHECK IF DATA EXISTS

            _rewardItems.value = miniGames.map { game ->
                RewardItemData(
                    id = game.gameId,
                    name = game.name,
                    description = "Unlock to play ${game.name}",
                    imageResId = R.drawable.question_icon,
                    cost = if (game.gameId == "11") 50 else 75,
                    isUnlocked = game.isUnlocked,
                    onClickAction = {}
                )
            }
            Log.d("DatabaseDebug", "Mapped reward items: ${_rewardItems.value}")
        }
    }*/

    private fun refreshRewards() {
        viewModelScope.launch(Dispatchers.IO) {
            val miniGames = miniGameDao.getAllMiniGames()
            Log.d("DatabaseDebug", "Fetched from DB: $miniGames") // DEBUG CHECK

            _rewardItems.value = miniGames.map { game ->
                when (game.gameId) {
                    "1", "2", "3", "4" -> {
                        // Placeholder rewards that show a Toast when unlocked
                        RewardItemData(
                            id = game.gameId,
                            name = game.name,
                            description = "Unlock to receive a surprise!",
                            imageResId = R.drawable.question_icon,
                            cost = 50, // Example cost
                            isUnlocked = game.isUnlocked,
                            onClickAction = {
                                if (game.isUnlocked) {
                                    Toast.makeText(
                                        getApplication(),
                                        "You've unlocked ${game.name}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                    "5" -> {
                        // The imported Unity mini-game reward
                        RewardItemData(
                            id = game.gameId,
                            name = "Break Baller",
                            description = "Break Baller is where you control a paddle and break bricks to win!",
                            imageResId = R.drawable.question_icon, // Replace with actual icon
                            cost = 75, // Adjust as needed
                            isUnlocked = game.isUnlocked,
                            onClickAction = {
                                if (game.isUnlocked) {
                                    Log.d("MiniGameDebug", "Launching Unity mini-game")
                                    val intent = Intent(getApplication(), UnityPlayerGameActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    getApplication<Application>().startActivity(intent)
                                }
                            }
                        )
                    }
                    else -> {
                        // Future Unity mini-games (keeps flexibility for upcoming imports)
                        RewardItemData(
                            id = game.gameId,
                            name = game.name,
                            description = "Unlock to play ${game.name}",
                            imageResId = R.drawable.question_icon, // Placeholder until assigned
                            cost = 75, // Default cost for future games
                            isUnlocked = game.isUnlocked,
                            onClickAction = {
                                if (game.isUnlocked) {
                                    Log.d("MiniGameDebug", "Launching ${game.name}")
                                    val intent = Intent(getApplication(), UnityPlayerGameActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    getApplication<Application>().startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
            Log.d("DatabaseDebug", "Mapped reward items: ${_rewardItems.value}")
        }
    }



    //  Unlock and refresh data
    fun unlockMiniGameAndDeductPoints(gameId: String, cost: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPoints = pointsDao.getPoints()
            if (currentPoints >= cost) {
                pointsDao.updatePoints(currentPoints - cost)
                val miniGame = miniGameDao.getMiniGameById(gameId)

                if (miniGame == null || !miniGame.isUnlocked) {
                    miniGameDao.insertGame(MiniGameEntity(gameId, "Unknown Game", true))
                }

                refreshRewards() // 🔥 Automatically update UI
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
