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

class ReinaViewModel (): ViewModel() {
    private val _addReinaStatus = MutableLiveData<Result<String>>()
    val addReinaStatus: LiveData<Result<String>> = _addReinaStatus

    fun guardarReina(nombre: String, imagenUrl: String, seasonName: String){
        viewModelScope.launch {
            addReina(nombre, imagenUrl, seasonName) { result ->
                _addReinaStatus.postValue(result)
            }
        }
    }

    fun deleteReinaModel(reina: Reina, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                // Lógica para eliminar de Firebase
                deleteReina(reina) // suspend fun
                onResult(Result.success(Unit))
            } catch (e: Exception) {
                Log.e("ReinaViewModel", "Error eliminando reina", e)
                onResult(Result.failure(e))
            }
        }
    }

}