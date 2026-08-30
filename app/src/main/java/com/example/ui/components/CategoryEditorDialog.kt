package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CategoryEntity
import com.example.util.IconUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
    initialCategory: CategoryEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String, colorHex: Long) -> Unit
) {
    val isEditing = initialCategory != null
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var selectedIconKey by remember { mutableStateOf(initialCategory?.iconKey ?: "shopping_bag") }
    var selectedColorHex by remember { mutableLongStateOf(initialCategory?.colorHex ?: 0xFFE07A5F) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("category_editor_dialog")
                .clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isEditing) "Editar categoría" else "Nueva categoría",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la categoría") },
                    placeholder = { Text("Ej. Mascotas, Inversiones...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color Selection
                Text(
                    text = "Seleccionar color",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconUtils.AVAILABLE_CATEGORY_COLORS.forEach { colorHex ->
                        val isSelected = selectedColorHex == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .clickable { selectedColorHex = colorHex }
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Selection
                Text(
                    text = "Seleccionar icono",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconUtils.AVAILABLE_CATEGORY_ICONS.forEach { (key, label) ->
                        val isSelected = selectedIconKey == key
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Color(selectedColorHex)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIconKey = key }
                                .then(
                                    if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconUtils.getCategoryIcon(key),
                                contentDescription = label,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                            if (name.isNotBlank()) {
                                onSave(name, selectedIconKey, selectedColorHex)
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("save_category_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (isEditing) "Actualizar" else "Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
