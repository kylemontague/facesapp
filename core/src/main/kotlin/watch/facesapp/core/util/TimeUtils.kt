package watch.facesapp.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    const val UK_DATE = "dd/MM/yy"
    const val US_DATE = "MM/dd/yy"
    const val SHORT_DATE = "dd MMM"
    const val MONTH_ONLY = "MMMM"
    const val DAY_ONLY = "dd"
    const val YEAR_ONLY = "yyyy"

    fun formatDate(date: Date, pattern: String = UK_DATE, locale: Locale = Locale.getDefault()): String {
        return SimpleDateFormat(pattern, locale).format(date)
    }

    /**
     * Compute clock hand angles in radians:
     * - secondAngle: 0..2*PI for seconds (0..59)
     * - minuteAngle: 0..2*PI for minutes (0..59)
     * - hourAngle: 0..2*PI for 12 hours including minute progression
     */
    fun computeHandAngles(calendar: Calendar): Triple<Float, Float, Float> {
        val seconds = calendar.get(Calendar.SECOND)
        val minutes = calendar.get(Calendar.MINUTE)
        val hour12 = calendar.get(Calendar.HOUR)

        val secAngle = (seconds / 30f) * Math.PI.toFloat()
        val minAngle = (minutes / 30f) * Math.PI.toFloat()
        val hourAngle = ((hour12 + (minutes / 60f)) / 6f) * Math.PI.toFloat()

        return Triple(hourAngle, minAngle, secAngle)
    }

    /**
     * Format time as HH:mm with leading zeros.
     */
    fun formatDigitalTime(calendar: Calendar, use24Hour: Boolean = false): String {
        val hour = if (use24Hour) {
            calendar.get(Calendar.HOUR_OF_DAY)
        } else {
            val h = calendar.get(Calendar.HOUR)
            if (h == 0) 12 else h
        }
        val minute = calendar.get(Calendar.MINUTE)
        return String.format(Locale.US, "%d:%02d", hour, minute)
    }

    /**
     * Format time with two-digit hour (e.g. "09" or "12")
     */
    fun formatTwoDigitHour(calendar: Calendar): String {
        val h = calendar.get(Calendar.HOUR)
        val hour12 = if (h == 0) 12 else h
        return String.format(Locale.US, "%02d", hour12)
    }
}
