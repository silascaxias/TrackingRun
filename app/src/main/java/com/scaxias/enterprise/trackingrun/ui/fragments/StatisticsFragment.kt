package com.scaxias.enterprise.trackingrun.ui.fragments

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.other.CustomMarkerView
import com.scaxias.enterprise.trackingrun.other.utils.ColorUtils.blackColor
import com.scaxias.enterprise.trackingrun.other.utils.ColorUtils.getChartLabelColor
import com.scaxias.enterprise.trackingrun.other.utils.TrackingUtils
import com.scaxias.enterprise.trackingrun.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        setupObservers()
        setupBarChart()
    }

    private fun setupBarChart() {
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = getChartLabelColor(requireContext())
            textColor = getChartLabelColor(requireContext())
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = getChartLabelColor(requireContext())
            textColor = getChartLabelColor(requireContext())
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = getChartLabelColor(requireContext())
            textColor = getChartLabelColor(requireContext())
            setDrawGridLines(false)
        }
        barChart.apply {
            setNoDataText(getString(R.string.no_chart_data_text))
            setNoDataTextColor(getChartLabelColor(requireContext()))
            description.text = getString(R.string.avg_speed_over_time_text)
            description.textColor = blackColor(requireContext())
            legend.isEnabled = false
        }
    }

    private fun setupObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, {
            it?.let {
                textViewTotalTime.text = TrackingUtils.getFormattedStopWatchTime(it)
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}${getString(R.string.km_text)}"
                textViewTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}${getString(R.string.km_h_text)}"
                textViewAverageSpeed.text = avgSpeedString
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, {
            it?.let {
                val totalCalories = "${it}${getString(R.string.kcal_text)}"
                textViewTotalCalories.text = totalCalories
            }
        })
        viewModel.runsByDate.observe(viewLifecycleOwner, {
            it?.let {
                val allAvgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                val barDataSet = BarDataSet(allAvgSpeeds, getString(R.string.avg_speed_over_time_text)).apply {
                    valueTextColor = getChartLabelColor(requireContext())
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                barChart.data = BarData(barDataSet)
                barChart.marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
                barChart.invalidate()
            }
        })
    }
}