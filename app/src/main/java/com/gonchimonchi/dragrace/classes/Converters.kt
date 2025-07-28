package com.gonchimonchi.dragrace.calls

import com.gonchimonchi.dragrace.classes.ColorPalette
import com.gonchimonchi.dragrace.classes.Punto
import com.gonchimonchi.dragrace.classes.Reina
import com.gonchimonchi.dragrace.classes.Season
import com.google.firebase.firestore.DocumentSnapshot

fun Season.Companion.fromDocument(doc: DocumentSnapshot, reinas: MutableList<Reina>? = null): Season {
    val mapaColores = doc.get("paleta") as? Map<String, String>
    return Season(
        id = doc.id,
        franquicia = doc.getString("franquicia").orEmpty(),
        nombre = doc.getString("nombre").orEmpty(),
        year = doc.getLong("year")?.toInt() ?: 0,
        capitulos = doc.get("capitulos") as? List<String> ?: emptyList(),
        paleta = ColorPalette.fromMap(mapaColores),
        reinas = reinas ?: mutableListOf()
    )
}

fun ColorPalette.Companion.fromMap(map: Map<String, String>?): ColorPalette {
    return ColorPalette(
        dominante = map?.get("dominante") ?: "#000000",
        vibrante = map?.get("vibrante") ?: "#000000",
        suave = map?.get("suave") ?: "#000000",
        oscuro = map?.get("oscuro") ?: "#000000",
        alternativo = map?.get("alternativo") ?: "#000000"
    )
}

fun Reina.Companion.fromDocument(doc: DocumentSnapshot): Reina? {
    return doc.toObject(Reina::class.java)?.apply {
        id = doc.id
    }
}

fun Punto.Companion.fromDocument(doc: DocumentSnapshot): Punto {
    return Punto(
        texto = doc.id,
        valor = (doc.get("valor") as? Number)?.toFloat() ?: 0f
    )
}
