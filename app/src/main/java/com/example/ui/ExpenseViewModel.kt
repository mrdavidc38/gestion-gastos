package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BudgetEntity
import com.example.data.CategoryEntity
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.model.CurrencyOption
import com.example.model.ExpenseStatus
import com.example.model.PaymentMethodType
import com.example.model.RecurrenceEditOption
import com.example.model.RecurrenceFrequency
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimeViewMode(val title: String) {
    DAY("Día"),
    WEEK("Semana"),
    MONTH("Mes"),
    ALL("Todos")
}

enum class SortOption(val title: String) {
    DATE_ASC("Fecha (Próximos primero)"),
    DATE_DESC("Fecha (Más lejanos primero)"),
    AMOUNT_DESC("Mayor valor"),
    AMOUNT_ASC("Menor valor"),
    CATEGORY("Categoría")
}

data class ExpenseFilter(
    val searchQuery: String = "",
    val categoryId: Long? = null,
    val status: ExpenseStatus? = null,
    val paymentMethod: PaymentMethodType? = null,
    val onlyRecurring: Boolean = false,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val sortOption: SortOption = SortOption.DATE_ASC
)

data class ReminderSettings(
    val sameDay: Boolean = true,
    val oneDayBefore: Boolean = true,
    val threeDaysBefore: Boolean = true,
    val sevenDaysBefore: Boolean = false
)

data class CategoryStat(
    val category: CategoryEntity,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int
)

