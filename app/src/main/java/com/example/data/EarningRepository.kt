package com.example.data

import kotlinx.coroutines.flow.Flow

class EarningRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<EarningTransaction>> = transactionDao.getAllTransactions()

    val totalPoints: Flow<Int> = transactionDao.observeTotalPoints()

    suspend fun insertTransaction(transaction: EarningTransaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun getCountToday(type: String, startOfDay: Long): Int {
        return transactionDao.getTransactionCountToday(type, startOfDay)
    }

    suspend fun clearAll() {
        transactionDao.clearAll()
    }
}
