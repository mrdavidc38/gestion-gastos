package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ExpenseEntity
import com.example.model.CurrencyOption
import com.example.ui.theme.StatusPaidGreen
import com.example.util.FormatUtils

@Composable
fun MarkAsPaidDialog(
    expense: ExpenseEntity,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirm: (actualAmount: Double) -> Unit
) {
    val initialAmountFormatted = if (expense.plannedAmount % 1.0 == 0.0) {
        expense.plannedAmount.toLong().toString()
    } else {
        expense.plannedAmount.toString()
    }

    var isDifferentAmount by remember { mutableStateOf(false) }
    var actualAmountText by remember { mutableStateOf(initialAmountFormatted) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("mark_as_paid_dialog")
                .clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusPaidGreen,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(
                        text = "Marcar como pagado",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Valor planificado: ${FormatUtils.formatCurrency(expense.plannedAmount, currency)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isDifferentAmount,
                        onCheckedChange = { isDifferentAmount = it }
                    )
                    Text(
                        text = "El valor pagado fue diferente al planificado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isDifferentAmount) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = actualAmountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) {
                                actualAmountText = input
                            }
                        },
                        label = { Text("Valor real pagado (${currency.code})") },
                        leadingIcon = {
                            Text(
                                text = currency.symbol,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val finalAmount = if (isDifferentAmount) {
                                actualAmountText.toDoubleOrNull() ?: expense.plannedAmount
                            } else {
                                expense.plannedAmount
                            }
                            onConfirm(finalAmount)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("confirm_paid_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPaidGreen)
                    ) {
                        Text("Confirmar pago", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
