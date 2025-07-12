package com.gonchimonchi.dragrace.activity

import TemporadaAdapter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gonchimonchi.dragrace.Punto
import com.gonchimonchi.dragrace.R
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season
import com.gonchimonchi.dragrace.adapter.RankingAdapter
import com.gonchimonchi.dragrace.ui.Utils
import com.gonchimonchi.dragrace.utils.ZoomLayout
import com.gonchimonchi.dragrace.viewmodel.EstadoViewModel
import com.gonchimonchi.dragrace.viewmodel.TemporadaViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.LinearLayout

class TablaRankingActivity : AppCompatActivity() {
    private lateinit var viewModelTemporada: TemporadaViewModel
    private lateinit var filterSeason: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var guardarRankingBtn: Button
    private var listaPuntos: List<Punto> = emptyList()
    private var temporadasCargadas: List<Season> = emptyList()
    private lateinit var temporadaSeleccionada: Season
    private var listaOrdenadaReinas: MutableList<Reina>? = null
    private lateinit var zoomLayout: ZoomLayout
    private lateinit var layoutRoot: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabla_ranking)

        recyclerView = findViewById(R.id.recyclerViewRanking)
        recyclerView.layoutManager = LinearLayoutManager(this)
        filterSeason = findViewById(R.id.filterSeason)
        guardarRankingBtn = findViewById(R.id.guardarRankingBtn)

        layoutRoot = findViewById(R.id.layoutRoot)

        zoomLayout = findViewById(R.id.zoomLayout)
        zoomLayout.onScaleChanged = { factor ->
            (recyclerView.adapter as? RankingAdapter)?.setScaleFactor(factor)
        }

        val estadoViewModel = ViewModelProvider(this)[EstadoViewModel::class.java]
        estadoViewModel.obtenerListaPuntos()
        estadoViewModel.listaPuntos.observe(this) { lista ->
            listaPuntos = lista
            Log.d("TablaRanking", "listaPuntos recibida con tamaño: ${listaPuntos.size}")
            intentarMostrarRanking()
        }

        viewModelTemporada = ViewModelProvider(this)[TemporadaViewModel::class.java]

        viewModelTemporada.obtenerSeasonsVacia()
        viewModelTemporada.seasons.observe(this) { listaSeasons ->
            temporadasCargadas = listaSeasons
            Log.d("TablaRanking", "temporadasCargadas recibidas: ${temporadasCargadas.size}")
        }

        filterSeason.setOnClickListener {
            if (temporadasCargadas.isNotEmpty()) {
                mostrarBottomSheetTemporadas(temporadasCargadas) { seleccionada ->
                    temporadaSeleccionada = seleccionada
                    filterSeason.text = seleccionada.nombre
                    Log.d("TablaRanking", "Temporada seleccionada: ${seleccionada.nombre} (${seleccionada.id})")

                    // Limpiar datos anteriores
                    listaOrdenadaReinas?.clear()
                    recyclerView.adapter = null

                    viewModelTemporada.obtenerSeasonCompleta(temporadaSeleccionada)
                }
            } else {
                Toast.makeText(this, "Temporadas no cargadas", Toast.LENGTH_SHORT).show()
                Log.d("TablaRanking", "Intento de seleccionar temporada pero no hay temporadas cargadas")
            }
        }

        viewModelTemporada.seasonCompleta.observe(this) { seasons ->
            Log.d("TablaRanking", "seasonCompleta recibida con tamaño: ${seasons.size}")
            if (seasons.isNotEmpty()) {
                temporadaSeleccionada = seasons[0]
                Log.d("TablaRanking", "Temporada completa cargada: ${temporadaSeleccionada.nombre}")
                Log.d("TablaRanking", "PALETA ${temporadaSeleccionada.paleta}")
                intentarMostrarRanking()
            }
        }

        guardarRankingBtn.setOnClickListener {
            if (::temporadaSeleccionada.isInitialized && !listaOrdenadaReinas.isNullOrEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Confirmar guardado")
                    .setMessage("Se van a guardar los datos del ranking en Firebase. ¿Deseas continuar?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        Log.d("TablaRanking", "Guardando ranking para temporada: ${temporadaSeleccionada.nombre}")
                        viewModelTemporada.actualizarTemporada(listaOrdenadaReinas!!, temporadaSeleccionada)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                Toast.makeText(this, "No hay datos para guardar", Toast.LENGTH_SHORT).show()
                Log.d("TablaRanking", "Intento de guardar pero no hay datos válidos")
            }
        }

        viewModelTemporada.addRankingStatus.observe(this) { resultado ->
            resultado.onSuccess {
                Toast.makeText(this, "Ranking guardado con éxito", Toast.LENGTH_SHORT).show()
                Log.d("TablaRanking", "Ranking guardado con éxito")
            }.onFailure {
                Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
                Log.e("TablaRanking", "Error al guardar ranking", it)
            }
        }
    }

    private fun intentarMostrarRanking() {
        if (!::temporadaSeleccionada.isInitialized) {
            Log.d("TablaRanking", "Temporada seleccionada no inicializada, no se muestra ranking")
            return
        }
        val reinas = temporadaSeleccionada.reinas ?: run {
            Log.d("TablaRanking", "Temporada sin reinas")
            return
        }
        if (reinas.isEmpty()) {
            Log.d("TablaRanking", "Lista de reinas vacía, no se muestra ranking")
            return
        }
        if (listaPuntos.isEmpty()) {
            Log.d("TablaRanking", "Lista de puntos vacía, no se muestra ranking")
            return
        }

        temporadaSeleccionada.paleta?.let {
            val utils = Utils()
            val fondoGeneral = Color.parseColor(it.suave)
            val fondoElementos = Color.parseColor(it.dominante)
            val colorTexto = if (utils.esColorOscuro(it.dominante)) Color.WHITE else Color.BLACK

            // Fondo de la actividad
            layoutRoot.setBackgroundColor(fondoGeneral)

            // Botón
            guardarRankingBtn.setBackgroundColor(fondoElementos)
            guardarRankingBtn.setTextColor(colorTexto)
        }


        listaOrdenadaReinas = reinas.map { reina ->
            Log.d("TablaRanking", "Lista de puntos filtrados reina (Punto) ${reina.puntuaciones}")
            val valores = reina.puntuaciones
                ?.filterNotNull()
                ?.filter { it.texto != "NA" }
                ?.mapNotNull { it.valor }
            Log.d("TablaRanking", "Valores puntos filtrados reina $valores")
            val media = if (!valores.isNullOrEmpty()) valores.average().toFloat() else 0f
            reina.apply { puntuacionMedia = media }
        }.sortedByDescending { it.puntuacionMedia }
            .toMutableList()

        Log.d("TablaRanking", "Mostrando ranking con ${listaOrdenadaReinas?.size} reinas")
        try {
            recyclerView.adapter = RankingAdapter(listaOrdenadaReinas!!, temporadaSeleccionada, listaPuntos)
            Log.d("TablaRanking", "Adapter asignado correctamente")
        } catch (e: Exception) {
            Log.e("TablaRanking", "Error al asignar adapter", e)
        }
    }

    fun mostrarBottomSheetTemporadas(temporadas: List<Season>, onSeleccion: (Season) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_temporadas, null)
        dialog.setContentView(view)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerTemporadas)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = TemporadaAdapter(temporadas) {
            onSeleccion(it)
            dialog.dismiss()
        }

        dialog.show()
    }
}
