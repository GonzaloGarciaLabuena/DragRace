package com.gonchimonchi.dragrace.activity

import TemporadaAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gonchimonchi.dragrace.R
import android.widget.EditText
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
import com.gonchimonchi.dragrace.classes.Reina
import com.gonchimonchi.dragrace.classes.Season
import com.gonchimonchi.dragrace.adapter.DeleteReinaAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.jvm.java

class DeleteReinaActivity : AppCompatActivity() {
    private lateinit var viewModelDropbox: DropboxViewModel

    private lateinit var filterNombre: EditText
    private lateinit var filterSeason: TextView
    private lateinit var temporadaSeleccionada: Season
    private lateinit var recyclerView: RecyclerView
    private var reinasCargadas: List<Reina> = emptyList()
    private var temporadasCargadas: List<Season> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_reina)
        filterNombre = findViewById(R.id.filterNombre)
        filterSeason = findViewById(R.id.filterSeason)
        viewModelDropbox = ViewModelProvider(this)[DropboxViewModel::class.java]



        val temporadaViewModel = ViewModelProvider(this)[TemporadaViewModel::class.java]
        temporadaViewModel.obtenerSeasonsConReinas()
        temporadaViewModel.seasonCompleta.observe(this) { listaSeasons ->
            reinasCargadas = listaSeasons
                .flatMap { it.reinas }
                .sortedBy { it.nombre.lowercase() }
            temporadasCargadas = listaSeasons
            cargarLista(reinasCargadas)
        }

        filterNombre.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString().lowercase().trim()
                val reinasFiltradas = reinasCargadas.filter {
                    it.nombre.lowercase().contains(texto) == true
                }
                cargarLista(reinasFiltradas)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        filterSeason.setOnClickListener {
            if (temporadasCargadas.isNotEmpty()) {
                mostrarBottomSheetTemporadas(temporadasCargadas) { seleccionada ->
                    filterSeason.text = seleccionada.nombre
                    temporadaSeleccionada = seleccionada
                }
            } else {
                Toast.makeText(this, "Temporadas no cargadas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun cargarLista(lista: List<Reina>) {
        recyclerView = findViewById(R.id.recyclerViewDeleteReina)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val viewModelReina = ViewModelProvider(this)[ReinaViewModel::class.java]

        recyclerView.adapter = DeleteReinaAdapter(lista) { reina ->
            AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar a ${reina.nombre}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    val nombre = reina.nombre ?: return@setPositiveButton
                    val season = reina.temporada ?: return@setPositiveButton
                    viewModelReina.deleteReinaModel(reina) { resultado ->
                        if (resultado.isSuccess) {
                            // 🔹 Eliminar imagen en segundo plano
                            lifecycleScope.launch {
                                viewModelDropbox.eliminarImagen(nombre, season)
                            }

                            Toast.makeText(this, "Reina eliminada correctamente", Toast.LENGTH_SHORT).show()

                            // 🔹 Actualiza la lista eliminando la reina visualmente
                            val nuevaLista = lista.filter { it.id != reina.id }
                            runOnUiThread {
                                cargarLista(nuevaLista)
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "Error al eliminar reina", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
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