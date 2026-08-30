package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    fun getBudgetForMonth(month: Int, year: Int): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetForMonthOnce(month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)
}
