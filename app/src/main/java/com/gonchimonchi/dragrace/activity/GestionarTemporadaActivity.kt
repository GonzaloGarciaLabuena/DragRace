package com.gonchimonchi.dragrace.activity

import TemporadaAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gonchimonchi.dragrace.R
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.gonchimonchi.dragrace.viewmodel.TemporadaViewModel
import kotlinx.coroutines.launch
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gonchimonchi.dragrace.classes.Season
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.jvm.java
import com.gonchimonchi.dragrace.adapter.GestionarCapituloAdapter
import android.util.Log
import com.gonchimonchi.dragrace.ui.Utils
import androidx.core.graphics.toColorInt

class GestionarTemporadaActivity : AppCompatActivity() {
    private lateinit var filterSeason: TextView
    private lateinit var seasonNombre: EditText
    private lateinit var seasonYear: EditText
    private lateinit var seasonFranquicia: EditText
    private lateinit var btnGuardar: Button
    private lateinit var resetButton: ImageButton
    private lateinit var addCapitulo: ImageButton
    private var temporadasCargadas: List<Season> = emptyList()
    private var temporadaSeleccionada: Season? = null


    private lateinit var recyclerViewCapitulos: RecyclerView
    private lateinit var capituloAdapter: GestionarCapituloAdapter
    private val capitulos: MutableList<String> = mutableListOf()

    private lateinit var temporadaViewModel: TemporadaViewModel

    companion object {
        private const val ITEM_HEIGHT_DP = 80
    }

