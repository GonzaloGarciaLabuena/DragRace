package com.gonchimonchi.dragrace.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gonchimonchi.dragrace.R
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season
import com.gonchimonchi.dragrace.adapter.RankingAdapter
import com.gonchimonchi.dragrace.viewmodel.RankingViewModelFactory
import com.gonchimonchi.dragrace.viewmodel.RankingViewModel
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import com.gonchimonchi.dragrace.Punto

class TablaRankingActivity : AppCompatActivity() {
    private lateinit var viewModel: RankingViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabla_ranking)

        // Obtener el nombre de la temporada desde un Intent extra (o puedes ponerlo directamente)
        val seasonName = intent.getStringExtra("season_name") ?: "usa15"

        val title = findViewById<TextView>(R.id.titleTable)

        // Crear el ViewModel con Factory
        val factory = RankingViewModelFactory(seasonName)
        viewModel = ViewModelProvider(this, factory)[RankingViewModel::class.java]

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRanking)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observar cambios en la lista de reinas
        viewModel.reinas.observe(this) { reinas ->
            // Paso 1: Calcular la media para cada Reina
            reinas.values.forEach { reina ->
                val total = reina.puntuaciones?.fold(0f) { acc, punto -> acc + (punto.valor ?: 0f) } ?: 0f
                val count = reina.puntuaciones?.size ?: 1
                reina.puntuacionMedia = total / count
            }

            // Paso 2: Ordenar las entradas del mapa por puntuacionMedia
            val sortedMap: LinkedHashMap<String, Reina> = reinas
                .toList() // List<Pair<String, Reina>>
                .sortedByDescending { (_, reina) -> reina.puntuacionMedia ?: 0f }
                .toMap(LinkedHashMap())

            // Paso 3: Pasar el mapa ordenado al adapter
            recyclerView.adapter = RankingAdapter(sortedMap)
        }

        viewModel.season.observe(this) { season ->
            title.text = season.nombre
        }
    }
}