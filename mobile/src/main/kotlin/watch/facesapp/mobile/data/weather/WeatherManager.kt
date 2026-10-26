package watch.facesapp.mobile.data.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration.Companion.milliseconds

data class WeatherInfo(
    val tempC: Int,
    val condition: String,
    val sunriseSunsetSummary: String,
    val isSunset: Boolean
)

class WeatherManager(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun fetchCurrentWeather(): WeatherInfo? = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cachedTime = prefs.getLong(KEY_CACHED_TIMESTAMP, 0L)
        val fourHoursMs = 4 * 60 * 60 * 1000L

        if (now - cachedTime < fourHoursMs) {
            val cachedTemp = prefs.getInt(KEY_CACHED_TEMP, Int.MIN_VALUE)
            val cachedCond = prefs.getString(KEY_CACHED_COND, null)
            val cachedSummary = prefs.getString(KEY_CACHED_SUMMARY, null)
            val cachedIsSunset = prefs.getBoolean(KEY_CACHED_IS_SUNSET, true)
            if (cachedTemp != Int.MIN_VALUE && !cachedCond.isNullOrBlank() && !cachedSummary.isNullOrBlank()) {
                Log.d(TAG, "Using cached weather (age ${(now - cachedTime) / 60000} minutes)")
                return@withContext WeatherInfo(cachedTemp, cachedCond, cachedSummary, cachedIsSunset)
            }
        }

        val (lat, lon) = getApproximateCoordinates()
        val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&daily=sunrise,sunset&timezone=auto"
        Log.d(TAG, "Requesting weather from: $urlString")

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        for (attempt in 1..3) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                val activeNetwork = cm?.activeNetwork
                connection = ((if (activeNetwork != null) {
                    activeNetwork.openConnection(url)
                } else {
                    url.openConnection()
                }) as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "FacesWatchApp/2.0 (Android; +https://facesapp.watch)")
                    setRequestProperty("Accept", "application/json")
                }

                val statusCode = connection.responseCode
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                    val json = JSONObject(response)
                    val current = json.getJSONObject("current")
                    val temp = current.getDouble("temperature_2m").toInt()
                    val code = current.getInt("weather_code")
                    val condition = mapWmoCode(code)

                    var sunSummary: String? = null
                    var isSunset = true

                    val daily = json.optJSONObject("daily")
                    if (daily != null) {
                        val sunrises = daily.optJSONArray("sunrise")
                        val sunsets = daily.optJSONArray("sunset")
                        if (sunrises != null && sunsets != null && sunrises.length() > 0 && sunsets.length() > 0) {
                            val todaySunrise = sunrises.optString(0, "")
                            val todaySunset = sunsets.optString(0, "")
                            val tomorrowSunrise = if (sunrises.length() > 1) sunrises.optString(1, "") else todaySunrise

                            val nowCal = java.util.Calendar.getInstance()
                            val curMinutes = nowCal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + nowCal.get(java.util.Calendar.MINUTE)

                            val riseTime = todaySunrise.substringAfter("T").take(5)
                            val setTime = todaySunset.substringAfter("T").take(5)
                            val nextRiseTime = tomorrowSunrise.substringAfter("T").take(5)

                            val riseMinutes = parseMinutes(riseTime)
                            val setMinutes = parseMinutes(setTime)

                            if (curMinutes in riseMinutes until setMinutes) {
                                isSunset = true
                                sunSummary = "Sunset $setTime"
                            } else if (curMinutes < riseMinutes) {
                                isSunset = false
                                sunSummary = "Sunrise $riseTime"
                            } else {
                                isSunset = false
                                sunSummary = "Sunrise $nextRiseTime"
                            }
                        }
                    }

                    if (sunSummary == null) {
                        val fallback = getSunCycle(lat, lon)
                        sunSummary = fallback.first
                        isSunset = fallback.second
                    }

                    prefs.edit {
                        putLong(KEY_CACHED_TIMESTAMP, now)
                        putInt(KEY_CACHED_TEMP, temp)
                        putString(KEY_CACHED_COND, condition)
                        putString(KEY_CACHED_SUMMARY, sunSummary)
                        putBoolean(KEY_CACHED_IS_SUNSET, isSunset)
                    }

                    Log.d(TAG, "Fetched live weather for ($lat, $lon): $temp°C, $condition, $sunSummary (isSunset=$isSunset)")
                    return@withContext WeatherInfo(temp, condition, sunSummary, isSunset)
                } else {
                    val errBody = try {
                        connection.errorStream?.bufferedReader()?.use(BufferedReader::readText)
                    } catch (_: Exception) {
                        null
                    }
                    Log.w(TAG, "Open-Meteo returned HTTP $statusCode for URL $urlString: $errBody")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Attempt $attempt failed fetching live weather: ${e.javaClass.simpleName} - ${e.message}")
                if (attempt < 3) {
                    delay((1000L * attempt).milliseconds)
                }
            } finally {
                try {
                    connection?.disconnect()
                } catch (_: Exception) {
                }
            }
        }

        val cachedTemp = prefs.getInt(KEY_CACHED_TEMP, Int.MIN_VALUE)
        val cachedCond = prefs.getString(KEY_CACHED_COND, null)
        val cachedSummary = prefs.getString(KEY_CACHED_SUMMARY, null)
        val cachedIsSunset = prefs.getBoolean(KEY_CACHED_IS_SUNSET, true)
        if (cachedTemp != Int.MIN_VALUE && !cachedCond.isNullOrBlank() && !cachedSummary.isNullOrBlank()) {
            Log.w(TAG, "Network fetch failed; falling back to cached weather")
            return@withContext WeatherInfo(cachedTemp, cachedCond, cachedSummary, cachedIsSunset)
        }

        null
    }

    private fun getApproximateCoordinates(): Pair<Double, Double> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val loc: Location? = (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    lm?.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                } else null)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (loc != null) {
                    val lat = String.format(java.util.Locale.US, "%.4f", loc.latitude).toDouble()
                    val lon = String.format(java.util.Locale.US, "%.4f", loc.longitude).toDouble()
                    return Pair(lat, lon)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error retrieving coarse location", e)
            }
        }

        // Default to London or typical coordinates
        return Pair(51.5074, -0.1278)
    }

    private fun mapWmoCode(code: Int): String {
        return when (code) {
            0 -> "Sunny"
            1, 2 -> "Partly Cloudy"
            3 -> "Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55, 61, 63, 65 -> "Rainy"
            71, 73, 75, 77, 85, 86 -> "Snowy"
            80, 81, 82 -> "Showers"
            95, 96, 99 -> "Stormy"
            else -> "Clear"
        }
    }

    fun getInitialSunCycle(): Pair<String, Boolean> {
        val (lat, lon) = getApproximateCoordinates()
        return getSunCycle(lat, lon)
    }

    private fun calculateSunTimes(
        lat: Double,
        lon: Double,
        calendar: java.util.Calendar = java.util.Calendar.getInstance()
    ): Pair<String, String> {
        val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val declination = 23.45 * kotlin.math.sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81)))
        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(declination)
        val cosHourAngle = (-kotlin.math.tan(latRad) * kotlin.math.tan(decRad)).coerceIn(-1.0, 1.0)
        val hourAngleDeg = Math.toDegrees(kotlin.math.acos(cosHourAngle))
        val halfDayHours = hourAngleDeg / 15.0

        val tzOffsetHours = calendar.timeZone.getOffset(calendar.timeInMillis) / 3600000.0
        val solarNoon = 12.0 - (lon / 15.0) + tzOffsetHours

        val sunriseHour = (solarNoon - halfDayHours + 24.0) % 24.0
        val sunsetHour = (solarNoon + halfDayHours + 24.0) % 24.0

        val sunriseH = sunriseHour.toInt()
        val sunriseM = ((sunriseHour - sunriseH) * 60).toInt().coerceIn(0, 59)
        val sunsetH = sunsetHour.toInt()
        val sunsetM = ((sunsetHour - sunsetH) * 60).toInt().coerceIn(0, 59)

        val sunriseStr = String.format(java.util.Locale.US, "%02d:%02d", sunriseH, sunriseM)
        val sunsetStr = String.format(java.util.Locale.US, "%02d:%02d", sunsetH, sunsetM)
        return Pair(sunriseStr, sunsetStr)
    }

    private fun getSunCycle(lat: Double, lon: Double): Pair<String, Boolean> {
        val (sunriseStr, sunsetStr) = calculateSunTimes(lat, lon)
        val now = java.util.Calendar.getInstance()
        val currentMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)

        val riseMinutes = parseMinutes(sunriseStr)
        val setMinutes = parseMinutes(sunsetStr)

        return if (currentMinutes in riseMinutes until setMinutes) {
            Pair("Sunset $sunsetStr", true)
        } else if (currentMinutes < riseMinutes) {
            Pair("Sunrise $sunriseStr", false)
        } else {
            Pair("Sunrise $sunriseStr", false)
        }
    }

    private fun parseMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        return if (parts.size >= 2) {
            (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
        } else 0
    }

    companion object {
        private const val TAG = "WeatherManager"
        private const val PREFS_NAME = "faces_weather_cache_prefs"
        private const val KEY_CACHED_TIMESTAMP = "cached_timestamp"
        private const val KEY_CACHED_TEMP = "cached_temp"
        private const val KEY_CACHED_COND = "cached_cond"
        private const val KEY_CACHED_SUMMARY = "cached_summary"
        private const val KEY_CACHED_IS_SUNSET = "cached_is_sunset"
    }
}
