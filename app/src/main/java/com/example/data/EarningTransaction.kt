package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "earning_transactions")
data class EarningTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val points: Int,              // Positive for earnings, negative for redemptions
    val type: String,              // "CHECK_IN", "VIDEO", "SURVEY", "MATH_GAME", "REFERRAL_REWARD", "REDEEM"
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""       // Additional description or meta information
)
