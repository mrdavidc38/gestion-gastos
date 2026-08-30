package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.ExpenseStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis ASC, id ASC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis ASC")
    fun getExpensesInRange(startMillis: Long, endMillis: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE recurrenceSeriesId = :seriesId ORDER BY dateMillis ASC")
    suspend fun getExpensesBySeriesId(seriesId: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE recurrenceSeriesId = :seriesId AND dateMillis >= :fromDateMillis ORDER BY dateMillis ASC")
    suspend fun getExpensesBySeriesIdFromDate(seriesId: String, fromDateMillis: Long): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>): List<Long>

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpenses(expenses: List<ExpenseEntity>)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM expenses WHERE recurrenceSeriesId = :seriesId")
    suspend fun deleteExpensesBySeriesId(seriesId: String)

    @Query("DELETE FROM expenses WHERE recurrenceSeriesId = :seriesId AND dateMillis >= :fromDateMillis")
    suspend fun deleteExpensesBySeriesIdFromDate(seriesId: String, fromDateMillis: Long)

    @Query("UPDATE expenses SET status = :newStatus, actualAmount = :actualAmount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateExpenseStatus(id: Long, newStatus: ExpenseStatus, actualAmount: Double?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM expenses WHERE dateMillis >= :currentDateMillis AND status = 'PENDING' ORDER BY dateMillis ASC")
    fun getUpcomingPendingExpenses(currentDateMillis: Long): Flow<List<ExpenseEntity>>
}
