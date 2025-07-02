package com.gonchimonchi.dragrace

data class Season(
    var id: String? = null,
    var franquicia: String? = null,
    val nombre: String? = null,
    var reinas: MutableList<Reina>? = mutableListOf(),
    val capitulos: List<String>? = null,
    val year: Int? = null
)

data class Reina(
    var id: String? = null,
    val nombre: String? = null,
    val imagen_url: String? = null,
    val temporada: String? = null,
    var puntuacionMedia: Float? = null,
    var puntuaciones: MutableList<Punto?>? = mutableListOf()
)

data class Punto(
    val texto: String,
    val valor: Float
)