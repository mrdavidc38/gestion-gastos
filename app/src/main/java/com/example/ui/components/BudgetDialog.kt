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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.data.BudgetEntity
import com.example.model.CurrencyOption
import com.example.util.FormatUtils

@Composable
fun BudgetDialog(
    budget: BudgetEntity?,
    month: Int,
    year: Int,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onSave: (expectedIncome: Double, totalBudget: Double) -> Unit
) {
    var incomeText by remember {
        mutableStateOf(
            if (budget != null) {
                if (budget.expectedIncome % 1.0 == 0.0) budget.expectedIncome.toLong().toString() else budget.expectedIncome.toString()
            } else "6000000"
        )
    }

    var budgetText by remember {
        mutableStateOf(
            if (budget != null) {
                if (budget.totalBudget % 1.0 == 0.0) budget.totalBudget.toLong().toString() else budget.totalBudget.toString()
            } else "4000000"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("budget_dialog")
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
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(
                        text = "Presupuesto de ${FormatUtils.formatMonthYear(month, year)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = incomeText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) incomeText = input
                    },
                    label = { Text("Ingresos esperados (${currency.code})") },
                    placeholder = { Text("6000000") },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) budgetText = input
                    },
                    label = { Text("Presupuesto límite de gastos (${currency.code})") },
                    placeholder = { Text("4000000") },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input"),
                    shape = RoundedCornerShape(12.dp)
                )

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
                            val income = incomeText.toDoubleOrNull() ?: 0.0
                            val budgetVal = budgetText.toDoubleOrNull() ?: 0.0
                            if (budgetVal > 0) {
                                onSave(income, budgetVal)
                            }
                        },
                        enabled = (budgetText.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("save_budget_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Guardar presupuesto", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
