package watch.facesapp.core.util

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import watch.facesapp.core.R

object FontProvider {

    private var quicksandBold: Typeface? = null
    private var proximaLight: Typeface? = null
    private var proximaSemibold: Typeface? = null

    fun getQuicksandBold(context: Context): Typeface {
        return quicksandBold ?: run {
            val appContext = context.applicationContext ?: context
            val tf = try {
                ResourcesCompat.getFont(appContext, R.font.quicksand_bold)
            } catch (_: Exception) {
                Typeface.createFromAsset(appContext.assets, "fonts/Quicksand-Bold.otf")
            } ?: Typeface.DEFAULT_BOLD
            quicksandBold = tf
            tf
        }
    }

    fun getProximaLight(context: Context): Typeface {
        return proximaLight ?: run {
            val appContext = context.applicationContext ?: context
            val tf = try {
                ResourcesCompat.getFont(appContext, R.font.proximanova_light)
            } catch (_: Exception) {
                Typeface.createFromAsset(appContext.assets, "fonts/ProximaNova-Light.otf")
            } ?: Typeface.DEFAULT
            proximaLight = tf
            tf
        }
    }

    fun getProximaSemibold(context: Context): Typeface {
        return proximaSemibold ?: run {
            val appContext = context.applicationContext ?: context
            val tf = try {
                ResourcesCompat.getFont(appContext, R.font.proximanova_semibold)
            } catch (_: Exception) {
                Typeface.createFromAsset(appContext.assets, "fonts/ProximaNova-Semibold.otf")
            } ?: Typeface.DEFAULT_BOLD
            proximaSemibold = tf
            tf
        }
    }
}
