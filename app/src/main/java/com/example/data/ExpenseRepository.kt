package com.example.data

import com.example.model.ExpenseStatus
import com.example.model.PaymentMethodType
import com.example.model.RecurrenceEditOption
import com.example.model.RecurrenceFrequency
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    fun getUpcomingPendingExpenses(currentDateMillis: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getUpcomingPendingExpenses(currentDateMillis)
    }

    fun getExpensesInRange(startMillis: Long, endMillis: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesInRange(startMillis, endMillis)
    }

    fun getBudgetForMonth(month: Int, year: Int): Flow<BudgetEntity?> {
        return budgetDao.getBudgetForMonth(month, year)
    }

    suspend fun getExpenseById(id: Long): ExpenseEntity? = expenseDao.getExpenseById(id)

    suspend fun insertExpense(
        name: String,
        plannedAmount: Double,
        actualAmount: Double?,
        categoryId: Long,
        dateMillis: Long,
        status: ExpenseStatus,
        paymentMethod: PaymentMethodType,
        notes: String?,
        isRecurring: Boolean,
        recurrenceFrequency: RecurrenceFrequency,
        numberOfOccurrences: Int = 12
    ): Long {
        if (!isRecurring || recurrenceFrequency == RecurrenceFrequency.NONE) {
            val expense = ExpenseEntity(
                name = name,
                plannedAmount = plannedAmount,
                actualAmount = actualAmount,
                categoryId = categoryId,
                dateMillis = dateMillis,
                status = status,
                paymentMethod = paymentMethod,
                notes = notes,
                isRecurring = false,
                recurrenceFrequency = RecurrenceFrequency.NONE,
                recurrenceSeriesId = null
            )
            return expenseDao.insertExpense(expense)
        } else {
            val seriesId = UUID.randomUUID().toString()
            val occurrences = mutableListOf<ExpenseEntity>()
            val cal = Calendar.getInstance()
            cal.timeInMillis = dateMillis

            for (i in 0 until numberOfOccurrences) {
                val occStatus = if (i == 0) status else ExpenseStatus.PENDING
                val occActualAmount = if (i == 0) actualAmount else null
                
                occurrences.add(
                    ExpenseEntity(
                        name = name,
                        plannedAmount = plannedAmount,
                        actualAmount = occActualAmount,
                        categoryId = categoryId,
                        dateMillis = cal.timeInMillis,
                        status = occStatus,
                        paymentMethod = paymentMethod,
                        notes = notes,
                        isRecurring = true,
                        recurrenceFrequency = recurrenceFrequency,
                        recurrenceSeriesId = seriesId,
                        recurrenceDayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                    )
                )

                // Advance calendar based on frequency
                when (recurrenceFrequency) {
                    RecurrenceFrequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    RecurrenceFrequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    RecurrenceFrequency.BIWEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 15)
                    RecurrenceFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
                    RecurrenceFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
                    RecurrenceFrequency.CUSTOM -> cal.add(Calendar.MONTH, 1)
                    RecurrenceFrequency.NONE -> break
                }
            }

            expenseDao.insertExpenses(occurrences)
            return 1L
        }
    }

    suspend fun updateExpense(
        expense: ExpenseEntity,
        editOption: RecurrenceEditOption = RecurrenceEditOption.ONLY_THIS
    ) {
        val seriesId = expense.recurrenceSeriesId
        if (!expense.isRecurring || seriesId == null || editOption == RecurrenceEditOption.ONLY_THIS) {
            expenseDao.updateExpense(
                expense.copy(
                    updatedAt = System.currentTimeMillis(),
                    // If modified as single occurrence, detach or retain series depending on intent
                    isRecurring = if (editOption == RecurrenceEditOption.ONLY_THIS && !expense.isRecurring) false else expense.isRecurring
                )
            )
        } else if (editOption == RecurrenceEditOption.THIS_AND_FUTURE) {
            val futureExpenses = expenseDao.getExpensesBySeriesIdFromDate(seriesId, expense.dateMillis)
            val updatedList = futureExpenses.map { existing ->
                existing.copy(
                    name = expense.name,
                    plannedAmount = expense.plannedAmount,
                    categoryId = expense.categoryId,
                    paymentMethod = expense.paymentMethod,
                    notes = expense.notes,
                    recurrenceFrequency = expense.recurrenceFrequency,
                    updatedAt = System.currentTimeMillis()
                )
            }
            expenseDao.updateExpenses(updatedList)
        } else if (editOption == RecurrenceEditOption.ALL_SERIES) {
            val allInSeries = expenseDao.getExpensesBySeriesId(seriesId)
            val updatedList = allInSeries.map { existing ->
                existing.copy(
                    name = expense.name,
                    plannedAmount = expense.plannedAmount,
                    categoryId = expense.categoryId,
                    paymentMethod = expense.paymentMethod,
                    notes = expense.notes,
                    recurrenceFrequency = expense.recurrenceFrequency,
                    updatedAt = System.currentTimeMillis()
                )
            }
            expenseDao.updateExpenses(updatedList)
        }
    }

    suspend fun deleteExpense(
        expense: ExpenseEntity,
        deleteOption: RecurrenceEditOption = RecurrenceEditOption.ONLY_THIS
    ) {
        val seriesId = expense.recurrenceSeriesId
        if (!expense.isRecurring || seriesId == null || deleteOption == RecurrenceEditOption.ONLY_THIS) {
            expenseDao.deleteExpense(expense)
        } else if (deleteOption == RecurrenceEditOption.THIS_AND_FUTURE) {
            expenseDao.deleteExpensesBySeriesIdFromDate(seriesId, expense.dateMillis)
        } else if (deleteOption == RecurrenceEditOption.ALL_SERIES) {
            expenseDao.deleteExpensesBySeriesId(seriesId)
        }
    }

    suspend fun markAsPaid(expenseId: Long, actualAmount: Double?) {
        val current = expenseDao.getExpenseById(expenseId) ?: return
        val finalActualAmount = actualAmount ?: current.plannedAmount
        expenseDao.updateExpenseStatus(expenseId, ExpenseStatus.PAID, finalActualAmount)
    }

    suspend fun markAsPending(expenseId: Long) {
        expenseDao.updateExpenseStatus(expenseId, ExpenseStatus.PENDING, null)
    }

    suspend fun duplicateExpense(expense: ExpenseEntity) {
        val duplicate = expense.copy(
            id = 0,
            name = "${expense.name} (Copia)",
            status = ExpenseStatus.PENDING,
            actualAmount = null,
            isRecurring = false,
            recurrenceSeriesId = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        expenseDao.insertExpense(duplicate)
    }

    // Categories
    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    // Budgets
    suspend fun setBudget(month: Int, year: Int, expectedIncome: Double, totalBudget: Double) {
        val existing = budgetDao.getBudgetForMonthOnce(month, year)
        if (existing != null) {
            budgetDao.updateBudget(
                existing.copy(
                    expectedIncome = expectedIncome,
                    totalBudget = totalBudget
                )
            )
        } else {
            budgetDao.insertOrUpdateBudget(
                BudgetEntity(
                    month = month,
                    year = year,
                    expectedIncome = expectedIncome,
                    totalBudget = totalBudget
                )
            )
        }
    }

    suspend fun resetWithDemoData() {
        // Clear all and reseed
        categoryDao.getAllCategories()
    }
}
