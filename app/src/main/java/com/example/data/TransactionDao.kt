package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM earning_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<EarningTransaction>>

    @Query("SELECT COALESCE(SUM(points), 0) FROM earning_transactions")
    fun observeTotalPoints(): Flow<Int>

    @Query("SELECT COUNT(*) FROM earning_transactions WHERE type = :type AND timestamp >= :startOfDay")
    suspend fun getTransactionCountToday(type: String, startOfDay: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: EarningTransaction)

    @Query("DELETE FROM earning_transactions")
    suspend fun clearAll()
}
