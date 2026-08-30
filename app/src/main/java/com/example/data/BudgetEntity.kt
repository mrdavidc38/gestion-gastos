package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val month: Int, // 1 - 12
    val year: Int,  // e.g. 2026
    val expectedIncome: Double,
    val totalBudget: Double
)
