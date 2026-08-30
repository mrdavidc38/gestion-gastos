package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryEntity
import com.example.data.ExpenseEntity
import com.example.model.CurrencyOption
import com.example.model.ExpenseStatus
import com.example.ui.ExpenseFilter
import com.example.ui.TimeViewMode
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExpenseCard
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.util.FormatUtils

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    categories: List<CategoryEntity>,
    currency: CurrencyOption,
    timeViewMode: TimeViewMode,
    selectedDateMillis: Long,
    selectedMonth: Int,
    selectedYear: Int,
    filter: ExpenseFilter,
    onTimeViewModeChanged: (TimeViewMode) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onOpenFilterClick: () -> Unit,
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

    val activeFilterCount = (if (filter.categoryId != null) 1 else 0) +
            (if (filter.status != null) 1 else 0) +
            (if (filter.paymentMethod != null) 1 else 0) +
            (if (filter.onlyRecurring) 1 else 0)

    val totalPeriodPlanned = expenses.sumOf { it.plannedAmount }
    val totalPeriodPaid = expenses.filter { it.status == ExpenseStatus.PAID }.sumOf { it.actualAmount ?: it.plannedAmount }
    val totalPeriodPending = expenses.filter { it.status != ExpenseStatus.PAID }.sumOf { it.plannedAmount }

    val periodLabel = when (timeViewMode) {
        TimeViewMode.DAY -> FormatUtils.formatFriendlyDate(selectedDateMillis)
        TimeViewMode.WEEK -> "Semana de ${FormatUtils.formatShortDate(FormatUtils.getStartOfWeek(selectedDateMillis))}"
        TimeViewMode.MONTH -> FormatUtils.formatMonthYear(selectedMonth, selectedYear)
        TimeViewMode.ALL -> "Todos los registros"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Time View Mode Selector Tabs: [ Día ] [ Semana ] [ Mes ] [ Todos ]
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimeViewMode.values().forEach { mode ->
                    val isSelected = timeViewMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surface
                                else Color.Transparent
                            )
                            .clickable { onTimeViewModeChanged(mode) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Period Navigation (◀ Label ▶) - Hidden in ALL mode
        if (timeViewMode != TimeViewMode.ALL) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousPeriod) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Periodo anterior")
                }

                Text(
                    text = periodLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onNextPeriod) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Periodo siguiente")
                }
            }
        }

        // Search Bar + Filter Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = filter.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Buscar gastos...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (filter.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Borrar búsqueda", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("expenses_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (activeFilterCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onOpenFilterClick() }
                    .testTag("open_filters_btn")
            ) {
                Box(
                    modifier = Modifier.padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (activeFilterCount > 0) {
                                Badge { Text("$activeFilterCount") }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = "Filtros",
                            tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Summary Ribbon (Total, Pagado, Pendiente)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total planificado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = FormatUtils.formatCurrency(totalPeriodPlanned, currency),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pagado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = FormatUtils.formatCurrency(totalPeriodPaid, currency),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = StatusPaidGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Pendiente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = FormatUtils.formatCurrency(totalPeriodPending, currency),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = StatusPendingAmber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expense List
        if (expenses.isEmpty()) {
            EmptyStateView(
                title = if (filter.searchQuery.isNotBlank() || activeFilterCount > 0) "No se encontraron gastos con estos filtros"
                else "No hay gastos para este periodo",
                description = "Prueba modificando tus filtros o planifica un nuevo gasto.",
                actionLabel = "Crear gasto",
                onActionClick = onAddExpenseClick
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(expenses, key = { it.id }) { expense ->
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

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
