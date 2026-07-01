package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ResqReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResqReportDao {
    @Query("SELECT * FROM resq_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ResqReportEntity>>

    @Query("SELECT * FROM resq_reports WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedReports(): List<ResqReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ResqReportEntity)

    @Update
    suspend fun updateReport(report: ResqReportEntity)
    
    @Query("UPDATE resq_reports SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)
}
