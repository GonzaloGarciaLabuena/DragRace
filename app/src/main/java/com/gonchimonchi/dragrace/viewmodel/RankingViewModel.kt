package com.gonchimonchi.dragrace.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.google.firebase.firestore.FirebaseFirestore
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season
import com.gonchimonchi.dragrace.calls.*

import androidx.lifecycle.viewModelScope
import com.gonchimonchi.dragrace.Punto
import kotlinx.coroutines.launch

class RankingViewModel (seasonName: String): ViewModel() {

    private val _reinas = MutableLiveData<Map<String, Reina>>()
    val reinas: LiveData<Map<String, Reina>> = _reinas

    private val _season = MutableLiveData<Season>()
    val season: LiveData<Season> = _season

    init {
        //Carga la temporada segun el nombre
        cargarSeason(seasonName)
    }

    private fun cargarSeason(seasonName: String) {
        viewModelScope.launch {
            val seasonData = getSeasonData(seasonName)
            _season.postValue(seasonData)
            Log.i("BBDD", "SEASON. ${seasonData}")

            val listaIds = seasonData?.reinas ?: emptyList()

            // Cargar reinas y esperar a tenerlas antes de cargar puntuaciones
            val mapaReinas = getReinasByIds(listaIds)
            val mapaActualizado = mutableMapOf<String, Reina>()

            for ((id, reina) in mapaReinas) {
                val puntos = getPuntosReina(seasonName, id)
                val reinaConPuntos = reina.copy(
                    puntuaciones = puntos
                )
                mapaActualizado[id] = reinaConPuntos
                Log.i("BBDD", "✅ Reina $id actualizada con puntuaciones $puntos")
            }
            Log.i("BBDD", "✅ Mapa Actualizado $mapaActualizado")
            _reinas.postValue(mapaActualizado)
        }
    }
}