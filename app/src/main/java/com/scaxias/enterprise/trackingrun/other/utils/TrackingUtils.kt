package com.scaxias.enterprise.trackingrun.other.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import android.view.View
import com.scaxias.enterprise.trackingrun.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit


object TrackingUtils {
    fun hasLocationPermissions(context: Context) =
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    fun calculatePolylineLength(polyline: Polyline): Float {
        var distance = 0f

        for(i in 0..polyline.size - 2) {
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]

            val result = FloatArray(1)

            Location.distanceBetween(
                    pos1.latitude,
                    pos1.longitude,
                    pos2.latitude,
                    pos2.longitude,
                    result
            )
            distance += result[0]
        }

        return distance
    }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10

        return getConcatTime(hours, minutes, seconds, milliseconds, includeMillis)
    }

    private fun getConcatTime(hours: Long, minutes: Long, seconds: Long, milliseconds: Long = 0L, includeMillis: Boolean): String {
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds" +
                if (!includeMillis) "" else "${if(milliseconds < 10) ":0" else ":"}$milliseconds"
    }

    fun loadBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }
}
