package com.gonchimonchi.dragrace.classes

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.gonchimonchi.dragrace.R
import com.google.firebase.firestore.DocumentSnapshot

data class Season(
    var id: String = "",
    var franquicia: String = "",
    val nombre: String = "",
    var reinas: MutableList<Reina> = mutableListOf(),
    val capitulos: List<String> = emptyList(),
    val year: Int = 0,
    val paleta: ColorPalette = ColorPalette()
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot, reinas: MutableList<Reina> = mutableListOf()): Season {
            val mapaColores = doc.get("paleta") as? Map<String, String>
            return Season(
                id = doc.id,
                franquicia = doc.getString("franquicia").orEmpty(),
                nombre = doc.getString("nombre").orEmpty(),
                year = doc.getLong("year")?.toInt() ?: 0,
                capitulos = doc.get("capitulos") as? List<String> ?: emptyList(),
                paleta = ColorPalette.fromMap(mapaColores),
                reinas = reinas
            )
        }
    }
}

data class Reina(
    var id: String = "",
    val nombre: String = "",
    val imagen_url: String = "",
    val temporada: String = "",
    var puntuacionMedia: Float = 0f,
    var puntuaciones: MutableList<Punto> = mutableListOf()
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Reina? {
            return doc.toObject(Reina::class.java)?.apply {
                id = doc.id
            }
        }
    }

    fun bindImg(imagen: ImageView, itemView: View) {
        Glide.with(itemView.context)
            .load(imagen_url)
            .placeholder(R.drawable.queen_not_found)
            .error(R.drawable.queen_error)
            .into(imagen)
    }
}

data class Punto(
    val texto: String = "",
    val valor: Float = 0f
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Punto {
            return Punto(
                texto = doc.id,
                valor = (doc.get("valor") as? Number)?.toFloat() ?: 0f
            )
        }
    }
}

data class ColorPalette(
    val dominante: String = "#000000",
    val vibrante: String = "#000000",
    val suave: String = "#000000",
    val oscuro: String = "#000000",
    val alternativo: String = "#000000"
) {
    companion object {
        fun fromMap(map: Map<String, String>?): ColorPalette {
            return ColorPalette(
                dominante = map?.get("dominante") ?: "#000000",
                vibrante = map?.get("vibrante") ?: "#000000",
                suave = map?.get("suave") ?: "#000000",
                oscuro = map?.get("oscuro") ?: "#000000",
                alternativo = map?.get("alternativo") ?: "#000000"
            )
        }
    }
}

fun Int.toHex(): String = String.format("#%06X", 0xFFFFFF and this)
