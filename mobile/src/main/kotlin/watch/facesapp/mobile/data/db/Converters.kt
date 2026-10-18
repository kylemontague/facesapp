package watch.facesapp.mobile.data.db

import androidx.room.TypeConverter
import watch.facesapp.core.model.FaceStyle

class Converters {

    @TypeConverter
    fun fromColorList(colors: List<Int>?): String {
        return colors?.joinToString(",") { it.toString() } ?: ""
    }

    @TypeConverter
    fun toColorList(data: String?): List<Int> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromFaceStyle(style: FaceStyle?): String {
        return style?.id ?: FaceStyle.OUA_TIME.id
    }

    @TypeConverter
    fun toFaceStyle(id: String?): FaceStyle {
        return FaceStyle.fromId(id ?: "")
    }
}
