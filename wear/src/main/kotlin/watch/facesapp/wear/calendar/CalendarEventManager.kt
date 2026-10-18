package watch.facesapp.wear.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat

data class CalendarEventInfo(
    val title: String,
    val minutesUntil: Int
)

/**
 * Queries Android's CalendarContract to retrieve the user's next upcoming calendar appointment.
 */
class CalendarEventManager(private val context: Context) {

    fun getNextEvent(): CalendarEventInfo? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "READ_CALENDAR permission not granted on wear device")
            return null
        }

        return try {
            val now = System.currentTimeMillis()
            val windowEnd = now + 24 * 60 * 60 * 1000L // Next 24 hours

            val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, now)
            ContentUris.appendId(builder, windowEnd)

            val projection = arrayOf(
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.ALL_DAY
            )

            val cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val titleIndex = it.getColumnIndex(CalendarContract.Instances.TITLE)
                    val beginIndex = it.getColumnIndex(CalendarContract.Instances.BEGIN)

                    val title = if (titleIndex >= 0) it.getString(titleIndex) ?: "Event" else "Event"
                    val beginTime = if (beginIndex >= 0) it.getLong(beginIndex) else now

                    val diffMillis = beginTime - now
                    val minutesUntil = if (diffMillis <= 0L) 0 else ((diffMillis / 60000L).toInt().coerceAtLeast(1))

                    return CalendarEventInfo(
                        title = title.take(20),
                        minutesUntil = minutesUntil
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Error querying calendar on Wear OS", e)
            null
        }
    }

    companion object {
        private const val TAG = "CalendarEventManager"
    }
}
