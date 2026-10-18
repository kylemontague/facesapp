package watch.facesapp.core.util

import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceStyle
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

data class SharedFaceData(
    val style: FaceStyle,
    val colors: List<Int>,
    val name: String,
    val options: Map<String, String> = emptyMap()
)

object FaceShareUtils {
    const val WEB_BASE_URL = "https://www.facesapp.watch/share"
    const val APP_SCHEME = "facesapp"
    const val APP_HOST = "share"

    /**
     * Builds a universal web deep link encoding the watch face template, color hexes, name, and options.
     * E.g. https://www.facesapp.watch/share?style=FILL_TIME&colors=FF89C4BB,FFCDE9E6&name=Filling+Time
     */
    fun createShareUrl(
        style: FaceStyle,
        colors: List<Int>,
        name: String? = null,
        options: Map<String, String> = emptyMap()
    ): String {
        val colorHexes = colors.joinToString(",") { color ->
            "%08X".format(color)
        }
        val faceName = name?.ifBlank { style.displayName } ?: style.displayName
        val encodedName = URLEncoder.encode(faceName, "UTF-8")
        val optsQuery = if (options.isNotEmpty()) {
            val optsStr = options.entries.joinToString(";") { "${it.key}:${it.value}" }
            "&opts=" + URLEncoder.encode(optsStr, "UTF-8")
        } else ""

        return "$WEB_BASE_URL?style=${style.id}&colors=$colorHexes&name=$encodedName$optsQuery"
    }

    /**
     * Builds a custom scheme deep link (facesapp://share?style=...&colors=...&name=...)
     */
    fun createCustomSchemeUrl(
        style: FaceStyle,
        colors: List<Int>,
        name: String? = null,
        options: Map<String, String> = emptyMap()
    ): String {
        val colorHexes = colors.joinToString(",") { color ->
            "%08X".format(color)
        }
        val faceName = name?.ifBlank { style.displayName } ?: style.displayName
        val encodedName = URLEncoder.encode(faceName, "UTF-8")
        val optsQuery = if (options.isNotEmpty()) {
            val optsStr = options.entries.joinToString(";") { "${it.key}:${it.value}" }
            "&opts=" + URLEncoder.encode(optsStr, "UTF-8")
        } else ""

        return "$APP_SCHEME://$APP_HOST?style=${style.id}&colors=$colorHexes&name=$encodedName$optsQuery"
    }

    /**
     * Parses a deep link String (e.g. https://www.facesapp.watch/share?... or facesapp://share?...)
     * and extracts the watch face style, colors, name, and options.
     */
    fun parseShareUri(uriString: String): SharedFaceData? {
        return try {
            val uri = URI(uriString)
            val scheme = uri.scheme?.lowercase() ?: return null
            val host = uri.host?.lowercase() ?: return null
            val path = uri.path ?: ""

            val isWebShare = (scheme == "https" || scheme == "http") &&
                    (host == "www.facesapp.watch" || host == "facesapp.watch" || host == "faces.watch") &&
                    path.startsWith("/share")

            val isCustomScheme = (scheme == APP_SCHEME || scheme == "faces") && host == APP_HOST

            if (!isWebShare && !isCustomScheme) {
                return null
            }

            val query = uri.rawQuery ?: return null
            val queryParams = query.split("&").mapNotNull { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = URLDecoder.decode(parts[0], "UTF-8")
                    val value = URLDecoder.decode(parts[1], "UTF-8")
                    key to value
                } else null
            }.toMap()

            val styleParam = queryParams["style"]
            val style = if (!styleParam.isNullOrBlank()) {
                FaceStyle.fromId(styleParam)
            } else {
                FaceStyle.OUA_TIME
            }

            val colorsParam = queryParams["colors"]
            val colors = if (!colorsParam.isNullOrBlank()) {
                colorsParam.split(",")
                    .take(16)
                    .mapNotNull { hexStr ->
                        val trimmed = hexStr.trim()
                        try {
                            when (trimmed.length) {
                                6 -> ("FF$trimmed").toLong(16).toInt()
                                8 -> trimmed.toLong(16).toInt()
                                else -> null
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }
            } else {
                emptyList()
            }

            val effectiveColors = colors.ifEmpty {
                FaceConfig.defaultColorsForStyle(style)
            }

            val nameParam = queryParams["name"]
            val name = nameParam?.trim()?.take(50)?.ifBlank { style.displayName } ?: style.displayName

            val optsParam = queryParams["opts"]
            val options = if (!optsParam.isNullOrBlank()) {
                optsParam.split(";").mapNotNull { entry ->
                    val parts = entry.split(":", limit = 2)
                    if (parts.size == 2 && parts[0].isNotBlank()) {
                        parts[0] to parts[1]
                    } else null
                }.toMap()
            } else {
                emptyMap()
            }

            SharedFaceData(
                style = style,
                colors = effectiveColors,
                name = name,
                options = options
            )
        } catch (_: Exception) {
            null
        }
    }

    fun parseShareUri(androidUri: android.net.Uri): SharedFaceData? =
        parseShareUri(androidUri.toString())
}
