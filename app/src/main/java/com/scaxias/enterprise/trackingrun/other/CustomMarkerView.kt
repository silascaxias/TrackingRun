package com.scaxias.enterprise.trackingrun.other

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.db.run.entities.Run
import com.scaxias.enterprise.trackingrun.other.utils.TrackingUtils
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView (
    val runs: List<Run>,
    c: Context,
    layoutId: Int
): MarkerView(c, layoutId) {

    override fun getOffset(): MPPointF = MPPointF(-width / 2f, -height.toFloat())

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e == null) return
        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        textViewDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKMH}${resources.getString(R.string.km_h_text)}"
        textViewAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f}${resources.getString(R.string.km_text)}"
        textViewDistance.text = distanceInKm

        textViewDuration.text = TrackingUtils.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurned}${resources.getString(R.string.zero_kcal_text)}"
        textViewCaloriesBurned.text = caloriesBurned
    }
}