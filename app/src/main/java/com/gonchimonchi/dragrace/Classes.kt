package com.gonchimonchi.dragrace

data class Season(
    var franquicia: String? = null,
    val nombre: String? = null,
    val reinas: List<String>? = null,
    val capitulos: List<String>? = null
)

data class Reina(
    val nombre: String? = null,
    val imagen_url: String? = null,
    var puntuacionMedia: Float? = null,
    val puntuaciones: List<Punto>? = null
)

data class Punto(
    val texto: String,
    val valor: Float
)