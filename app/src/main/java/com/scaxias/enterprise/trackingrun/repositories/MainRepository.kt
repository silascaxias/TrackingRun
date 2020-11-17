package com.scaxias.enterprise.trackingrun.repositories

import com.scaxias.enterprise.trackingrun.db.run.dao.RunDAO
import com.scaxias.enterprise.trackingrun.db.run.entities.Run
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDAO: RunDAO
) {
    suspend fun insertRun(run: Run) = runDAO.insert(run)

    suspend fun deleteRun(run: Run) = runDAO.delete(run)

    fun fetchRunsByDate() = runDAO.fetchByDate()

    fun fetchRunsByDistance() = runDAO.fetchByDistance()

    fun fetchRunsByTimeInMillis() = runDAO.fetchByTimeInMillis()

    fun fetchRunsByAvgSpeed() = runDAO.fetchByAvgSpeed()

    fun fetchAllRunsByCaloriesBurned() = runDAO.fetchByCaloriesBurned()

    fun fetchTotalAvgSpeed() = runDAO.fetchTotalAvgSpeed()

    fun fetchTotalDistance() = runDAO.fetchTotalDistance()

    fun fetchTotalCaloriesBurned() = runDAO.fetchTotalCaloriesBurned()

    fun fetchTotalTimeInMillis() = runDAO.fetchTotalTimeInMillis()
}
