package com.scaxias.enterprise.trackingrun.other.utils

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.scaxias.enterprise.trackingrun.R

object ColorUtils {
    private fun isPortrait(context: Context) = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    fun getChartLabelColor(context: Context) =
            if(isPortrait(context)) ContextCompat.getColor(context, R.color.blueColor)
            else ContextCompat.getColor(context, android.R.color.white)

    fun blackColor(context: Context) = ContextCompat.getColor(context, android.R.color.black)
}