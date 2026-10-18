package com.faces.mobile.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wear_logs",
    foreignKeys = [
        ForeignKey(
            entity = FaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["faceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["faceId"])]
)
data class FaceWearLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val faceId: String,
    val wornAt: Long = System.currentTimeMillis(),
    val wornUntil: Long? = null
)
