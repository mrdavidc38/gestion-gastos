package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryEntity
import com.example.data.ExpenseEntity
import com.example.model.CurrencyOption
import com.example.model.ExpenseStatus
import com.example.ui.TimelineDayGroup
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExpenseCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.util.FormatUtils
import com.example.util.IconUtils
import java.util.Calendar

enum class CalendarViewTab {
    TIMELINE,
    CALENDAR
}

@Composable
fun CalendarTimelineScreen(
    allExpenses: List<ExpenseEntity>,
    timelineGroups: List<TimelineDayGroup>,
    categories: List<CategoryEntity>,
    currency: CurrencyOption,
    selectedMonth: Int,
    selectedYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onMarkAsPaidClick: (ExpenseEntity) -> Unit,
    onMarkAsPendingClick: (ExpenseEntity) -> Unit,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onDuplicateExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteExpenseClick: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    var currentTab by remember { mutableStateOf(CalendarViewTab.TIMELINE) }

    // Selected Day in Calendar view (defaults to today)
    var selectedCalendarDateMillis by remember {
        mutableLongStateOf(FormatUtils.normalizeToStartOfDay(System.currentTimeMillis()))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // View Tab Toggle: [ Línea de Tiempo ] [ Calendario ]
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                // Tab 1: Línea de tiempo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (currentTab == CalendarViewTab.TIMELINE) MaterialTheme.colorScheme.surface
                            else Color.Transparent
                        )
                        .clickable { currentTab = CalendarViewTab.TIMELINE }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = if (currentTab == CalendarViewTab.TIMELINE) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Línea de tiempo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (currentTab == CalendarViewTab.TIMELINE) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tab 2: Calendario
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (currentTab == CalendarViewTab.CALENDAR) MaterialTheme.colorScheme.surface
                            else Color.Transparent
                        )
                        .clickable { currentTab = CalendarViewTab.CALENDAR }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = if (currentTab == CalendarViewTab.CALENDAR) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Calendario",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (currentTab == CalendarViewTab.CALENDAR) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentTab == CalendarViewTab.TIMELINE) {
            // ==========================================
            // TIMELINE VIEW
            // ==========================================
            if (timelineGroups.isEmpty()) {
                EmptyStateView(
                    title = "Línea de tiempo vacía",
                    description = "Comienza a programar gastos para visualizar tu flujo de pagos cronológico.",
                    actionLabel = "Planificar gasto",
                    onActionClick = onAddExpenseClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF162332) else Color(0xFFEFF6FF)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Flujo Cronológico Futuro",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Conoce con exactitud cuánto dinero necesitarás en cada fecha.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(timelineGroups, key = { it.dateMillis }) { group ->
                        TimelineDayItem(
                            group = group,
                            categoryMap = categoryMap,
                            currency = currency,
                            onMarkAsPaidClick = onMarkAsPaidClick,
                            onMarkAsPendingClick = onMarkAsPendingClick,
                            onEditExpenseClick = onEditExpenseClick,
                            onDuplicateExpenseClick = onDuplicateExpenseClick,
                            onDeleteExpenseClick = onDeleteExpenseClick
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        } else {
            // ==========================================
            // CALENDAR MONTH VIEW
            // ==========================================
            CalendarMonthView(
                allExpenses = allExpenses,
                categories = categories,
                currency = currency,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                selectedDateMillis = selectedCalendarDateMillis,
                onDateSelected = { selectedCalendarDateMillis = it },
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onAddExpenseClick = onAddExpenseClick,
                onMarkAsPaidClick = onMarkAsPaidClick,
                onMarkAsPendingClick = onMarkAsPendingClick,
                onEditExpenseClick = onEditExpenseClick,
                onDuplicateExpenseClick = onDuplicateExpenseClick,
                onDeleteExpenseClick = onDeleteExpenseClick
            )
        }
    }
}

