package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.model.CurrencyOption
import com.example.model.ExpenseStatus
import com.example.model.PaymentMethodType
import com.example.ui.CategoryStat
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.util.FormatUtils
import com.example.util.IconUtils

@Composable
fun StatsScreen(
    currentMonthExpenses: List<ExpenseEntity>,
    categoryStats: List<CategoryStat>,
    currency: CurrencyOption,
    selectedMonth: Int,
    selectedYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val totalPlanned = currentMonthExpenses.sumOf { it.plannedAmount }
    val paidExpenses = currentMonthExpenses.filter { it.status == ExpenseStatus.PAID }
    val totalPaid = paidExpenses.sumOf { it.actualAmount ?: it.plannedAmount }
    val diff = totalPlanned - totalPaid

    val topCategory = categoryStats.firstOrNull()

    // Payment Methods Breakdown
    val paymentStats = remember(currentMonthExpenses) {
        val total = currentMonthExpenses.sumOf { it.actualAmount ?: it.plannedAmount }
        PaymentMethodType.values().mapNotNull { method ->
            val expensesForMethod = currentMonthExpenses.filter { it.paymentMethod == method }
            if (expensesForMethod.isEmpty()) null
            else {
                val sum = expensesForMethod.sumOf { it.actualAmount ?: it.plannedAmount }
                val pct = if (total > 0) (sum / total).toFloat() else 0f
                Triple(method, sum, pct)
            }
        }.sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
                }

                Text(
                    text = "Estadísticas • ${FormatUtils.formatMonthYear(selectedMonth, selectedYear)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
                }
            }
        }

        // Planificado vs Real Comparison Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Planificado vs Real",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (paidExpenses.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (diff >= 0) StatusPaidGreen.copy(alpha = 0.15f) else StatusOverdueRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (diff >= 0) "Ahorro: ${FormatUtils.formatCurrency(diff, currency)}"
                                    else "Exceso: ${FormatUtils.formatCurrency(-diff, currency)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (diff >= 0) StatusPaidGreen else StatusOverdueRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
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
                                text = "Planeaste gastar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FormatUtils.formatCurrency(totalPlanned, currency),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Realmente pagado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FormatUtils.formatCurrency(totalPaid, currency),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = StatusPaidGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (paidExpenses.isEmpty()) "Aún no has registrado pagos para este mes."
                        else "Planeaste gastar ${FormatUtils.formatCurrency(totalPlanned, currency)} y has pagado efectivamente ${FormatUtils.formatCurrency(totalPaid, currency)}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Smart Financial Insight Banner
        if (topCategory != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E2638) else Color(0xFFEFF6FF)
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
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Insight Financiero",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tu categoría con mayor gasto este mes es ${topCategory.category.name} (${(topCategory.percentage * 100).toInt()}%), representando ${FormatUtils.formatCurrency(topCategory.totalAmount, currency)}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Gastos por Categoría
        item {
            Text(
                text = "Gastos por categoría",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (categoryStats.isEmpty()) {
            item {
                Text(
                    text = "No hay registros en este mes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(categoryStats, key = { it.category.id }) { stat ->
                val catColor = Color(stat.category.colorHex)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(catColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconUtils.getCategoryIcon(stat.category.iconKey),
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = stat.category.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${stat.count} gasto(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatUtils.formatCurrency(stat.totalAmount, currency),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(stat.percentage * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { stat.percentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = catColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Métodos de Pago Breakdown
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Gastos por método de pago",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (paymentStats.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        paymentStats.forEach { (method, sum, pct) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = IconUtils.getPaymentMethodIcon(method.iconKey),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = method.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = FormatUtils.formatCurrency(sum, currency),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${(pct * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
