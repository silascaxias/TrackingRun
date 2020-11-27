package com.scaxias.enterprise.trackingrun.db.run.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.scaxias.enterprise.trackingrun.db.run.entities.Run

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: Run)

    @Query("DELETE FROM run WHERE id IN (:ids)")
    suspend fun deleteRunsByIds(ids: Array<Int>): Int

    @Query("SELECT * FROM run")
    fun fetchAllRuns(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillis) from run")
    fun fetchTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) from run")
    fun fetchTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) from run")
    fun fetchTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKMH) from run")
    fun fetchTotalAvgSpeed(): LiveData<Float>
}