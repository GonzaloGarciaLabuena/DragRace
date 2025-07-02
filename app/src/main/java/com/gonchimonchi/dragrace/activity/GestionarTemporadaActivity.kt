package com.gonchimonchi.dragrace.activity

import TemporadaAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gonchimonchi.dragrace.R
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.gonchimonchi.dragrace.viewmodel.DropboxViewModel
import com.gonchimonchi.dragrace.viewmodel.ReinaViewModel
import com.gonchimonchi.dragrace.viewmodel.TemporadaViewModel
import kotlinx.coroutines.launch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.jvm.java
import com.gonchimonchi.dragrace.adapter.GestionarCapituloAdapter
import android.util.Log
import android.view.View

class GestionarTemporadaActivity : AppCompatActivity() {
    private lateinit var filterSeason: TextView
    private lateinit var seasonNombre: EditText
    private lateinit var seasonYear: EditText
    private lateinit var seasonFranquicia: EditText
    private lateinit var btnGuardar: Button
    private lateinit var resetButton: ImageButton
    private var temporadasCargadas: List<Season> = emptyList()
    private lateinit var temporadaSeleccionada: Season


    private lateinit var recyclerViewCapitulos: RecyclerView
    private lateinit var capituloAdapter: GestionarCapituloAdapter
    private val capitulos: MutableList<String> = mutableListOf()

    private lateinit var temporadaViewModel: TemporadaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temporada)

        seasonNombre = findViewById(R.id.editNombre)
        seasonYear = findViewById(R.id.editYear)
        seasonFranquicia = findViewById(R.id.editFranquicia)
        btnGuardar = findViewById<Button>(R.id.guardarRankingBtn)
        resetButton = findViewById<ImageButton>(R.id.btnResetCampos)

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
                    seleccionada.capitulos?.let { capitulos.addAll(it) }

                    capituloAdapter = GestionarCapituloAdapter(
                        capitulos,
                        onSaveClick = { position, nuevoTexto ->
                            capitulos[position] = nuevoTexto
                            Toast.makeText(this, "Capítulo guardado", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteClick = { index ->
                            capitulos.removeAt(index)
                            capituloAdapter.notifyItemRemoved(index)
                        },
                        onAddClick = {
                            capitulos.add("Nuevo capítulo")
                            capituloAdapter.notifyItemInserted(capitulos.size - 1)
                            recyclerViewCapitulos.scrollToPosition(capitulos.size - 1)
                        }
                    )
                    recyclerViewCapitulos.adapter = capituloAdapter
                }
            } else {
                Toast.makeText(this, "Temporadas no cargadas", Toast.LENGTH_SHORT).show()
            }
        }


        btnGuardar.setOnClickListener {
            val nombre = seasonNombre.text.toString().trim()
            val franquicia = seasonFranquicia.text.toString().trim()
            val year = seasonYear.text.toString().toIntOrNull()

            if (nombre.isEmpty() || franquicia.isEmpty() || year == null) {
                Toast.makeText(this, "Rellena todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Asegurarse de que temporadaSeleccionada está inicializada
            if (!::temporadaSeleccionada.isInitialized) {
                temporadaSeleccionada = Season()
            }
            Log.d("capitulos", " Capitulos: ${capitulos.toList()}")
            val seasonActualizada = temporadaSeleccionada.copy(
                nombre = nombre,
                franquicia = franquicia,
                year = year,
                capitulos = capitulos.toList()
            )
            temporadaViewModel = ViewModelProvider(this)[TemporadaViewModel::class.java]
            temporadaViewModel.actualizarCapitulosTemporada(seasonActualizada) { result ->
                result.onSuccess {
                    Toast.makeText(this, "Temporada guardada", Toast.LENGTH_SHORT).show()
                    btnGuardar.text = "Actualizar temporada"
                    filterSeason.text = nombre
                    temporadaSeleccionada = seasonActualizada
                }.onFailure {
                    Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }



        resetButton.setOnClickListener {
            seasonNombre.text.clear()
            seasonYear.text.clear()
            seasonFranquicia.text.clear()
            filterSeason.text = "Buscar temporada"
            temporadaSeleccionada = Season()
            findViewById<Button>(R.id.guardarRankingBtn).text = "Guardar temporada"
            capitulos.clear()
            capituloAdapter = GestionarCapituloAdapter(
                capitulos,
                onSaveClick = { position, nuevoTexto ->
                    capitulos[position] = nuevoTexto
                    Toast.makeText(this, "Capítulo guardado", Toast.LENGTH_SHORT).show()
                },
                onDeleteClick = { index ->
                    capitulos.removeAt(index)
                    capituloAdapter.notifyItemRemoved(index)
                },
                onAddClick = {
                    capitulos.add("Nuevo capítulo")
                    capituloAdapter.notifyItemInserted(capitulos.size - 1)
                    recyclerViewCapitulos.scrollToPosition(capitulos.size - 1)
                }
            )
            recyclerViewCapitulos.adapter = capituloAdapter
        }

        recyclerViewCapitulos = findViewById(R.id.recyclerViewCapitulos)
        recyclerViewCapitulos.layoutManager = LinearLayoutManager(this)

        val temporadaViewModel = ViewModelProvider(this)[TemporadaViewModel::class.java]
        temporadaViewModel.obtenerSeasonsVacia()
        temporadaViewModel.seasons.observe(this) { listaSeasons ->
            temporadasCargadas = listaSeasons
        }

        val rootView = findViewById<View>(android.R.id.content)
        val spacer = findViewById<View>(R.id.keyboardSpacer)

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                spacer.layoutParams.height = keypadHeight
                spacer.visibility = View.VISIBLE
                spacer.requestLayout()
            } else {
                spacer.layoutParams.height = 0
                spacer.visibility = View.GONE
                spacer.requestLayout()
            }
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