package com.example.data

import androidx.room.TypeConverter
import com.example.model.ExpenseStatus
import com.example.model.PaymentMethodType
import com.example.model.RecurrenceFrequency

class Converters {
    @TypeConverter
    fun fromExpenseStatus(status: ExpenseStatus?): String? = status?.name

    @TypeConverter
    fun toExpenseStatus(value: String?): ExpenseStatus =
        value?.let {
            try {
                ExpenseStatus.valueOf(it)
            } catch (e: Exception) {
                ExpenseStatus.PENDING
            }
        } ?: ExpenseStatus.PENDING

    @TypeConverter
    fun fromRecurrenceFrequency(freq: RecurrenceFrequency?): String? = freq?.name

    @TypeConverter
    fun toRecurrenceFrequency(value: String?): RecurrenceFrequency =
        value?.let {
            try {
                RecurrenceFrequency.valueOf(it)
            } catch (e: Exception) {
                RecurrenceFrequency.NONE
            }
        } ?: RecurrenceFrequency.NONE

    @TypeConverter
    fun fromPaymentMethodType(method: PaymentMethodType?): String? = method?.name

    @TypeConverter
    fun toPaymentMethodType(value: String?): PaymentMethodType =
        value?.let {
            try {
                PaymentMethodType.valueOf(it)
            } catch (e: Exception) {
                PaymentMethodType.DEBIT_CARD
            }
        } ?: PaymentMethodType.DEBIT_CARD
}
