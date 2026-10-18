package watch.facesapp.mobile.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.coroutines.flow.Flow

data class FaceWearSession(
    @Embedded
    val log: FaceWearLogEntity,
    @Relation(
        parentColumn = "faceId",
        entityColumn = "id"
    )
    val face: FaceEntity?
)

@Dao
interface FaceDao {

    @Query("SELECT * FROM faces ORDER BY createdAt DESC")
    fun getAllFaces(): Flow<List<FaceEntity>>

    @Query("SELECT * FROM faces WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteFaces(): Flow<List<FaceEntity>>

    @Query("SELECT * FROM faces WHERE id = :id LIMIT 1")
    fun getFaceById(id: String): Flow<FaceEntity?>

    @Query("SELECT * FROM faces WHERE isCurrentOnWatch = 1 LIMIT 1")
    fun getCurrentActiveFace(): Flow<FaceEntity?>

    @Query("SELECT * FROM faces WHERE lastWornAt IS NOT NULL AND isCurrentOnWatch = 0 ORDER BY lastWornAt ASC")
    fun getHistoricalWornFaces(): Flow<List<FaceEntity>>

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

    // Timeline & Wear Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWearLog(log: FaceWearLogEntity)

    @Transaction
    @Query("SELECT * FROM wear_logs ORDER BY wornAt DESC")
    fun getAllWearSessions(): Flow<List<FaceWearSession>>

    @Query("SELECT * FROM wear_logs ORDER BY wornAt DESC")
    fun getAllWearLogs(): Flow<List<FaceWearLogEntity>>

    @Query("SELECT * FROM wear_logs WHERE faceId = :faceId ORDER BY wornAt DESC")
    fun getWearLogsForFace(faceId: String): Flow<List<FaceWearLogEntity>>

    @Query("UPDATE wear_logs SET wornUntil = :timestamp WHERE wornUntil IS NULL")
    suspend fun closeOpenWearLogs(timestamp: Long)

    @Query("UPDATE faces SET isCurrentOnWatch = 0")
    suspend fun clearCurrentOnWatch()

    @Query("UPDATE faces SET isCurrentOnWatch = 1, lastWornAt = :timestamp WHERE id = :faceId")
    suspend fun markFaceAsCurrent(faceId: String, timestamp: Long)

    @Transaction
    suspend fun recordWearFace(faceId: String, timestamp: Long = System.currentTimeMillis()) {
        closeOpenWearLogs(timestamp)
        clearCurrentOnWatch()
        markFaceAsCurrent(faceId, timestamp)
        insertWearLog(FaceWearLogEntity(faceId = faceId, wornAt = timestamp, wornUntil = null))
    }
}
