package com.faces.mobile.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceStyle
import java.util.UUID

@Entity(tableName = "faces")
data class FaceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val style: FaceStyle = FaceStyle.OUA_TIME,
    val colors: List<Int> = emptyList(),
    val name: String = style.displayName,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastWornAt: Long? = null,
    val isCurrentOnWatch: Boolean = false
) {
    fun toFaceConfig(): FaceConfig = FaceConfig(
        id = id,
        style = style,
        colors = colors,
        isFavorite = isFavorite,
        createdAt = createdAt
    )

    companion object {
        fun fromFaceConfig(
            config: FaceConfig,
            name: String = config.style.displayName,
            lastWornAt: Long? = System.currentTimeMillis(),
            isCurrentOnWatch: Boolean = false
        ): FaceEntity = FaceEntity(
            id = config.id,
            style = config.style,
            colors = config.colors,
            name = name,
            isFavorite = config.isFavorite,
            createdAt = config.createdAt,
            lastWornAt = lastWornAt,
            isCurrentOnWatch = isCurrentOnWatch
        )

        fun createStockPresets(): List<FaceEntity> = FaceStyle.entries.mapIndexed { index, style ->
            val isFirst = index == 0
            FaceEntity(
                id = "stock_${style.id.lowercase()}",
                style = style,
                colors = FaceConfig.defaultColorsForStyle(style),
                name = style.displayName,
                isFavorite = false,
                createdAt = System.currentTimeMillis() - (index * 86_400_000L),
                lastWornAt = if (isFirst) System.currentTimeMillis() else null,
                isCurrentOnWatch = isFirst
            )
        }
    }
}
