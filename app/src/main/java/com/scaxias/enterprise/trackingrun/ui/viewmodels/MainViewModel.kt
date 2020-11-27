package com.scaxias.enterprise.trackingrun.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.scaxias.enterprise.trackingrun.db.run.entities.Run
import com.scaxias.enterprise.trackingrun.other.RunsFilterType
import com.scaxias.enterprise.trackingrun.other.RunsFilterType.*
import com.scaxias.enterprise.trackingrun.repositories.MainRepository
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.round

class MainViewModel @ViewModelInject constructor(
    private val mainRepository: MainRepository
): ViewModel() {

    private val runsData = mainRepository.fetchRuns()

    val runs = MediatorLiveData<List<Run>>()
    val pages = MediatorLiveData<List<Int>>()
    var selectedPage: Int = 1

    var runsPerPage = MutableLiveData<List<Run>>()
    var filterType = DATE

    var idsToDelete = MutableLiveData<MutableList<Int>>()

    init {

        idsToDelete.postValue(mutableListOf())

        pages.addSource(runs) {result ->
            updatePages(result)
        }

        runs.addSource(runsData) { result ->
            result?.let { runs.value = result }
        }
    }

    private fun updatePages(result: List<Run>) {
        val pages = ceil((result.count().toDouble() / 10))
        this.pages.value = (1..pages.toInt()).map { it }.toList()
        selectedPage = 1
    }

    fun applyRunsPerPage(selectedPage: Int = this.selectedPage, result: List<Run> = this.runs.value ?: arrayListOf()) {
        this.selectedPage = selectedPage

        if(runs.value?.count() ?: 0 > 10) {
            val toIndex = selectedPage * 10

            if(selectedPage == ceil(result.count().toDouble() / 10).toInt()) {
                runsPerPage.value = result.subList(toIndex - 10, result.count())
            } else runsPerPage.value = result.subList(toIndex - 10, toIndex)

        } else runsPerPage.value = result
    }

    private fun getFilterProp(filterType: RunsFilterType, it: List<Run>) = when(filterType) {
        DATE -> it.sortedByDescending { run -> run.timestamp }
        RUNNING_TIME ->  it.sortedByDescending { run -> run.timeInMillis }
        AVG_SPEED -> it.sortedByDescending { run -> run.avgSpeedInKMH }
        DISTANCE -> it.sortedByDescending { run -> run.distanceInMeters }
        CALORIES_BURNED -> it.sortedByDescending { run -> run.caloriesBurned }
    }.also {
        this.filterType = filterType
    }

    fun applyFilter(filterType: RunsFilterType) = runsPerPage.value?.let {
        runsPerPage.value = getFilterProp(filterType,it)
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

    fun deleteRun(id: Int) = viewModelScope.launch {
        mainRepository.deleteRunsByIds(arrayOf(id))
    }

    fun deleteAllRuns(ids: Array<Int>) = viewModelScope.launch {
        mainRepository.deleteRunsByIds(ids)
    }
}