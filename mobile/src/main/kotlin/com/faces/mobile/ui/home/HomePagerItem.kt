package com.faces.mobile.ui.home

import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceStyle
import com.faces.mobile.data.db.FaceEntity

sealed interface HomePagerItem {
    val key: String
    val config: FaceConfig
    val title: String

    /**
     * A familiar face from the user's wear history.
     * Swiped to the left from the active watch face.
     */
    data class HistoricalFace(
        val logId: Long,
        val entity: FaceEntity,
        val wornAt: Long,
        override val title: String
    ) : HomePagerItem {
        override val key: String get() = "hist_log_${logId}"
        override val config: FaceConfig get() = entity.toFaceConfig()
    }

    /**
     * The currently active watch face on the user's wrist.
     */
    data class ActiveFace(
        val entity: FaceEntity,
        override val title: String = "On yer wrist!"
    ) : HomePagerItem {
        override val key: String get() = "active_${entity.id}"
        override val config: FaceConfig get() = entity.toFaceConfig()
    }

    /**
     * A base template ready to be customized or created with a photo.
     * Swiped to the right from the active watch face.
     */
    data class TemplateFace(
        val style: FaceStyle,
        val defaultColors: List<Int> = FaceConfig.defaultColorsForStyle(style)
    ) : HomePagerItem {
        override val key: String get() = "tmpl_${style.id}"
        override val title: String get() = style.displayName
        override val config: FaceConfig get() = FaceConfig(
            id = "tmpl_${style.id}",
            style = style,
            colors = defaultColors
        )
    }
}
