package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.ExpenseStatus
import com.example.model.PaymentMethodType
import com.example.model.RecurrenceFrequency

@Entity(
    tableName = "expenses",
    indices = [
        Index("categoryId"),
        Index("dateMillis"),
        Index("status"),
        Index("recurrenceSeriesId")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val plannedAmount: Double,
    val actualAmount: Double? = null,
    val categoryId: Long,
    val dateMillis: Long,
    val status: ExpenseStatus = ExpenseStatus.PENDING,
    val paymentMethod: PaymentMethodType = PaymentMethodType.DEBIT_CARD,
    val notes: String? = null,
    val isRecurring: Boolean = false,
    val recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val recurrenceSeriesId: String? = null,
    val recurrenceInterval: Int = 1,
    val recurrenceDayOfMonth: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
