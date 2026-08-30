package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CategoryEntity
import com.example.data.ExpenseEntity
import com.example.model.CurrencyOption
import com.example.model.ExpenseStatus
import com.example.model.PaymentMethodType
import com.example.model.RecurrenceFrequency
import com.example.util.FormatUtils
import com.example.util.IconUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseDialog(
    initialExpense: ExpenseEntity? = null,
    categories: List<CategoryEntity>,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        plannedAmount: Double,
        actualAmount: Double?,
        categoryId: Long,
        dateMillis: Long,
        status: ExpenseStatus,
        paymentMethod: PaymentMethodType,
        notes: String?,
        isRecurring: Boolean,
        recurrenceFrequency: RecurrenceFrequency
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditing = initialExpense != null

    var name by remember { mutableStateOf(initialExpense?.name ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (initialExpense != null) {
                if (initialExpense.plannedAmount % 1.0 == 0.0) {
                    initialExpense.plannedAmount.toLong().toString()
                } else {
                    initialExpense.plannedAmount.toString()
                }
            } else ""
        )
    }

    var selectedCategoryId by remember {
        mutableLongStateOf(
            initialExpense?.categoryId ?: (categories.firstOrNull()?.id ?: 1L)
        )
    }

    var selectedDateMillis by remember {
        mutableLongStateOf(initialExpense?.dateMillis ?: System.currentTimeMillis())
    }

    var selectedStatus by remember {
        mutableStateOf(initialExpense?.status ?: ExpenseStatus.PENDING)
    }

    var selectedPaymentMethod by remember {
        mutableStateOf(initialExpense?.paymentMethod ?: PaymentMethodType.DEBIT_CARD)
    }

    var selectedFrequency by remember {
        mutableStateOf(initialExpense?.recurrenceFrequency ?: RecurrenceFrequency.NONE)
    }

    var isRecurring by remember {
        mutableStateOf(initialExpense?.isRecurring ?: false)
    }

    var notes by remember { mutableStateOf(initialExpense?.notes ?: "") }
    var smartSuggestionDismissed by remember { mutableStateOf(false) }

    // Smart UX detection
    val smartSuggestedFrequency = remember(name) {
        val lower = name.lowercase().trim()
        val keywords = listOf(
            "netflix", "spotify", "hbo", "disney", "youtube", "amazon prime",
            "arriendo", "alquiler", "internet", "fibra", "celular", "plan movil",
            "gimnasio", "gym", "seguro", "colegiatura", "administracion", "cuota"
        )
        if (!isRecurring && !smartSuggestionDismissed && keywords.any { lower.contains(it) }) {
            RecurrenceFrequency.MONTHLY
        } else null
    }

    // Dropdown states
    var categoryExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var paymentMethodExpanded by remember { mutableStateOf(false) }

    val selectedCategory = categories.find { it.id == selectedCategoryId }

    // Date Picker Dialog
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            selectedDateMillis = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("expense_dialog")
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Editar gasto" else "Nuevo gasto",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Status Pill Toggle (Pendiente / Pagado)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (selectedStatus == ExpenseStatus.PENDING) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { selectedStatus = ExpenseStatus.PENDING }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Pendiente",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedStatus == ExpenseStatus.PENDING) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (selectedStatus == ExpenseStatus.PAID) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { selectedStatus = ExpenseStatus.PAID }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Pagado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedStatus == ExpenseStatus.PAID) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del gasto") },
                    placeholder = { Text("Ej. Arriendo, Mercado, Netflix...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Smart Suggestion Banner
                AnimatedVisibility(visible = smartSuggestedFrequency != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¿Convertir en gasto mensual recurrente?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = {
                                    isRecurring = true
                                    selectedFrequency = RecurrenceFrequency.MONTHLY
                                    smartSuggestionDismissed = true
                                }
                            ) {
                                Text("Sí", fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { smartSuggestionDismissed = true }) {
                                Text("No")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Valor / Planned Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        // Allow digits only
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountText = input
                        }
                    },
                    label = { Text("Valor (${currency.code})") },
                    placeholder = { Text("0") },
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
                        .testTag("expense_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Categoría Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Seleccionar categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        leadingIcon = {
                            selectedCategory?.let {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(it.colorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconUtils.getCategoryIcon(it.iconKey),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } ?: Icon(Icons.Default.Category, contentDescription = null)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("category_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color(cat.colorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconUtils.getCategoryIcon(cat.iconKey),
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(cat.name)
                                    }
                                },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fecha Selector
                OutlinedTextField(
                    value = FormatUtils.formatFriendlyDate(selectedDateMillis),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("expense_date_input"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Quick Date Pills (Hoy, Mañana, +7 Días, Fin de Mes)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickDates = listOf(
                        "Hoy" to 0,
                        "Mañana" to 1,
                        "+7 días" to 7,
                        "+15 días" to 15
                    )
                    quickDates.forEach { (label, days) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clickable {
                                val cal = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, days)
                                    set(Calendar.HOUR_OF_DAY, 12)
                                    set(Calendar.MINUTE, 0)
                                }
                                selectedDateMillis = cal.timeInMillis
                            }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Frecuencia / Recurrencia Dropdown
                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = !frequencyExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (isRecurring) selectedFrequency.displayName else "Una vez",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frecuencia") },
                        leadingIcon = {
                            Icon(Icons.Default.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("frequency_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false }
                    ) {
                        RecurrenceFrequency.values().forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.displayName) },
                                onClick = {
                                    selectedFrequency = freq
                                    isRecurring = freq != RecurrenceFrequency.NONE
                                    frequencyExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Método de pago Dropdown
                ExposedDropdownMenuBox(
                    expanded = paymentMethodExpanded,
                    onExpandedChange = { paymentMethodExpanded = !paymentMethodExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMethod.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Método de pago") },
                        leadingIcon = {
                            Icon(
                                imageVector = IconUtils.getPaymentMethodIcon(selectedPaymentMethod.iconKey),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMethodExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("payment_method_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = paymentMethodExpanded,
                        onDismissRequest = { paymentMethodExpanded = false }
                    ) {
                        PaymentMethodType.values().forEach { method ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = IconUtils.getPaymentMethodIcon(method.iconKey),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(method.displayName)
                                    }
                                },
                                onClick = {
                                    selectedPaymentMethod = method
                                    paymentMethodExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nota
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Nota (Opcional)") },
                    placeholder = { Text("Detalles adicionales...") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_notes_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: [ Cancelar ] [ Guardar gasto ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_expense_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && parsedAmount > 0) {
                                onSave(
                                    name,
                                    parsedAmount,
                                    if (selectedStatus == ExpenseStatus.PAID) parsedAmount else null,
                                    selectedCategoryId,
                                    selectedDateMillis,
                                    selectedStatus,
                                    selectedPaymentMethod,
                                    notes,
                                    isRecurring,
                                    selectedFrequency
                                )
                            }
                        },
                        enabled = name.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("save_expense_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (isEditing) "Actualizar" else "Guardar gasto",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
