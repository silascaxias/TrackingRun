package com.scaxias.enterprise.trackingrun.entities

class TimeControl (
    var isTimerEnabled: Boolean = false,
    var lapTime: Long = 0L,
    var timeRun: Long = 0L,
    var timeStarted: Long = 0L,
    var lastSecondTimestamp: Long = 0L
) {
    fun timeRunInMillis(): Long = timeRun + lapTime
}