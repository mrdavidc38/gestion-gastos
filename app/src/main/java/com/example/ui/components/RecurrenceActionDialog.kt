package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.RecurrenceEditOption

@Composable
fun RecurrenceActionDialog(
    isDelete: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (option: RecurrenceEditOption) -> Unit
) {
    var selectedOption by remember { mutableStateOf(RecurrenceEditOption.ONLY_THIS) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("recurrence_action_dialog")
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
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(
                        text = if (isDelete) "¿Qué deseas eliminar?" else "¿Qué deseas modificar?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Solo este gasto
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedOption = RecurrenceEditOption.ONLY_THIS }
                        .padding(vertical = 6.dp)
                ) {
                    RadioButton(
                        selected = selectedOption == RecurrenceEditOption.ONLY_THIS,
                        onClick = { selectedOption = RecurrenceEditOption.ONLY_THIS }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Solo este gasto", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Afecta únicamente la ocurrencia seleccionada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Option 2: Este y los siguientes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedOption = RecurrenceEditOption.THIS_AND_FUTURE }
                        .padding(vertical = 6.dp)
                ) {
                    RadioButton(
                        selected = selectedOption == RecurrenceEditOption.THIS_AND_FUTURE,
                        onClick = { selectedOption = RecurrenceEditOption.THIS_AND_FUTURE }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Este y los siguientes", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Afecta esta fecha y todas las futuras de la serie.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Option 3: Toda la serie
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedOption = RecurrenceEditOption.ALL_SERIES }
                        .padding(vertical = 6.dp)
                ) {
                    RadioButton(
                        selected = selectedOption == RecurrenceEditOption.ALL_SERIES,
                        onClick = { selectedOption = RecurrenceEditOption.ALL_SERIES }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Toda la serie", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Afecta todas las ocurrencias pasadas y futuras.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

                    androidx.compose.material3.Button(
                        onClick = { onConfirm(selectedOption) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_recurrence_action_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (isDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isDelete) "Eliminar" else "Continuar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
