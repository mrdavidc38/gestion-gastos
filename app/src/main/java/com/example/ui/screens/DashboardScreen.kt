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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BudgetEntity
import com.example.data.CategoryEntity
import com.example.data.ExpenseEntity
import com.example.model.CurrencyOption
import com.example.model.ExpenseStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExpenseCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.util.FormatUtils
import java.util.Calendar

@Composable
fun DashboardScreen(
    currentMonthExpenses: List<ExpenseEntity>,
    upcomingExpenses: List<ExpenseEntity>,
    urgentAlerts: List<ExpenseEntity>,
    categories: List<CategoryEntity>,
    budget: BudgetEntity?,
    selectedMonth: Int,
    selectedYear: Int,
    currency: CurrencyOption,
    committed7Days: Double,
    committed15Days: Double,
    committed30Days: Double,
    totalPlanned: Double,
    totalPaid: Double,
    totalPending: Double,
    totalProjected: Double,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onEditBudgetClick: () -> Unit,
    onMarkAsPaidClick: (ExpenseEntity) -> Unit,
    onMarkAsPendingClick: (ExpenseEntity) -> Unit,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onDuplicateExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteExpenseClick: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    var committedDaysTab by remember { mutableIntStateOf(7) }
    val currentCommitted = when (committedDaysTab) {
        7 -> committed7Days
        15 -> committed15Days
        else -> committed30Days
    }

    val totalBudgetLimit = budget?.totalBudget ?: 4000000.0
    val expectedIncome = budget?.expectedIncome ?: 6000000.0
    val budgetProgress = if (totalBudgetLimit > 0) (totalProjected / totalBudgetLimit).toFloat() else 0f
    val budgetPercentInt = (budgetProgress * 100).toInt()

    val availableAfterExpenses = (expectedIncome - totalProjected).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Greeting & Month Selector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "¡Buenos días! 👋",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Planifica y controla tu dinero",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Month Navigation Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = onPreviousMonth,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior", modifier = Modifier.size(18.dp))
                        }

                        Text(
                            text = FormatUtils.formatMonthYear(selectedMonth, selectedYear),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = onNextMonth,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Urgent Alerts Card (if any pending payments today or overdue)
        if (urgentAlerts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF3B1812) else Color(0xFFFFF1EE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StatusOverdueRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = StatusOverdueRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Próximos pagos urgentes (${urgentAlerts.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)
                            )
                            val firstAlert = urgentAlerts.first()
                            Text(
                                text = "${firstAlert.name} • ${FormatUtils.formatCurrency(firstAlert.plannedAmount, currency)} (${FormatUtils.formatFriendlyDate(firstAlert.dateMillis)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFFFFDAD6) else Color(0xFF410002)
                            )
                        }

                        TextButton(onClick = onNavigateToExpenses) {
                            Text("Revisar", fontWeight = FontWeight.Bold, color = StatusOverdueRed)
                        }
                    }
                }
            }
        }

        // Hero Monthly Budget Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onEditBudgetClick() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Gastos del mes",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FormatUtils.formatCurrency(totalProjected, currency),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Percentage Pill
                        val badgeColor = if (budgetProgress > 1f) StatusOverdueRed else if (budgetProgress >= 0.85f) StatusPendingAmber else EmeraldPrimary
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = badgeColor.copy(alpha = if (isDark) 0.25f else 0.15f)
                        ) {
                            Text(
                                text = "$budgetPercentInt%",
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "de ${FormatUtils.formatCurrency(totalBudgetLimit, currency)} presupuestados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    val progressColor = if (budgetProgress > 1f) StatusOverdueRed else if (budgetProgress >= 0.85f) StatusPendingAmber else EmeraldPrimary
                    LinearProgressIndicator(
                        progress = { budgetProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Warning condition text
                    if (budgetProgress > 1f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusOverdueRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Has superado tu presupuesto mensual.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusOverdueRed
                            )
                        }
                    } else if (budgetProgress >= 0.85f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusPendingAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Has utilizado el $budgetPercentInt% de tu presupuesto.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = StatusPendingAmber
                            )
                        }
                    }
                }
            }
        }

        // Dinero Comprometido Feature Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF132338) else Color(0xFFEFF6FF)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dinero comprometido",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Selector for 7, 15, 30 days
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .padding(2.dp)
                        ) {
                            listOf(7 to "7d", 15 to "15d", 30 to "30d").forEach { (days, label) ->
                                val isSelected = committedDaysTab == days
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .clickable { committedDaysTab = days }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total comprometido (${committedDaysTab} días)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FormatUtils.formatCurrency(currentCommitted, currency),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = StatusPendingAmber
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Disponible estimado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FormatUtils.formatCurrency(availableAfterExpenses, currency),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }

        // Summary Breakdown Strip (Pagado / Pendiente / Proyectado)
        item {
            Text(
                text = "Resumen",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pagado
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusPaidGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pagado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtils.formatCurrency(totalPaid, currency),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusPaidGreen
                        )
                    }
                }

                // Pendiente
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusPendingAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pendiente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtils.formatCurrency(totalPending, currency),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusPendingAmber
                        )
                    }
                }

                // Proyectado
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Proyectado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtils.formatCurrency(totalProjected, currency),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Section: Próximos gastos Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Próximos gastos",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = onNavigateToExpenses,
                    modifier = Modifier.testTag("view_all_upcoming_btn")
                ) {
                    Text("Ver todos", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Upcoming Expenses Items (Take first 4)
        val previewUpcoming = upcomingExpenses.take(4)
        if (previewUpcoming.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No tienes gastos próximos pendientes 🎉",
                    description = "Todos tus gastos planificados están al día.",
                    actionLabel = "Planificar un gasto",
                    onActionClick = onAddExpenseClick
                )
            }
        } else {
            items(previewUpcoming, key = { it.id }) { expense ->
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
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom nav and FAB
        }
    }
}
