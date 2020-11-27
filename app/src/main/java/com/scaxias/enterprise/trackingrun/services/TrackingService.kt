package com.scaxias.enterprise.trackingrun.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.scaxias.enterprise.trackingrun.R
import com.scaxias.enterprise.trackingrun.entities.TimeControl
import com.scaxias.enterprise.trackingrun.other.Constants.ACTION_PAUSE_SERVICE
import com.scaxias.enterprise.trackingrun.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.scaxias.enterprise.trackingrun.other.Constants.ACTION_STOP_SERVICE
import com.scaxias.enterprise.trackingrun.other.Constants.FASTEST_LOCATION_INTERVAL
import com.scaxias.enterprise.trackingrun.other.Constants.LOCATION_UPDATE_INTERVAL
import com.scaxias.enterprise.trackingrun.other.Constants.NOTIFICATION_ACTION_PAUSE
import com.scaxias.enterprise.trackingrun.other.Constants.NOTIFICATION_ACTION_RESUME
import com.scaxias.enterprise.trackingrun.other.Constants.NOTIFICATION_CHANNEL_ID
import com.scaxias.enterprise.trackingrun.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.scaxias.enterprise.trackingrun.other.Constants.NOTIFICATION_ID
import com.scaxias.enterprise.trackingrun.other.Constants.TIMER_UPDATE_INTERVAL
import com.scaxias.enterprise.trackingrun.other.utils.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService() {

    private var isFirstRun = true
    private var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        //fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, {
            updateLocationTracking(it)
            updateNotificationTrackingStage(it)
        })
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else startTimer()
                }
                ACTION_PAUSE_SERVICE -> pauseService()
                ACTION_STOP_SERVICE -> killService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var timeControl = TimeControl()

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeControl.timeStarted = System.currentTimeMillis()
        timeControl.isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value == true) {
                timeControl.lapTime = System.currentTimeMillis() - timeControl.timeStarted
                timeRunInMillis.postValue(timeControl.timeRunInMillis())
                delay(TIMER_UPDATE_INTERVAL)
                timeRunInMillis.value?.let {
                    if (it >= timeControl.lastSecondTimestamp + 1000L) {
                        timeRunInSeconds.value?.let { seconds -> timeRunInSeconds.postValue(seconds + 1) }
                        timeControl.lastSecondTimestamp += 1000L
                    }
                }
            }
            timeControl.timeRun += timeControl.lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        timeControl.isTimerEnabled = false
    }

    private fun updateNotificationTrackingStage(isTracking: Boolean) {
        val notificationActionText = if(isTracking) NOTIFICATION_ACTION_PAUSE else NOTIFICATION_ACTION_RESUME
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>()) // Remove old actions on update notification with de Old action
        }

        if(serviceKilled.not()) {
            currentNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtils.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallBack,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
        }
    }

    private val locationCallBack = object: LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            isTracking.value?.let {
                if(it) {
                    result?.locations?.let { locations ->
                        for(location in locations) {
                            addPathPoint(location)
                            Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                        }

                    }
                }
            }

        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            pathPoints.value?.apply {
                last().add(LatLng(location.latitude, location.longitude))
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() {
        pathPoints.value?.apply {
            add(mutableListOf())
            pathPoints.postValue(this)
        } ?: pathPoints.postValue(mutableListOf(mutableListOf()))
    }

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, {
            if(serviceKilled.not()) {
                val notification = currentNotificationBuilder
                        .setContentText(TrackingUtils.getFormattedStopWatchTime(it * 1000L, false))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}