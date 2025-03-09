package com.google.ar.sceneform.samples.gltf.library.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.BrainPointsEntity
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.MiniGameEntity
import com.google.ar.sceneform.samples.gltf.library.data.local.entities.ModelEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ModelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ModelEntity)

    @Query("SELECT * FROM models WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getModelByName(name: String): ModelEntity?

    @Query("SELECT * FROM models")
    fun getAllModels(): LiveData<List<ModelEntity>>

    @Query("SELECT name FROM models")
    fun getAllModelNames(): LiveData<List<String>>

}
@Dao
interface PointsDao {
    @Query("SELECT * FROM brain_points WHERE id = 1")
    fun getPointsFlow(): Flow<BrainPointsEntity?>  // Use Flow for Live Data

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(pointsEntity: BrainPointsEntity)

    @Query("SELECT points FROM brain_points WHERE id = 1")
    suspend fun getPoints(): Int

    @Query("UPDATE brain_points SET points = :newPoints WHERE id = 1")
    suspend fun updatePoints(newPoints: Int)
}

@Dao
interface MiniGameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: MiniGameEntity)


    @Query("SELECT * FROM mini_games WHERE gameId = :gameId")
    suspend fun getMiniGameById(gameId: String): MiniGameEntity?


    @Query("UPDATE mini_games SET isUnlocked = :isUnlocked WHERE gameId = :gameId")
    suspend fun updateUnlockStatus(gameId: String, isUnlocked: Boolean)

    @Query("SELECT * FROM mini_games")
    fun getAllMiniGamesFlow(): Flow<List<MiniGameEntity>> //  Use Flow for LiveData updates

    @Query("SELECT * FROM mini_games")
    suspend fun getAllMiniGames(): List<MiniGameEntity>

}