data class TimelineDayGroup(
    val dateMillis: Long,
    val friendlyLabel: String,
    val expenses: List<ExpenseEntity>,
    val totalPlanned: Double,
    val totalPaid: Double,
    val totalPending: Double
)

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    // Current Date State (defaults to current system time or preset 2026-08-30)
    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    private val _selectedMonth = MutableStateFlow(
        Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }.get(Calendar.MONTH) + 1
    )
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(
        Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }.get(Calendar.YEAR)
    )
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Time View
    private val _timeViewMode = MutableStateFlow(TimeViewMode.MONTH)
    val timeViewMode: StateFlow<TimeViewMode> = _timeViewMode.asStateFlow()

    // Filters & Search
    private val _filter = MutableStateFlow(ExpenseFilter())
    val filter: StateFlow<ExpenseFilter> = _filter.asStateFlow()

    // Settings
    private val _currency = MutableStateFlow(CurrencyOption.COP)
    val currency: StateFlow<CurrencyOption> = _currency.asStateFlow()

    private val _reminderSettings = MutableStateFlow(ReminderSettings())
    val reminderSettings: StateFlow<ReminderSettings> = _reminderSettings.asStateFlow()

    // Room Flows
    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Month Budget
    val currentMonthBudget: StateFlow<BudgetEntity?> = combine(
        allBudgets, _selectedMonth, _selectedYear
    ) { budgets, month, year ->
        budgets.find { it.month == month && it.year == year }
            ?: BudgetEntity(month = month, year = year, expectedIncome = 6000000.0, totalBudget = 4000000.0)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BudgetEntity(month = 8, year = 2026, expectedIncome = 6000000.0, totalBudget = 4000000.0)
    )

    // Filtered Expenses for current view mode & filters
    private val dateInfoFlow = combine(_selectedDateMillis, _selectedMonth, _selectedYear) { dateMillis, month, year ->
        Triple(dateMillis, month, year)
    }

    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses, _timeViewMode, dateInfoFlow, _filter
    ) { expenses, mode, dateInfo, filter ->
        val (dateMillis, month, year) = dateInfo
        var list = expenses

        // 1. Filter by Time View Mode
        when (mode) {
            TimeViewMode.DAY -> {
                val start = FormatUtils.normalizeToStartOfDay(dateMillis)
                val end = FormatUtils.getEndOfDay(dateMillis)
                list = list.filter { it.dateMillis in start..end }
            }
            TimeViewMode.WEEK -> {
                val start = FormatUtils.getStartOfWeek(dateMillis)
                val end = FormatUtils.getEndOfWeek(dateMillis)
                list = list.filter { it.dateMillis in start..end }
            }
            TimeViewMode.MONTH -> {
                val start = FormatUtils.getStartOfMonth(month, year)
                val end = FormatUtils.getEndOfMonth(month, year)
                list = list.filter { it.dateMillis in start..end }
            }
            TimeViewMode.ALL -> {
                // Keep all
            }
        }

        // 2. Search query
        if (filter.searchQuery.isNotBlank()) {
            val q = filter.searchQuery.trim().lowercase()
            list = list.filter { it.name.lowercase().contains(q) || (it.notes?.lowercase()?.contains(q) == true) }
        }

        // 3. Category
        if (filter.categoryId != null) {
            list = list.filter { it.categoryId == filter.categoryId }
        }

        // 4. Status
        if (filter.status != null) {
            list = list.filter { it.status == filter.status }
        }

        // 5. Payment Method
        if (filter.paymentMethod != null) {
            list = list.filter { it.paymentMethod == filter.paymentMethod }
        }

        // 6. Only Recurring
        if (filter.onlyRecurring) {
            list = list.filter { it.isRecurring }
        }

        // 7. Amount Range
        if (filter.minAmount != null) {
            list = list.filter { it.plannedAmount >= filter.minAmount }
        }
        if (filter.maxAmount != null) {
            list = list.filter { it.plannedAmount <= filter.maxAmount }
        }

        // 8. Sorting
        when (filter.sortOption) {
            SortOption.DATE_ASC -> list.sortedWith(compareBy({ it.dateMillis }, { it.id }))
            SortOption.DATE_DESC -> list.sortedWith(compareByDescending<ExpenseEntity> { it.dateMillis }.thenByDescending { it.id })
            SortOption.AMOUNT_DESC -> list.sortedByDescending { it.actualAmount ?: it.plannedAmount }
            SortOption.AMOUNT_ASC -> list.sortedBy { it.actualAmount ?: it.plannedAmount }
            SortOption.CATEGORY -> list.sortedBy { it.categoryId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Timeline Grouped list
    val timelineGroups: StateFlow<List<TimelineDayGroup>> = allExpenses.combine(_selectedDateMillis) { expenses, _ ->
        val sorted = expenses.sortedBy { it.dateMillis }
        val grouped = sorted.groupBy { FormatUtils.normalizeToStartOfDay(it.dateMillis) }
        grouped.map { (dayMillis, dayExpenses) ->
            val planned = dayExpenses.sumOf { it.plannedAmount }
            val paid = dayExpenses.filter { it.status == ExpenseStatus.PAID }.sumOf { it.actualAmount ?: it.plannedAmount }
            val pending = dayExpenses.filter { it.status != ExpenseStatus.PAID }.sumOf { it.plannedAmount }
            TimelineDayGroup(
                dateMillis = dayMillis,
                friendlyLabel = FormatUtils.formatFriendlyDate(dayMillis),
                expenses = dayExpenses,
                totalPlanned = planned,
                totalPaid = paid,
                totalPending = pending
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Committed Money (Dinero Comprometido) in next 7, 15, 30 days
    val committedNext7Days: StateFlow<Double> = allExpenses.combine(_selectedDateMillis) { expenses, _ ->
        val now = FormatUtils.normalizeToStartOfDay(System.currentTimeMillis())
        val sevenDaysAhead = now + (7L * 24 * 60 * 60 * 1000)
        expenses.filter { it.dateMillis in now..sevenDaysAhead && it.status != ExpenseStatus.PAID }
            .sumOf { it.plannedAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val committedNext15Days: StateFlow<Double> = allExpenses.combine(_selectedDateMillis) { expenses, _ ->
        val now = FormatUtils.normalizeToStartOfDay(System.currentTimeMillis())
        val fifteenDaysAhead = now + (15L * 24 * 60 * 60 * 1000)
        expenses.filter { it.dateMillis in now..fifteenDaysAhead && it.status != ExpenseStatus.PAID }
            .sumOf { it.plannedAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val committedNext30Days: StateFlow<Double> = allExpenses.combine(_selectedDateMillis) { expenses, _ ->
        val now = FormatUtils.normalizeToStartOfDay(System.currentTimeMillis())
        val thirtyDaysAhead = now + (30L * 24 * 60 * 60 * 1000)
        expenses.filter { it.dateMillis in now..thirtyDaysAhead && it.status != ExpenseStatus.PAID }
            .sumOf { it.plannedAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Month Stats for Dashboard
    val currentMonthExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses, _selectedMonth, _selectedYear
    ) { expenses, month, year ->
        val start = FormatUtils.getStartOfMonth(month, year)
        val end = FormatUtils.getEndOfMonth(month, year)
        expenses.filter { it.dateMillis in start..end }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMonthPlanned: StateFlow<Double> = currentMonthExpenses.combine(_selectedMonth) { expenses, _ ->
        expenses.sumOf { it.plannedAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalMonthPaid: StateFlow<Double> = currentMonthExpenses.combine(_selectedMonth) { expenses, _ ->
        expenses.filter { it.status == ExpenseStatus.PAID }.sumOf { it.actualAmount ?: it.plannedAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalMonthPending: StateFlow<Double> = currentMonthExpenses.combine(_selectedMonth) { expenses, _ ->
        expenses.filter { it.status != ExpenseStatus.PAID }.sumOf { it.plannedAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalMonthProjected: StateFlow<Double> = combine(totalMonthPaid, totalMonthPending) { paid, pending ->
        paid + pending
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Upcoming planned expenses (Future or Pending today)
    val upcomingExpenses: StateFlow<List<ExpenseEntity>> = allExpenses.combine(_selectedDateMillis) { expenses, _ ->
        val todayStart = FormatUtils.normalizeToStartOfDay(System.currentTimeMillis())
        expenses
            .filter { it.dateMillis >= todayStart && it.status == ExpenseStatus.PENDING }
            .sortedBy { it.dateMillis }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Urgent alerts (overdue expenses or expenses in next 3 days)
    val urgentAlerts: StateFlow<List<ExpenseEntity>> = allExpenses.combine(_selectedDateMillis) { expenses, _ ->
        val todayStart = FormatUtils.normalizeToStartOfDay(System.currentTimeMillis())
        val threeDaysAhead = todayStart + (3L * 24 * 60 * 60 * 1000)
        expenses.filter {
            it.status == ExpenseStatus.PENDING && (it.dateMillis < todayStart || it.dateMillis in todayStart..threeDaysAhead)
        }.sortedBy { it.dateMillis }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category Spending Stats
    val categoryStats: StateFlow<List<CategoryStat>> = combine(
        currentMonthExpenses, allCategories
    ) { expenses, categories ->
        val categoryMap = categories.associateBy { it.id }
        val totalSpent = expenses.sumOf { it.actualAmount ?: it.plannedAmount }

        val grouped = expenses.groupBy { it.categoryId }
        grouped.mapNotNull { (catId, catExpenses) ->
            val cat = categoryMap[catId] ?: return@mapNotNull null
            val sum = catExpenses.sumOf { it.actualAmount ?: it.plannedAmount }
            val pct = if (totalSpent > 0) (sum / totalSpent).toFloat() else 0f
            CategoryStat(
                category = cat,
                totalAmount = sum,
                percentage = pct,
                count = catExpenses.size
            )
        }.sortedByDescending { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation & Date controls
    fun setSelectedDate(millis: Long) {
        _selectedDateMillis.value = millis
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        _selectedMonth.value = cal.get(Calendar.MONTH) + 1
        _selectedYear.value = cal.get(Calendar.YEAR)
    }

    fun setSelectedMonthYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        _selectedDateMillis.value = cal.timeInMillis
    }

    fun nextPeriod() {
        when (_timeViewMode.value) {
            TimeViewMode.DAY -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = _selectedDateMillis.value
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                setSelectedDate(cal.timeInMillis)
            }
            TimeViewMode.WEEK -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = _selectedDateMillis.value
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
                setSelectedDate(cal.timeInMillis)
            }
            TimeViewMode.MONTH -> {
                var m = _selectedMonth.value + 1
                var y = _selectedYear.value
                if (m > 12) {
                    m = 1
                    y += 1
                }
                setSelectedMonthYear(m, y)
            }
            TimeViewMode.ALL -> {}
        }
    }

    fun previousPeriod() {
        when (_timeViewMode.value) {
            TimeViewMode.DAY -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = _selectedDateMillis.value
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                setSelectedDate(cal.timeInMillis)
            }
            TimeViewMode.WEEK -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = _selectedDateMillis.value
                    add(Calendar.WEEK_OF_YEAR, -1)
                }
                setSelectedDate(cal.timeInMillis)
            }
            TimeViewMode.MONTH -> {
                var m = _selectedMonth.value - 1
                var y = _selectedYear.value
                if (m < 1) {
                    m = 12
                    y -= 1
                }
                setSelectedMonthYear(m, y)
            }
            TimeViewMode.ALL -> {}
        }
    }

    fun setTimeViewMode(mode: TimeViewMode) {
        _timeViewMode.value = mode
    }

    fun updateFilter(newFilter: ExpenseFilter) {
        _filter.value = newFilter
    }

    fun resetFilter() {
        _filter.value = ExpenseFilter()
    }

    fun setCurrency(currency: CurrencyOption) {
        _currency.value = currency
    }

    fun updateReminderSettings(settings: ReminderSettings) {
        _reminderSettings.value = settings
    }

    // Expense CRUD Actions
    fun addExpense(
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
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                name = name.trim(),
                plannedAmount = plannedAmount,
                actualAmount = actualAmount,
                categoryId = categoryId,
                dateMillis = dateMillis,
                status = status,
                paymentMethod = paymentMethod,
                notes = notes?.trim()?.ifEmpty { null },
                isRecurring = isRecurring,
                recurrenceFrequency = recurrenceFrequency,
                numberOfOccurrences = numberOfOccurrences
            )
        }
    }

    fun updateExpense(
        expense: ExpenseEntity,
        editOption: RecurrenceEditOption = RecurrenceEditOption.ONLY_THIS
    ) {
        viewModelScope.launch {
            repository.updateExpense(expense, editOption)
        }
    }

    fun deleteExpense(
        expense: ExpenseEntity,
        deleteOption: RecurrenceEditOption = RecurrenceEditOption.ONLY_THIS
    ) {
        viewModelScope.launch {
            repository.deleteExpense(expense, deleteOption)
        }
    }

    fun markAsPaid(expenseId: Long, actualAmount: Double?) {
        viewModelScope.launch {
            repository.markAsPaid(expenseId, actualAmount)
        }
    }

    fun markAsPending(expenseId: Long) {
        viewModelScope.launch {
            repository.markAsPending(expenseId)
        }
    }

    fun duplicateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.duplicateExpense(expense)
        }
    }

    // Category CRUD
    fun addCategory(name: String, iconKey: String, colorHex: Long) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    iconKey = iconKey,
                    colorHex = colorHex,
                    isDefault = false
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Budget Update
    fun updateBudget(month: Int, year: Int, expectedIncome: Double, totalBudget: Double) {
        viewModelScope.launch {
            repository.setBudget(month, year, expectedIncome, totalBudget)
        }
    }

    // Smart UX detection: Check if expense name suggests recurring subscription
    fun checkSmartRecurrenceSuggestion(name: String): RecurrenceFrequency? {
        val lower = name.lowercase().trim()
        val monthlyKeywords = listOf(
            "netflix", "spotify", "hbo", "disney", "youtube", "amazon prime",
            "arriendo", "alquiler", "internet", "fibra", "celular", "plan movil",
            "gimnasio", "gym", "seguro", "colegiatura", "administracion", "cuota"
        )
        return if (monthlyKeywords.any { lower.contains(it) }) {
            RecurrenceFrequency.MONTHLY
        } else null
    }
}
