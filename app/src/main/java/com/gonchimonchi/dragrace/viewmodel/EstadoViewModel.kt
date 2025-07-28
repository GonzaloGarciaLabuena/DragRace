package com.gonchimonchi.dragrace.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.gonchimonchi.dragrace.calls.*

import androidx.lifecycle.viewModelScope
import com.gonchimonchi.dragrace.classes.Punto
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