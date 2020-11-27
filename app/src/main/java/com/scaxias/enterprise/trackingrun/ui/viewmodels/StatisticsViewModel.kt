package com.scaxias.enterprise.trackingrun.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.scaxias.enterprise.trackingrun.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(
    mainRepository: MainRepository
): ViewModel() {

    val totalTimeRun = mainRepository.fetchTotalTimeInMillis()
    val totalDistance = mainRepository.fetchTotalDistance()
    val totalCaloriesBurned = mainRepository.fetchTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.fetchTotalAvgSpeed()

    val runs = mainRepository.fetchRuns()
}