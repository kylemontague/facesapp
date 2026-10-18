package watch.facesapp.wear.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import watch.facesapp.wear.service.FacesWatchFaceService

/**
 * Transparent activity that requests Wear OS runtime permissions for heart rate,
 * physical activity / step counters, and calendar access.
 */
class WearPermissionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val needed = getMissingPermissions(this)
        if (needed.isEmpty()) {
            FacesWatchFaceService.onPermissionsGranted(this)
            finish()
            return
        }

        requestPermissions(needed.toTypedArray(), RC_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        FacesWatchFaceService.onPermissionsGranted(this)
        finish()
    }

    companion object {
        private const val RC_PERMISSIONS = 1001

        fun getMissingPermissions(context: Context): List<String> {
            val list = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                list.add(Manifest.permission.BODY_SENSORS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    list.add(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED
            ) {
                list.add(Manifest.permission.READ_CALENDAR)
            }
            return list
        }

        fun checkAndRequest(context: Context) {
            val missing = getMissingPermissions(context)
            if (missing.isNotEmpty()) {
                val intent = Intent(context, WearPermissionActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                try {
                    val pendingIntent = android.app.PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    pendingIntent.send()
                } catch (e: Exception) {
                    try {
                        context.startActivity(intent)
                    } catch (e2: Exception) {
                        android.util.Log.w("WearPermissionActivity", "Unable to start permission activity", e2)
                    }
                }
            }
        }
    }
}
