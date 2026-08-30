package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.model.ExpenseStatus
import com.example.model.PaymentMethodType
import com.example.ui.ExpenseFilter
import com.example.ui.SortOption
import com.example.util.IconUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentFilter: ExpenseFilter,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onApplyFilter: (ExpenseFilter) -> Unit,
    onResetFilter: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var tempCategoryId by remember { mutableStateOf(currentFilter.categoryId) }
    var tempStatus by remember { mutableStateOf(currentFilter.status) }
    var tempPaymentMethod by remember { mutableStateOf(currentFilter.paymentMethod) }
    var tempOnlyRecurring by remember { mutableStateOf(currentFilter.onlyRecurring) }
    var tempSortOption by remember { mutableStateOf(currentFilter.sortOption) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("filter_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filtros y orden",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = {
                        tempCategoryId = null
                        tempStatus = null
                        tempPaymentMethod = null
                        tempOnlyRecurring = false
                        tempSortOption = SortOption.DATE_ASC
                        onResetFilter()
                    }
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order / Sorting
            Text(
                text = "Ordenar por",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SortOption.values().forEach { sort ->
                    FilterChip(
                        selected = tempSortOption == sort,
                        onClick = { tempSortOption = sort },
                        label = { Text(sort.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Filter
            Text(
                text = "Estado",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = tempStatus == null,
                    onClick = { tempStatus = null },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = tempStatus == ExpenseStatus.PENDING,
                    onClick = { tempStatus = if (tempStatus == ExpenseStatus.PENDING) null else ExpenseStatus.PENDING },
                    label = { Text("Pendiente") }
                )
                FilterChip(
                    selected = tempStatus == ExpenseStatus.PAID,
                    onClick = { tempStatus = if (tempStatus == ExpenseStatus.PAID) null else ExpenseStatus.PAID },
                    label = { Text("Pagado") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recurring Only Toggle
            Text(
                text = "Tipo de gasto",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !tempOnlyRecurring,
                    onClick = { tempOnlyRecurring = false },
                    label = { Text("Todos los gastos") }
                )
                FilterChip(
                    selected = tempOnlyRecurring,
                    onClick = { tempOnlyRecurring = true },
                    label = { Text("Solo recurrentes 🔁") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = tempCategoryId == null,
                    onClick = { tempCategoryId = null },
                    label = { Text("Todas") }
                )

                categories.forEach { cat ->
                    val isSelected = tempCategoryId == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            tempCategoryId = if (isSelected) null else cat.id
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(cat.colorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconUtils.getCategoryIcon(cat.iconKey),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Filter
            Text(
                text = "Método de pago",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = tempPaymentMethod == null,
                    onClick = { tempPaymentMethod = null },
                    label = { Text("Todos") }
                )

                PaymentMethodType.values().forEach { method ->
                    val isSelected = tempPaymentMethod == method
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            tempPaymentMethod = if (isSelected) null else method
                        },
                        label = { Text(method.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action button
            Button(
                onClick = {
                    onApplyFilter(
                        currentFilter.copy(
                            categoryId = tempCategoryId,
                            status = tempStatus,
                            paymentMethod = tempPaymentMethod,
                            onlyRecurring = tempOnlyRecurring,
                            sortOption = tempSortOption
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apply_filters_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Aplicar filtros", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
