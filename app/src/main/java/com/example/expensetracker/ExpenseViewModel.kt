package com.example.expensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ExpenseDatabase.getDatabase(application)
    private val expenseDao = database.expenseDao()

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun insertExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.insert(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.update(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.delete(expense)
        }
    }

    fun getExpenseById(id: Long): Flow<Expense?> {
        return expenseDao.getExpenseById(id)
    }

    suspend fun getExpensesBetweenDates(startDate: String, endDate: String): List<Expense> {
        return expenseDao.getExpensesBetweenDates(startDate, endDate)
    }

    fun performWeeklySettlement() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            calendar.add(Calendar.DAY_OF_WEEK, -6)
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val expenses = getExpensesBetweenDates(startDate, endDate)
            val totalAmount = expenses.sumOf { it.amount }
            
            if (totalAmount > 0) {
                val settlement = Expense(
                    time = endDate,
                    purpose = "周结算 ($startDate 至 $endDate)",
                    amount = totalAmount,
                    isSettlement = true,
                    settlementType = "weekly"
                )
                expenseDao.insert(settlement)
            }
        }
    }

    fun performMonthlySettlement() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            
            val startCalendar = Calendar.getInstance().apply {
                set(year, month, 1)
            }
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startCalendar.time)
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val expenses = getExpensesBetweenDates(startDate, endDate)
            val totalAmount = expenses.sumOf { it.amount }
            
            if (totalAmount > 0) {
                val monthName = SimpleDateFormat("yyyy年MM月", Locale.getDefault()).format(calendar.time)
                val settlement = Expense(
                    time = endDate,
                    purpose = "月结算 ($monthName)",
                    amount = totalAmount,
                    isSettlement = true,
                    settlementType = "monthly"
                )
                expenseDao.insert(settlement)
            }
        }
    }
}
