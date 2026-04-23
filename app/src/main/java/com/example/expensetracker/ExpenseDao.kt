package com.example.expensetracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: Long): Flow<Expense?>

    @Query("SELECT * FROM expenses WHERE time BETWEEN :startDate AND :endDate AND isSettlement = 0")
    suspend fun getExpensesBetweenDates(startDate: String, endDate: String): List<Expense>

    @Query("SELECT * FROM expenses WHERE isSettlement = 1 AND settlementType = :type ORDER BY time DESC")
    suspend fun getSettlementsByType(type: String): List<Expense>
}
