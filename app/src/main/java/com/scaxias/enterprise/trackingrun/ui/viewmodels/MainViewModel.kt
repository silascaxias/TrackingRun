package com.scaxias.enterprise.trackingrun.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scaxias.enterprise.trackingrun.db.run.entities.Run
import com.scaxias.enterprise.trackingrun.other.RunsFilterType
import com.scaxias.enterprise.trackingrun.other.RunsFilterType.*
import com.scaxias.enterprise.trackingrun.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    private val mainRepository: MainRepository
): ViewModel() {

    private val runsByDate = mainRepository.fetchRunsByDate()
    private val runsByDistance = mainRepository.fetchRunsByDistance()
    private val runsByCaloriesBurned = mainRepository.fetchRunsByCaloriesBurned()
    private val runsByTimesInMillis = mainRepository.fetchRunsByTimeInMillis()
    private val runsByAvgSpeed = mainRepository.fetchRunsByAvgSpeed()

    val runs = MediatorLiveData<List<Run>>()

    var filterType = DATE

    init {
        runs.addSource(runsByDate) { result ->
            if(filterType == DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsByDistance) { result ->
            if(filterType == DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsByCaloriesBurned) { result ->
            if(filterType == CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsByTimesInMillis) { result ->
            if(filterType == RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsByAvgSpeed) { result ->
            if(filterType == AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
    }

    fun applyFilter(filterType: RunsFilterType) = when(filterType) {
        DATE -> runsByDate.value?.let { runs.value = it }
        RUNNING_TIME -> runsByTimesInMillis.value?.let { runs.value = it }
        AVG_SPEED -> runsByAvgSpeed.value?.let { runs.value = it }
        DISTANCE -> runsByDistance.value?.let { runs.value = it }
        CALORIES_BURNED -> runsByCaloriesBurned.value?.let { runs.value = it }
    }.also { this.filterType = filterType }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}