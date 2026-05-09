package com.shaun.stocktracker.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shaun.stocktracker.data.entity.AlertCondition
import com.shaun.stocktracker.data.entity.AlertType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertBottomSheet(
    viewModel: StockDetailViewModel,
    onDismiss: () -> Unit,
    currentLtp: Double
) {
    val alerts by viewModel.alerts.collectAsState()
    
    var priceInput by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(AlertType.ABOVE) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Alerts for ${viewModel.symbol}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Current Price: ₹%.2f".format(currentLtp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // ── Existing Alerts List ──
            if (alerts.isEmpty()) {
                Text(
                    text = "No alerts set.",
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        AlertRow(alert = alert, onDelete = { viewModel.deleteAlert(it) })
                        Divider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Add New Alert Section ──
            Text(
                text = "Add New Alert",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown for ABOVE/BELOW
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        AlertType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Price Input
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val price = priceInput.toDoubleOrNull()
                    if (price != null && price > 0) {
                        viewModel.addAlert(selectedType, price)
                        priceInput = "" // reset
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = priceInput.isNotBlank()
            ) {
                Text("Add Alert")
            }
        }
    }
}

@Composable
fun AlertRow(alert: AlertCondition, onDelete: (AlertCondition) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "${alert.alertType.displayName} ₹%.2f".format(alert.targetPrice),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (alert.isEnabled) "Active" else "Disabled",
                style = MaterialTheme.typography.bodySmall,
                color = if (alert.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { onDelete(alert) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Alert", tint = MaterialTheme.colorScheme.error)
        }
    }
}