@Composable
private fun TimelineDayItem(
    group: TimelineDayGroup,
    categoryMap: Map<Long, CategoryEntity>,
    currency: CurrencyOption,
    onMarkAsPaidClick: (ExpenseEntity) -> Unit,
    onMarkAsPendingClick: (ExpenseEntity) -> Unit,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onDuplicateExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteExpenseClick: (ExpenseEntity) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isToday = FormatUtils.normalizeToStartOfDay(System.currentTimeMillis()) == group.dateMillis

    Row(modifier = Modifier.fillMaxWidth()) {
        // Vertical Timeline Axis (Circle Node + Line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isToday) EmeraldPrimary else MaterialTheme.colorScheme.outline)
                    .then(
                        if (isToday) Modifier.border(3.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        else Modifier
                    )
            )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(if (group.expenses.size > 1) 120.dp else 80.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Content Area
        Column(modifier = Modifier.weight(1f)) {
            // Day Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = group.friendlyLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (isToday) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "HOY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Total día: ${FormatUtils.formatCurrency(group.totalPlanned, currency)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Expenses in this day
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                group.expenses.forEach { expense ->
                    ExpenseCard(
                        expense = expense,
                        category = categoryMap[expense.categoryId],
                        currency = currency,
                        onMarkAsPaidClick = { onMarkAsPaidClick(expense) },
                        onMarkAsPendingClick = { onMarkAsPendingClick(expense) },
                        onEditClick = { onEditExpenseClick(expense) },
                        onDuplicateClick = { onDuplicateExpenseClick(expense) },
                        onDeleteClick = { onDeleteExpenseClick(expense) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthView(
    allExpenses: List<ExpenseEntity>,
    categories: List<CategoryEntity>,
    currency: CurrencyOption,
    selectedMonth: Int,
    selectedYear: Int,
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onMarkAsPaidClick: (ExpenseEntity) -> Unit,
    onMarkAsPendingClick: (ExpenseEntity) -> Unit,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onDuplicateExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteExpenseClick: (ExpenseEntity) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    // Calculate calendar grid days
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, selectedYear)
        set(Calendar.MONTH, selectedMonth - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        firstDayOfWeek = Calendar.MONDAY
    }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeekIndex = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

    // Expenses mapped by start of day
    val expensesByDay = remember(allExpenses, selectedMonth, selectedYear) {
        allExpenses.groupBy { FormatUtils.normalizeToStartOfDay(it.dateMillis) }
    }

    // Selected day expenses
    val selectedDayExpenses = expensesByDay[selectedDateMillis] ?: emptyList()
    val totalSelectedDayAmount = selectedDayExpenses.sumOf { it.actualAmount ?: it.plannedAmount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Navigation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
                }

                Text(
                    text = FormatUtils.formatMonthYear(selectedMonth, selectedYear),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
                }
            }
        }

        // Calendar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Weekday headers (Lun, Mar, Mié, Jue, Vie, Sáb, Dom)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach { dayName ->
                            Text(
                                text = dayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Grid of 6 rows x 7 days
                    val totalCells = ((firstDayOfWeekIndex + daysInMonth + 6) / 7) * 7
                    for (row in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = (row * 7) + col
                                val dayNumber = cellIndex - firstDayOfWeekIndex + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val cellCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, selectedYear)
                                        set(Calendar.MONTH, selectedMonth - 1)
                                        set(Calendar.DAY_OF_MONTH, dayNumber)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    val cellMillis = cellCal.timeInMillis
                                    val isSelected = cellMillis == selectedDateMillis
                                    val dayExpenses = expensesByDay[cellMillis] ?: emptyList()
                                    val hasExpenses = dayExpenses.isNotEmpty()
                                    val hasPending = dayExpenses.any { it.status != ExpenseStatus.PAID }
                                    val hasPaid = dayExpenses.any { it.status == ExpenseStatus.PAID }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else if (hasExpenses) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                else Color.Transparent
                                            )
                                            .clickable { onDateSelected(cellMillis) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNumber",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || hasExpenses) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White
                                                else MaterialTheme.colorScheme.onSurface
                                            )

                                            // Expense Dot Indicators
                                            if (hasExpenses) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (hasPaid) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color.White else StatusPaidGreen)
                                                        )
                                                    }
                                                    if (hasPending) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color.White else StatusPendingAmber)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Details Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = FormatUtils.formatFriendlyDate(selectedDateMillis),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${selectedDayExpenses.size} gasto(s) para este día",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedDayExpenses.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Total: ${FormatUtils.formatCurrency(totalSelectedDayAmount, currency)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Selected Day Expense Items
        if (selectedDayExpenses.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No hay gastos en esta fecha",
                    description = "Toca '+' para planificar o registrar un gasto en este día.",
                    actionLabel = "Agregar a este día",
                    onActionClick = onAddExpenseClick
                )
            }
        } else {
            items(selectedDayExpenses, key = { it.id }) { expense ->
                ExpenseCard(
                    expense = expense,
                    category = categoryMap[expense.categoryId],
                    currency = currency,
                    onMarkAsPaidClick = { onMarkAsPaidClick(expense) },
                    onMarkAsPendingClick = { onMarkAsPendingClick(expense) },
                    onEditClick = { onEditExpenseClick(expense) },
                    onDuplicateClick = { onDuplicateExpenseClick(expense) },
                    onDeleteClick = { onDeleteExpenseClick(expense) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
