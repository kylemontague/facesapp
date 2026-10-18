package com.faces.mobile.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FaceDao {

    @Query("SELECT * FROM faces ORDER BY createdAt DESC")
    fun getAllFaces(): Flow<List<FaceEntity>>

    @Query("SELECT * FROM faces WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteFaces(): Flow<List<FaceEntity>>

    @Query("SELECT * FROM faces WHERE id = :id LIMIT 1")
    fun getFaceById(id: String): Flow<FaceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFace(face: FaceEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFaces(faces: List<FaceEntity>)

    @Update
    suspend fun updateFace(face: FaceEntity)

    @Delete
    suspend fun deleteFace(face: FaceEntity)

    @Query("DELETE FROM faces WHERE id = :id")
    suspend fun deleteFaceById(id: String)

    @Query("UPDATE faces SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM faces")
    suspend fun getCount(): Int
}
