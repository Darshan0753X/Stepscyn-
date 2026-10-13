package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyStepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepDao {
    @Query("SELECT * FROM daily_steps ORDER BY date DESC")
    fun getAllDailySteps(): Flow<List<DailyStepRecord>>

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    fun getStepRecordByDate(date: String): Flow<DailyStepRecord?>

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getStepRecordSync(date: String): DailyStepRecord?

    @Query("SELECT * FROM daily_steps ORDER BY date DESC LIMIT :limit")
    fun getRecentDays(limit: Int): Flow<List<DailyStepRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: DailyStepRecord)

    @Query("SELECT SUM(stepCount) FROM daily_steps")
    fun getTotalLifetimeSteps(): Flow<Long?>

    @Query("SELECT MAX(stepCount) FROM daily_steps")
    fun getBestDaySteps(): Flow<Long?>

    @Query("DELETE FROM daily_steps")
    suspend fun clearAll()
}
