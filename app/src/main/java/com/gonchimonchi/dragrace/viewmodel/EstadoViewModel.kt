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

class EstadoViewModel (): ViewModel() {
    private val _listaPuntos = MutableLiveData<List<Punto>>()
    val listaPuntos: LiveData<List<Punto>> = _listaPuntos

    fun obtenerListaPuntos(){
        viewModelScope.launch {
            _listaPuntos.postValue(getPuntosName())
        }
    }

}