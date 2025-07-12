package com.gonchimonchi.dragrace.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

import com.google.firebase.firestore.FirebaseFirestore
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season
import com.gonchimonchi.dragrace.calls.*

import androidx.lifecycle.viewModelScope
import com.gonchimonchi.dragrace.ColorPalette
import com.gonchimonchi.dragrace.Punto
import kotlinx.coroutines.launch

class TemporadaViewModel (): ViewModel() {
    private val _season = MutableLiveData<Season>()
    val season: LiveData<Season> = _season

    private val _seasons = MutableLiveData<List<Season>>()
    val seasons: LiveData<List<Season>> = _seasons

    private val _seasonCompleta = MutableLiveData<List<Season>>() // temporada completa
    val seasonCompleta: LiveData<List<Season>> = _seasonCompleta

    private val _addRankingStatus = MutableLiveData<Result<String>>()
    val addRankingStatus: LiveData<Result<String>> = _addRankingStatus

    private val _addCapituloStatus = MutableLiveData<Result<String>>()
    val addCapituloStatus: LiveData<Result<String>> = _addCapituloStatus

    fun addSeason(season: Season, idDocumento: String, onResult: (Result<Season>) -> Unit) {
        viewModelScope.launch {
            addNewTemporada(season, idDocumento) { result ->
                onResult(result)
            }
        }
    }


    fun obtenerSeasonCompleta(season: Season){
        viewModelScope.launch {
            val seasons = getSeason(season).map{ estaSeason -> // Solo hay una season
                val listaReinas = estaSeason.reinas ?: emptyList()
                estaSeason.reinas = actualizarListaReinasConPuntos(listaReinas, estaSeason)
                estaSeason
            }
            _seasonCompleta.value = seasons
        }
    }

    fun obtenerSeasonsVacia() {
        viewModelScope.launch {
            _seasons.value = getSeasonsVacia()
        }
    }

    fun obtenerSeasonsConReinas() {
        viewModelScope.launch {
            ->
            _seasonCompleta.value = getSeasonsPoblada()
        }
    }

    fun actualizarTemporada(
        reinas: MutableList<Reina>?,
        season: Season
    ) {
        if (reinas.isNullOrEmpty()) {
            _addRankingStatus.postValue(Result.failure(Exception("No hay reinas para actualizar")))
            return
        }

        viewModelScope.launch {
            actualizarRanking(reinas, season) { result ->
                _addRankingStatus.postValue(result)
            }
        }
    }

    private suspend fun actualizarListaReinasConPuntos(
        reinas: List<Reina>,
        season: Season
    ): MutableList<Reina> {
        val listaActualizada = reinas.mapNotNull { reina ->
            reina.id?.let { id ->
                val puntos = getPuntosReina(season.id.toString(), id)
                Log.i("BBDD", "✅ Reina $id actualizada con puntuaciones $puntos")
                reina.copy(puntuaciones = puntos?.toMutableList())
            }
        }
        return listaActualizada.toMutableList()
    }

    fun actualizarCapitulosTemporada(season: Season, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            updateCapituloTemporada(season, onResult)
        }
    }

    fun actualizarPaletaTemporada(
        season: Season,
        paleta: ColorPalette,
        onResult: (Result<Unit>) -> Unit
    ) {
        val id = season.id
        if (id == null) {
            onResult(Result.failure(Exception("ID de temporada no disponible")))
            return
        }

        // No hace falta lanzar coroutine porque la función usa callbacks
        actualizarPaletaTemporadaFB(id, paleta) { result ->
            onResult(result)
        }
    }



    fun copiarTemporada(){
        viewModelScope.launch {
            copiarDocumento(
                db = FirebaseFirestore.getInstance(),
                coleccion = "season",
                idOriginal = "usa15",
                idNuevo = "S15",
            ) { exito, error ->
                if (exito) {
                    Log.i("Firebase", "Documento copiado correctamente")
                } else {
                    Log.e("Firebase", "Error copiando documento", error)
                }
            }
            copiarDocumento(
                db = FirebaseFirestore.getInstance(),
                coleccion = "season",
                idOriginal = "usa17",
                idNuevo = "S17",
            ) { exito, error ->
                if (exito) {
                    Log.i("Firebase", "Documento copiado correctamente")
                } else {
                    Log.e("Firebase", "Error copiando documento", error)
                }
            }
        }
    }

}