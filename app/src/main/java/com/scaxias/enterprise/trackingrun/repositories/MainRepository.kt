package com.scaxias.enterprise.trackingrun.repositories

import com.scaxias.enterprise.trackingrun.db.run.dao.RunDAO
import com.scaxias.enterprise.trackingrun.db.run.entities.Run
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDAO: RunDAO
) {
    suspend fun insertRun(run: Run) = runDAO.insert(run)

    suspend fun deleteRunsByIds(ids: Array<Int>) = runDAO.deleteRunsByIds(ids)

    fun fetchRuns() = runDAO.fetchAllRuns()

    fun fetchTotalAvgSpeed() = runDAO.fetchTotalAvgSpeed()

    fun fetchTotalDistance() = runDAO.fetchTotalDistance()

    fun fetchTotalCaloriesBurned() = runDAO.fetchTotalCaloriesBurned()

    fun fetchTotalTimeInMillis() = runDAO.fetchTotalTimeInMillis()
}
