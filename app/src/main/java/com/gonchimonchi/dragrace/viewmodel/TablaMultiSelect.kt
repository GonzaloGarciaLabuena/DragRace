package com.gonchimonchi.dragrace.viewmodel

import androidx.annotation.OptIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.gonchimonchi.dragrace.calls.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

data class Season(
    var franquicia: String? = null,
    val nombre: String? = null,
    val year: Int? = null,
    val reinas: List<String>? = null,
    val capitulos: List<String>? = null
)

data class Reina(
    val nombre: String? = null,
    val imagen: String? = null,
    var temporadas: List<String>? = null
)

@OptIn(UnstableApi::class)
@Composable
fun TablaMultiSelect(modifier: Modifier = Modifier) {
    var optionsState = produceState<List<Option>>(initialValue = emptyList()) {
        value = getPuntosName()
    }
    val options = optionsState.value

    val emptySeason = Season(franquicia = null, nombre = null, year = null, reinas = emptyList(), capitulos = emptyList())

    val seasonState = produceState(initialValue = emptySeason) {
        value = getSeasonData("usa15") ?: emptySeason
    }
    val season = seasonState.value

    // Nueva llamada para cargar las reinas completas a partir de sus IDs
    val reinasState = produceState<List<Reina>>(initialValue = emptyList(), key1 = season.reinas) {
        val ids = season.reinas ?: emptyList()
        value = if (ids.isNotEmpty()) getReinasByIds(ids) else emptyList()
    }
    val reinas = reinasState.value

    // tableData guarda la lista de opciones seleccionadas para cada celda
    val tableData = remember {
        mutableStateListOf<SnapshotStateList<Option?>>()
    }

    LaunchedEffect(reinas) {
        tableData.clear()
        reinas.forEach {
            tableData.add(mutableStateListOf<Option?>().apply {
                repeat(season.capitulos?.size ?: 0) {
                    add(null)
                }
            })
        }
    }

    val scrollState = rememberScrollState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(scrollState) // Scroll en toda el área
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Column {
            // 🔷 Fila header: capítulos
            Row {
                Text(
                    text = "", // esquina vacía
                    modifier = Modifier
                        .width(100.dp)
                        .padding(8.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )

                season.capitulos?.forEach { capitulo ->
                    Text(
                        text = capitulo,
                        modifier = Modifier
                            .border(1.dp, Color.Gray)
                            .width(116.dp)
                            .padding(8.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔶 Filas con nombre de reina + celdas
            reinas.forEachIndexed { rowIndex, reina ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Primera celda: nombre de la reina
                    Text(
                        text = reina.nombre ?: "Not found",
                        modifier = Modifier
                            .width(100.dp)
                            .padding(8.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )

                    tableData.getOrNull(rowIndex)?.forEachIndexed { colIndex, selectedOption ->
                        MultiSelectCell(
                            options = options,
                            selectedOption = selectedOption,
                            onSelectionChange = { newSelection ->
                                tableData[rowIndex][colIndex] = newSelection
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}