package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryEntity
import com.example.data.ExpenseEntity
import com.example.model.ExpenseStatus
import com.example.model.PaymentMethodType
import com.example.model.RecurrenceEditOption
import com.example.model.RecurrenceFrequency
import com.example.ui.components.BudgetDialog
import com.example.ui.components.CategoryEditorDialog
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.ExpenseDialog
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.MarkAsPaidDialog
import com.example.ui.components.RecurrenceActionDialog
import com.example.ui.screens.CalendarTimelineScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen

enum class MainDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Inicio", Icons.Default.Home, Icons.Outlined.Home, "nav_dashboard"),
    CALENDAR("Calendario", Icons.Default.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_calendar"),
    EXPENSES("Gastos", Icons.Default.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_expenses"),
    STATS("Estadísticas", Icons.Default.Insights, Icons.Outlined.Insights, "nav_stats"),
    SETTINGS("Ajustes", Icons.Default.Settings, Icons.Outlined.Settings, "nav_settings")
}

@Composable
fun GastoPlanApp(
    viewModel: ExpenseViewModel
) {
    var currentDestination by remember { mutableStateOf(MainDestination.DASHBOARD) }

    // State collections
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val currentMonthExpenses by viewModel.currentMonthExpenses.collectAsState()
    val upcomingExpenses by viewModel.upcomingExpenses.collectAsState()
    val urgentAlerts by viewModel.urgentAlerts.collectAsState()
    val timelineGroups by viewModel.timelineGroups.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    val currentBudget by viewModel.currentMonthBudget.collectAsState()

    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val timeViewMode by viewModel.timeViewMode.collectAsState()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val reminderSettings by viewModel.reminderSettings.collectAsState()

    val committed7Days by viewModel.committedNext7Days.collectAsState()
    val committed15Days by viewModel.committedNext15Days.collectAsState()
    val committed30Days by viewModel.committedNext30Days.collectAsState()

    val totalMonthPlanned by viewModel.totalMonthPlanned.collectAsState()
    val totalMonthPaid by viewModel.totalMonthPaid.collectAsState()
    val totalMonthPending by viewModel.totalMonthPending.collectAsState()
    val totalMonthProjected by viewModel.totalMonthProjected.collectAsState()

    // Dialog & Modal State
    var showExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }

    var showMarkAsPaidDialog by remember { mutableStateOf(false) }
    var expenseToMarkPaid by remember { mutableStateOf<ExpenseEntity?>(null) }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    var showCategoryEditorDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }

    var showRecurrenceActionDialog by remember { mutableStateOf(false) }
    var recurrenceActionIsDelete by remember { mutableStateOf(false) }
    var pendingRecurringExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var pendingRecurringEditPayload by remember {
        mutableStateOf<Triple<ExpenseEntity, (ExpenseEntity, RecurrenceEditOption) -> Unit, String>?>(null)
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                MainDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    expenseToEdit = null
                    showExpenseDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("fab_add_expense")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar gasto",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                MainDestination.DASHBOARD -> {
                    DashboardScreen(
                        currentMonthExpenses = currentMonthExpenses,
                        upcomingExpenses = upcomingExpenses,
                        urgentAlerts = urgentAlerts,
                        categories = allCategories,
                        budget = currentBudget,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        currency = currency,
                        committed7Days = committed7Days,
                        committed15Days = committed15Days,
                        committed30Days = committed30Days,
                        totalPlanned = totalMonthPlanned,
                        totalPaid = totalMonthPaid,
                        totalPending = totalMonthPending,
                        totalProjected = totalMonthProjected,
                        onPreviousMonth = { viewModel.previousPeriod() },
                        onNextMonth = { viewModel.nextPeriod() },
                        onNavigateToExpenses = { currentDestination = MainDestination.EXPENSES },
                        onNavigateToCalendar = { currentDestination = MainDestination.CALENDAR },
                        onAddExpenseClick = {
                            expenseToEdit = null
                            showExpenseDialog = true
                        },
                        onEditBudgetClick = { showBudgetDialog = true },
                        onMarkAsPaidClick = { expense ->
                            expenseToMarkPaid = expense
                            showMarkAsPaidDialog = true
                        },
                        onMarkAsPendingClick = { expense ->
                            viewModel.markAsPending(expense.id)
                        },
                        onEditExpenseClick = { expense ->
                            expenseToEdit = expense
                            showExpenseDialog = true
                        },
                        onDuplicateExpenseClick = { expense ->
                            viewModel.duplicateExpense(expense)
                        },
                        onDeleteExpenseClick = { expense ->
                            if (expense.isRecurring) {
                                pendingRecurringExpense = expense
                                recurrenceActionIsDelete = true
                                showRecurrenceActionDialog = true
                            } else {
                                expenseToDelete = expense
                                showDeleteConfirmDialog = true
                            }
                        }
                    )
                }

                MainDestination.CALENDAR -> {
                    CalendarTimelineScreen(
                        allExpenses = allExpenses,
                        timelineGroups = timelineGroups,
                        categories = allCategories,
                        currency = currency,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        onPreviousMonth = { viewModel.previousPeriod() },
                        onNextMonth = { viewModel.nextPeriod() },
                        onAddExpenseClick = {
                            expenseToEdit = null
                            showExpenseDialog = true
                        },
                        onMarkAsPaidClick = { expense ->
                            expenseToMarkPaid = expense
                            showMarkAsPaidDialog = true
                        },
                        onMarkAsPendingClick = { expense ->
                            viewModel.markAsPending(expense.id)
                        },
                        onEditExpenseClick = { expense ->
                            expenseToEdit = expense
                            showExpenseDialog = true
                        },
                        onDuplicateExpenseClick = { expense ->
                            viewModel.duplicateExpense(expense)
                        },
                        onDeleteExpenseClick = { expense ->
                            if (expense.isRecurring) {
                                pendingRecurringExpense = expense
                                recurrenceActionIsDelete = true
                                showRecurrenceActionDialog = true
                            } else {
                                expenseToDelete = expense
                                showDeleteConfirmDialog = true
                            }
                        }
                    )
                }

                MainDestination.EXPENSES -> {
                    ExpensesScreen(
                        expenses = filteredExpenses,
                        categories = allCategories,
                        currency = currency,
                        timeViewMode = timeViewMode,
                        selectedDateMillis = selectedDateMillis,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        filter = filter,
                        onTimeViewModeChanged = { viewModel.setTimeViewMode(it) },
                        onPreviousPeriod = { viewModel.previousPeriod() },
                        onNextPeriod = { viewModel.nextPeriod() },
                        onSearchQueryChanged = { viewModel.updateFilter(filter.copy(searchQuery = it)) },
                        onOpenFilterClick = { showFilterSheet = true },
                        onAddExpenseClick = {
                            expenseToEdit = null
                            showExpenseDialog = true
                        },
                        onMarkAsPaidClick = { expense ->
                            expenseToMarkPaid = expense
                            showMarkAsPaidDialog = true
                        },
                        onMarkAsPendingClick = { expense ->
                            viewModel.markAsPending(expense.id)
                        },
                        onEditExpenseClick = { expense ->
                            expenseToEdit = expense
                            showExpenseDialog = true
                        },
                        onDuplicateExpenseClick = { expense ->
                            viewModel.duplicateExpense(expense)
                        },
                        onDeleteExpenseClick = { expense ->
                            if (expense.isRecurring) {
                                pendingRecurringExpense = expense
                                recurrenceActionIsDelete = true
                                showRecurrenceActionDialog = true
                            } else {
                                expenseToDelete = expense
                                showDeleteConfirmDialog = true
                            }
                        }
                    )
                }

                MainDestination.STATS -> {
                    StatsScreen(
                        currentMonthExpenses = currentMonthExpenses,
                        categoryStats = categoryStats,
                        currency = currency,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        onPreviousMonth = { viewModel.previousPeriod() },
                        onNextMonth = { viewModel.nextPeriod() }
                    )
                }

                MainDestination.SETTINGS -> {
                    SettingsScreen(
                        categories = allCategories,
                        currency = currency,
                        reminderSettings = reminderSettings,
                        onCurrencyChanged = { viewModel.setCurrency(it) },
                        onReminderSettingsChanged = { viewModel.updateReminderSettings(it) },
                        onEditBudgetClick = { showBudgetDialog = true },
                        onAddCategoryClick = {
                            categoryToEdit = null
                            showCategoryEditorDialog = true
                        },
                        onEditCategoryClick = { cat ->
                            categoryToEdit = cat
                            showCategoryEditorDialog = true
                        },
                        onDeleteCategoryClick = { cat ->
                            viewModel.deleteCategory(cat)
                        }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & BOTTOM SHEETS
    // ==========================================

    // Expense Add / Edit Dialog
    if (showExpenseDialog) {
        ExpenseDialog(
            initialExpense = expenseToEdit,
            categories = allCategories,
            currency = currency,
            onDismiss = {
                showExpenseDialog = false
                expenseToEdit = null
            },
            onSave = { name, plannedAmount, actualAmount, categoryId, dateMillis, status, paymentMethod, notes, isRecurring, recurrenceFrequency ->
                if (expenseToEdit != null) {
                    val updated = expenseToEdit!!.copy(
                        name = name,
                        plannedAmount = plannedAmount,
                        actualAmount = actualAmount,
                        categoryId = categoryId,
                        dateMillis = dateMillis,
                        status = status,
                        paymentMethod = paymentMethod,
                        notes = notes,
                        isRecurring = isRecurring,
                        recurrenceFrequency = recurrenceFrequency
                    )
                    if (updated.isRecurring) {
                        pendingRecurringExpense = updated
                        recurrenceActionIsDelete = false
                        showRecurrenceActionDialog = true
                    } else {
                        viewModel.updateExpense(updated, RecurrenceEditOption.ONLY_THIS)
                    }
                } else {
                    viewModel.addExpense(
                        name = name,
                        plannedAmount = plannedAmount,
                        actualAmount = actualAmount,
                        categoryId = categoryId,
                        dateMillis = dateMillis,
                        status = status,
                        paymentMethod = paymentMethod,
                        notes = notes,
                        isRecurring = isRecurring,
                        recurrenceFrequency = recurrenceFrequency
                    )
                }
                showExpenseDialog = false
                expenseToEdit = null
            }
        )
    }

    // Mark As Paid Dialog
    if (showMarkAsPaidDialog && expenseToMarkPaid != null) {
        MarkAsPaidDialog(
            expense = expenseToMarkPaid!!,
            currency = currency,
            onDismiss = {
                showMarkAsPaidDialog = false
                expenseToMarkPaid = null
            },
            onConfirm = { actualAmount ->
                viewModel.markAsPaid(expenseToMarkPaid!!.id, actualAmount)
                showMarkAsPaidDialog = false
                expenseToMarkPaid = null
            }
        )
    }

    // Recurring Action Dialog (Only this / This & Future / All series)
    if (showRecurrenceActionDialog && pendingRecurringExpense != null) {
        RecurrenceActionDialog(
            isDelete = recurrenceActionIsDelete,
            onDismiss = {
                showRecurrenceActionDialog = false
                pendingRecurringExpense = null
            },
            onConfirm = { option ->
                if (recurrenceActionIsDelete) {
                    viewModel.deleteExpense(pendingRecurringExpense!!, option)
                } else {
                    viewModel.updateExpense(pendingRecurringExpense!!, option)
                }
                showRecurrenceActionDialog = false
                pendingRecurringExpense = null
            }
        )
    }

    // Delete Confirm Dialog (Single Non-recurring item)
    if (showDeleteConfirmDialog && expenseToDelete != null) {
        DeleteConfirmationDialog(
            itemName = expenseToDelete!!.name,
            onDismiss = {
                showDeleteConfirmDialog = false
                expenseToDelete = null
            },
            onConfirm = {
                viewModel.deleteExpense(expenseToDelete!!, RecurrenceEditOption.ONLY_THIS)
                showDeleteConfirmDialog = false
                expenseToDelete = null
            }
        )
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = filter,
            categories = allCategories,
            onDismiss = { showFilterSheet = false },
            onApplyFilter = { newFilter -> viewModel.updateFilter(newFilter) },
            onResetFilter = { viewModel.resetFilter() }
        )
    }

    // Budget Dialog
    if (showBudgetDialog) {
        BudgetDialog(
            budget = currentBudget,
            month = selectedMonth,
            year = selectedYear,
            currency = currency,
            onDismiss = { showBudgetDialog = false },
            onSave = { expectedIncome, totalBudget ->
                viewModel.updateBudget(selectedMonth, selectedYear, expectedIncome, totalBudget)
                showBudgetDialog = false
            }
        )
    }

    // Category Editor Dialog
    if (showCategoryEditorDialog) {
        CategoryEditorDialog(
            initialCategory = categoryToEdit,
            onDismiss = {
                showCategoryEditorDialog = false
                categoryToEdit = null
            },
            onSave = { name, iconKey, colorHex ->
                if (categoryToEdit != null) {
                    viewModel.updateCategory(
                        categoryToEdit!!.copy(
                            name = name,
                            iconKey = iconKey,
                            colorHex = colorHex
                        )
                    )
                } else {
                    viewModel.addCategory(name, iconKey, colorHex)
                }
                showCategoryEditorDialog = false
                categoryToEdit = null
            }
        )
    }
}
