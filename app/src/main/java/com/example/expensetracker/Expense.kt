package com.example.expensetracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val time: String,
    val purpose: String,
    val amount: Double,
    val isSettlement: Boolean = false,
    val settlementType: String? = null // "weekly" or "monthly"
)
