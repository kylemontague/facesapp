package watch.facesapp.mobile.data.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

import android.net.ConnectivityManager
import kotlinx.coroutines.delay

data class WeatherInfo(
    val tempC: Int,
    val condition: String
)

class WeatherManager(private val context: Context) {

    suspend fun fetchCurrentWeather(): WeatherInfo? = withContext(Dispatchers.IO) {
        val (lat, lon) = getApproximateCoordinates()
        val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
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

                    Log.d(TAG, "Fetched live weather for ($lat, $lon): $temp°C, $condition")
                    return@withContext WeatherInfo(temp, condition)
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
                    delay(1000L * attempt)
                }
            } finally {
                try {
                    connection?.disconnect()
                } catch (_: Exception) {
                }
            }
        }

        // Return null if offline or network unavailable so complication is not populated with dummy data
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

    companion object {
        private const val TAG = "WeatherManager"
    }
}
