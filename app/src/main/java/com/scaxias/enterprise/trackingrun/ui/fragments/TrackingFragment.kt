package com.scaxias.enterprise.trackingrun.ui.fragments

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.db.run.entities.Run
import com.scaxias.enterprise.trackingrun.extensions.gone
import com.scaxias.enterprise.trackingrun.extensions.visible
import com.scaxias.enterprise.trackingrun.other.Constants.ACTION_PAUSE_SERVICE
import com.scaxias.enterprise.trackingrun.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.scaxias.enterprise.trackingrun.other.Constants.ACTION_STOP_SERVICE
import com.scaxias.enterprise.trackingrun.other.Constants.MAP_ZOOM
import com.scaxias.enterprise.trackingrun.other.Constants.POLYLINE_COLOR
import com.scaxias.enterprise.trackingrun.other.Constants.POLYLINE_WIDTH
import com.scaxias.enterprise.trackingrun.other.utils.ConfirmDialogFragment
import com.scaxias.enterprise.trackingrun.other.utils.TrackingUtils
import com.scaxias.enterprise.trackingrun.services.Polyline
import com.scaxias.enterprise.trackingrun.services.TrackingService
import com.scaxias.enterprise.trackingrun.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {

    private val cancelTrackingDialogTag = "cancelTrackingDialogTag"

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var lastLatLngVisible: LatLng? = null
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var curTimeInMillis = 0L

    private var menu: Menu? = null

    @set:Inject
    var weight = 80f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        lastLatLngVisible = null

        if(savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                cancelTrackingDialogTag) as ConfirmDialogFragment?
            cancelTrackingDialog?.setPositiveListener { stopRun() }
        }
        buttonRun.setOnClickListener { toggleRun() }
        buttonFinish.setOnClickListener {
            seeAllTrack()
            finishRun()
        }
        mapView.getMapAsync {
            map = it
            addAllRoutes()
        }
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, { updateTracking(it) })
        TrackingService.pathPoints.observe(viewLifecycleOwner, {
            pathPoints = it
            addLatestRoute()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {
            curTimeInMillis = it
            val formattedTime = TrackingUtils.getFormattedStopWatchTime(curTimeInMillis, true)
            textViewTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
    }

    private fun showCancelDialog() = ConfirmDialogFragment(
                getString(R.string.cancel_run_text),
                getString(R.string.cancel_run_text_confirm),
                getString(R.string.yes_text),
                { stopRun() },
                getString(R.string.no_text)
    ) .show(parentFragmentManager, cancelTrackingDialogTag)

    private fun stopRun() {
        textViewTimer.text = getString(R.string.empty_time_milliseconds)
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if(isTracking.not() and (curTimeInMillis > 0L)) {
            buttonRun.text = getString(R.string.start_text)
            buttonFinish.visible()
        } else if(isTracking) {
            buttonRun.text = getString(R.string.stop_text)
            menu?.getItem(0)?.isVisible = true
            buttonFinish.gone()
        }
    }

    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            pathPoints.last().last(),
                            MAP_ZOOM
                    )
            )
        }
    }

    private fun seeAllTrack() {
        val bounds = LatLngBounds.builder()

        pathPoints.forEach { polyline ->
            polyline.forEach { location ->
                bounds.include(location)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun finishRun() {
        map?.snapshot { bitmap ->
            var distanceInMeters = 0
            pathPoints.forEach { polyline ->
                distanceInMeters += TrackingUtils.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            viewModel.insertRun(Run(bitmap, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned))
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                getString(R.string.run_success_save_text),
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun addAllRoutes() {
        for(polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestRoute() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = if(lastLatLngVisible != null) lastLatLngVisible
                                else pathPoints.last()[pathPoints.last().size - 2]

            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .add(preLastLatLng)
                    .add(lastLatLng)

            map?.addPolyline(polylineOptions)

            if(lastLatLngVisible != null) lastLatLngVisible = null
        }
        moveCameraToUser()
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis > 0L) this.menu?.getItem(0)?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.itemCancelTracking) showCancelDialog()
        return super.onOptionsItemSelected(item)
    }
    
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        lastLatLngVisible = try {  pathPoints.last().last() } catch (_: Exception) { null }
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        lastLatLngVisible = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}