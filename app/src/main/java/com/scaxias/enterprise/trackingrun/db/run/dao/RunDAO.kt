package com.scaxias.enterprise.trackingrun.db.run.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.scaxias.enterprise.trackingrun.db.run.entities.Run

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: Run)

    @Delete
    suspend fun delete(run: Run)

    @Query("SELECT * FROM run ORDER BY timestamp DESC")
    fun fetchByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM run ORDER BY timeInMillis DESC")
    fun fetchByTimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM run ORDER BY caloriesBurned DESC")
    fun fetchByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM run ORDER BY avgSpeedInKMH DESC")
    fun fetchByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM run ORDER BY distanceInMeters DESC")
    fun fetchByDistance(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillis) from run")
    fun fetchTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) from run")
    fun fetchTotalCaloriesBurned(): LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) from run")
    fun fetchTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeedInKMH) from run")
    fun fetchTotalAvgSpeed(): LiveData<Float>
}