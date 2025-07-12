package com.gonchimonchi.dragrace

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gonchimonchi.dragrace.R

data class Season(
    var id: String? = null,
    var franquicia: String? = null,
    val nombre: String? = null,
    var reinas: MutableList<Reina>? = mutableListOf(),
    val capitulos: List<String>? = null,
    val year: Int? = null,
    val paleta: ColorPalette? = null,
)

data class Reina(
    var id: String? = null,
    val nombre: String? = null,
    val imagen_url: String? = null,
    val temporada: String? = null,
    var puntuacionMedia: Float? = null,
    var puntuaciones: MutableList<Punto?>? = mutableListOf(),
) {
    fun bindImg(imagen: ImageView, itemView: View) {
        Glide.with(itemView.context)
            .load(imagen_url)
            .placeholder(R.drawable.queen_not_found)
            .error(R.drawable.queen_error)
            .into(imagen)
    }
}

data class Punto(
    val texto: String,
    val valor: Float
)

data class ColorPalette(
    val dominante: String,
    val vibrante: String,
    val suave: String,
    val oscuro: String,
    val alternativo: String
)
fun Int.toHex(): String = String.format("#%06X", 0xFFFFFF and this)