    @SuppressLint("UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temporada)

        seasonNombre = findViewById(R.id.editNombre)
        seasonYear = findViewById(R.id.editYear)
        seasonFranquicia = findViewById(R.id.editFranquicia)
        btnGuardar = findViewById<Button>(R.id.guardarRankingBtn)
        resetButton = findViewById<ImageButton>(R.id.btnResetCampos)
        addCapitulo = findViewById<ImageButton>(R.id.btnAddCapitulo)

        filterSeason = findViewById(R.id.filterSeason)
        filterSeason.setOnClickListener {
            if (temporadasCargadas.isNotEmpty()) {
                mostrarBottomSheetTemporadas(temporadasCargadas) { seleccionada ->
                    seasonNombre.setText(seleccionada.nombre)
                    seasonYear.setText(seleccionada.year.toString())
                    seasonFranquicia.setText(seleccionada.franquicia)
                    temporadaSeleccionada = seleccionada
                    filterSeason.text = seleccionada.nombre
                    btnGuardar.text = "Actualizar temporada"

                    capitulos.clear()
                    seleccionada.capitulos.let { capitulos.addAll(it) }
                    capituloAdapter.notifyDataSetChanged()
                    ajustarAlturaRecyclerView(recyclerViewCapitulos, capitulos.size)
                    lifecycleScope.launch {
                        Log.i("TEMPORADA" , "COLORES GENERAR")
                        Utils().generarColoresImagenUrl(this@GestionarTemporadaActivity, seleccionada) { colores ->
                            colores?.let {
                                Log.i("TEMPORADA" , "COLORES $colores")
                                // Aplica a la UI si quieres
                                filterSeason.setBackgroundColor(it.dominante.toColorInt())

                                temporadaViewModel.actualizarPaletaTemporada(seleccionada, colores) { result ->
                                    runOnUiThread {
                                        result.onSuccess {
                                            Toast.makeText(this@GestionarTemporadaActivity, "Paleta actualizada correctamente", Toast.LENGTH_SHORT).show()
                                        }.onFailure { error ->
                                            Toast.makeText(this@GestionarTemporadaActivity, "Error al actualizar paleta: ${error.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }

                            }
                        }
                    }

                }
            } else {
                Toast.makeText(this, "Temporadas no cargadas", Toast.LENGTH_SHORT).show()
            }
        }


        btnGuardar.setOnClickListener{
            val nombre = seasonNombre.text.toString().trim()
            val franquicia = seasonFranquicia.text.toString().trim()
            val year = seasonYear.text.toString().toIntOrNull()

            if (nombre.isEmpty() || franquicia.isEmpty() || year == null) {
                Toast.makeText(
                    this,
                    "Rellena todos los campos correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if(temporadaSeleccionada == null){
                val textoFranquicia = seasonFranquicia.text.toString().trim()
                val partes = textoFranquicia.split(" ")

                if (partes.size < 2) {
                    Toast.makeText(this, "Formato de franquicia incorrecto. Usa formato como 'USA 15'", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val franquicia = partes.slice(0 until partes.size - 1).joinToString(" ").trim().uppercase()
                val numeroTemporada = partes.last()
                Log.i("TEMPORADA", "$franquicia $numeroTemporada")
                val idDocumento = when (franquicia) {
                    "USA" -> "S$numeroTemporada"
                    "USA ALL STARS" -> "AS$numeroTemporada"
                    "UK" -> "DRUK$numeroTemporada"
                    "UK VS THE WORLD" -> "UKvsTW$numeroTemporada"
                    "ESPAÑA" -> "DRES$numeroTemporada"
                    "ESPAÑA ALL STARS" -> "ESAS$numeroTemporada"
                    "FRANCIA" -> "DRFR$numeroTemporada"
                    "FRANCIA ALL STARS" -> "DRFRAS$numeroTemporada"
                    "MEXICO" -> "DRMX$numeroTemporada"
                    "DOWN UNDER" -> "DRDU$numeroTemporada"
                    "GLOBAL ALL STARS" -> "GAS$numeroTemporada"
                    else -> {
                        Toast.makeText(this, "Franquicia desconocida: $franquicia", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }

                val seasonNueva = Season(
                    nombre = nombre,
                    franquicia = franquicia,
                    year = year,
                    capitulos = capitulos.toList()
                )
                temporadaViewModel.addSeason(seasonNueva, idDocumento) { result ->
                    result.onSuccess { nuevaConId ->
                        Toast.makeText(this, "Temporada guardada", Toast.LENGTH_SHORT).show()
                        btnGuardar.text = "Actualizar temporada"
                        filterSeason.text = nombre
                        temporadaSeleccionada = nuevaConId

                        temporadasCargadas = temporadasCargadas + nuevaConId
                    }.onFailure {
                        Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }

            }else {
                Log.d("capitulos", " Capitulos: ${capitulos.toList()}")
                val seasonActualizada = temporadaSeleccionada!!.copy(
                    nombre = nombre,
                    franquicia = franquicia,
                    year = year,
                    capitulos = capitulos.toList()
                )
                temporadaViewModel.actualizarCapitulosTemporada(seasonActualizada) { result ->
                    result.onSuccess {
                        Toast.makeText(this, "Temporada guardada", Toast.LENGTH_SHORT).show()
                        btnGuardar.text = "Actualizar temporada"
                        filterSeason.text = nombre
                        temporadaSeleccionada = seasonActualizada
                        temporadasCargadas = temporadasCargadas.map {
                            if (it.id == seasonActualizada.id) seasonActualizada else it
                        }
                    }.onFailure {
                        Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }

        resetButton.setOnClickListener {
            seasonNombre.text.clear()
            seasonYear.text.clear()
            seasonFranquicia.text.clear()
            filterSeason.text = "Buscar temporada"
            temporadaSeleccionada = null
            btnGuardar.text = "Guardar temporada"
            capitulos.clear()
            capituloAdapter.notifyDataSetChanged()
            ajustarAlturaRecyclerView(recyclerViewCapitulos, capitulos.size)
        }

        addCapitulo.setOnClickListener {
            capitulos.add("")
            val index = capitulos.size - 1
            ajustarAlturaRecyclerView(recyclerViewCapitulos, capitulos.size)
            capituloAdapter.notifyItemInserted(index)

            recyclerViewCapitulos.post {
                recyclerViewCapitulos.smoothScrollToPosition(index)
                // Esperamos un poco antes de intentar enfocar
                recyclerViewCapitulos.postDelayed({
                    val holder = recyclerViewCapitulos.findViewHolderForAdapterPosition(index)
                    if (holder is GestionarCapituloAdapter.CapituloViewHolder) {
                        holder.editCapitulo.requestFocus()
                    }
                }, 100)
            }
        }


        recyclerViewCapitulos = findViewById(R.id.recyclerViewCapitulos)
        capituloAdapter = GestionarCapituloAdapter(
            capitulos,
            onSaveClick = { position, nuevoTexto ->
                capitulos[position] = nuevoTexto
                Toast.makeText(this, "Capítulo guardado", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { index ->
                capitulos.removeAt(index)
                ajustarAlturaRecyclerView(recyclerViewCapitulos, capitulos.size)
                capituloAdapter.notifyItemRemoved(index)
            }
        )
        recyclerViewCapitulos.adapter = capituloAdapter
        recyclerViewCapitulos.layoutManager = LinearLayoutManager(this)

        temporadaViewModel = ViewModelProvider(this)[TemporadaViewModel::class.java]
        temporadaViewModel.obtenerSeasonsVacia()
        temporadaViewModel.seasons.observe(this) { listaSeasons ->
            temporadasCargadas = listaSeasons
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

    fun ajustarAlturaRecyclerView(recyclerView: RecyclerView, totalItems: Int) {
        val density = recyclerView.resources.displayMetrics.density
        val layoutParams = recyclerView.layoutParams
        layoutParams.height = (ITEM_HEIGHT_DP * totalItems * density).toInt()
        recyclerView.layoutParams = layoutParams
    }
}