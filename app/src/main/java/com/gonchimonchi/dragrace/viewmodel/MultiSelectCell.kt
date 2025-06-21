package com.gonchimonchi.dragrace.viewmodel

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class Option(val id: String, val value: String)

@Composable
fun MultiSelectCell(
    options: List<Option>,
    selectedOption: Option?,
    onSelectionChange: (Option?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // Celda visible que muestra las opciones seleccionadas
        Text(
            text = selectedOption?.id ?: "",
            modifier = Modifier
                .clickable { expanded = true }
                .border(1.dp, Color.Gray)
                .padding(8.dp)
                .width(100.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(Modifier.width(6.dp))
                            Text(option.id)
                        }
                    },
                    onClick = {
                        onSelectionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}