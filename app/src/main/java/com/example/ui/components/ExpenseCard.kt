package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryEntity
import com.example.data.ExpenseEntity
import com.example.model.CurrencyOption
import com.example.model.ExpenseStatus
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusPaidGreen
import com.example.util.FormatUtils
import com.example.util.IconUtils

@Composable
fun ExpenseCard(
    expense: ExpenseEntity,
    category: CategoryEntity?,
    currency: CurrencyOption,
    onMarkAsPaidClick: () -> Unit,
    onMarkAsPendingClick: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isPaid = expense.status == ExpenseStatus.PAID
    val isOverdue = !isPaid && expense.dateMillis < FormatUtils.normalizeToStartOfDay(System.currentTimeMillis())
    val effectiveStatus = if (isPaid) ExpenseStatus.PAID else if (isOverdue) ExpenseStatus.OVERDUE else ExpenseStatus.PENDING

    val catColor = category?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary
    val catIcon = category?.let { IconUtils.getCategoryIcon(it.iconKey) } ?: IconUtils.getCategoryIcon("category")
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEditClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 1.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon with colored circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(catColor.copy(alpha = if (isDark) 0.22f else 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = category?.name ?: "Categoría",
                        tint = catColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Metadata
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expense.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                textDecoration = if (isPaid) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (isPaid) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (expense.isRecurring) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = "Recurrente",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = expense.recurrenceFrequency.displayName,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = FormatUtils.formatFriendlyDate(expense.dateMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                        )

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        category?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Text(
                            text = expense.paymentMethod.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Amount Column & Status Action
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val displayAmount = if (isPaid && expense.actualAmount != null) expense.actualAmount else expense.plannedAmount
                    Text(
                        text = FormatUtils.formatCurrency(displayAmount, currency),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = if (isPaid) StatusPaidGreen else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    StatusBadge(
                        status = effectiveStatus,
                        isDark = isDark
                    )
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("expense_menu_btn_${expense.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (isPaid) {
                            DropdownMenuItem(
                                text = { Text("Marcar como pendiente") },
                                leadingIcon = { Icon(Icons.Default.Undo, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onMarkAsPendingClick()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Marcar como pagado") },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusPaidGreen) },
                                onClick = {
                                    showMenu = false
                                    onMarkAsPaidClick()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Editar") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Duplicar") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onDuplicateClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            // Planned vs Actual amount indicator if paid and different
            if (isPaid && expense.actualAmount != null && expense.actualAmount != expense.plannedAmount) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Planificado: ${FormatUtils.formatCurrency(expense.plannedAmount, currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val diff = expense.plannedAmount - expense.actualAmount
                        Text(
                            text = if (diff > 0) "Ahorro: ${FormatUtils.formatCurrency(diff, currency)}"
                            else "Excedente: ${FormatUtils.formatCurrency(-diff, currency)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (diff > 0) StatusPaidGreen else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Notes if any
            if (!expense.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = expense.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
