package com.faces.core.util

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.faces.core.R

object FontProvider {

    private var quicksandBold: Typeface? = null
    private var proximaLight: Typeface? = null
    private var proximaSemibold: Typeface? = null

    fun getQuicksandBold(context: Context): Typeface {
        return quicksandBold ?: run {
            val tf = try {
                ResourcesCompat.getFont(context, R.font.quicksand_bold)
            } catch (e: Exception) {
                Typeface.createFromAsset(context.assets, "fonts/Quicksand-Bold.otf")
            } ?: Typeface.DEFAULT_BOLD
            quicksandBold = tf
            tf
        }
    }

    fun getProximaLight(context: Context): Typeface {
        return proximaLight ?: run {
            val tf = try {
                ResourcesCompat.getFont(context, R.font.proximanova_light)
            } catch (e: Exception) {
                Typeface.createFromAsset(context.assets, "fonts/ProximaNova-Light.otf")
            } ?: Typeface.DEFAULT
            proximaLight = tf
            tf
        }
    }

    fun getProximaSemibold(context: Context): Typeface {
        return proximaSemibold ?: run {
            val tf = try {
                ResourcesCompat.getFont(context, R.font.proximanova_semibold)
            } catch (e: Exception) {
                Typeface.createFromAsset(context.assets, "fonts/ProximaNova-Semibold.otf")
            } ?: Typeface.DEFAULT_BOLD
            proximaSemibold = tf
            tf
        }
    }
}
