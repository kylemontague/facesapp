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

data class WeatherInfo(
    val tempC: Int,
    val condition: String
)

class WeatherManager(private val context: Context) {

    suspend fun fetchCurrentWeather(): WeatherInfo? = withContext(Dispatchers.IO) {
        val (lat, lon) = getApproximateCoordinates()

        try {
            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "FacesApp/2.0")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val json = JSONObject(response)
                val current = json.getJSONObject("current")
                val temp = current.getDouble("temperature_2m").toInt()
                val code = current.getInt("weather_code")
                val condition = mapWmoCode(code)

                Log.d(TAG, "Fetched live weather for ($lat, $lon): $temp°C, $condition")
                return@withContext WeatherInfo(temp, condition)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching live weather from Open-Meteo", e)
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
                val loc: Location? = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                if (loc != null) {
                    return Pair(loc.latitude, loc.longitude)
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
