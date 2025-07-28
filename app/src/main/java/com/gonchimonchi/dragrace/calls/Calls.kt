package com.gonchimonchi.dragrace.calls

import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.firebase.firestore.*
import com.gonchimonchi.dragrace.classes.ColorPalette
import com.gonchimonchi.dragrace.classes.Punto
import com.gonchimonchi.dragrace.classes.Reina
import com.gonchimonchi.dragrace.classes.Season
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/** --- CATEGORÍAS / PUNTOS --- */

@OptIn(UnstableApi::class)
suspend fun getPuntosName(): List<Punto> {
    return try {
        FirebaseFirestore.getInstance().collection("puntos")
            .orderBy("valor", Query.Direction.DESCENDING)
            .get().await()
            .documents.map { Punto.fromDocument(it) }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al cargar puntos", e)
        emptyList()
    }
}

@OptIn(UnstableApi::class)
suspend fun getPuntosReina(season: String, idReina: String): List<Punto> = coroutineScope {
    try {
        val doc = FirebaseFirestore.getInstance().collection("ranking")
            .document("dragO'Nita").get().await()

        val temporadaMap = doc.data?.get(season) as? Map<String, List<String>>
        val rawPuntos = temporadaMap?.get(idReina) ?: return@coroutineScope emptyList()

        val categorias = getPuntosName()
        rawPuntos.mapNotNull { textoRaw ->
            val texto = textoRaw.lowercase().trim()
            val match = categorias.find { it.texto.lowercase().trim() == texto }
            match?.let { Punto(it.texto, it.valor) }
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al cargar puntuaciones de $idReina", e)
        emptyList()
    }
}

/** --- REINAS --- */

@OptIn(UnstableApi::class)
suspend fun getReinasByIds(ids: List<String>): List<Reina> = coroutineScope {
    try {
        ids.map { id ->
            async {
                val doc = FirebaseFirestore.getInstance().collection("reina")
                    .document(id).get().await()
                Reina.fromDocument(doc)
            }
        }.awaitAll().filterNotNull()
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener reinas", e)
        emptyList()
    }
}

@OptIn(UnstableApi::class)
fun addReina(nombre: String, imagenUrl: String, seasonName: String, onResult: (Result<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val datosReina = hashMapOf("nombre" to nombre, "imagen_url" to imagenUrl, "temporada" to seasonName)

    db.collection("reina")
        .add(datosReina)
        .addOnSuccessListener { ref ->
            actualizarSeasonConReina(seasonName, ref.id) {
                if (it.isSuccess) onResult(Result.success(ref.id))
                else onResult(Result.failure(it.exceptionOrNull() ?: Exception("Error desconocido")))
            }
        }
        .addOnFailureListener { onResult(Result.failure(it)) }
}

@OptIn(UnstableApi::class)
fun deleteReina(reina: Reina) {
    FirebaseFirestore.getInstance().collection("reina").document(reina.id)
        .delete()
        .addOnSuccessListener {
            deleteReinaDeSeason(reina)
        }
        .addOnFailureListener { Log.e("Firestore", "Error al eliminar reina", it) }
}

/** --- TEMPORADAS --- */

@OptIn(UnstableApi::class)
suspend fun getSeasonData(seasonName: String): Season {
    val doc = FirebaseFirestore.getInstance().collection("season")
        .document(seasonName).get().await()
    if (!doc.exists()) throw IllegalStateException("La temporada '$seasonName' no existe")
    return Season.fromDocument(doc)
}

@OptIn(UnstableApi::class)
suspend fun getSeasonsPoblada(): List<Season> = coroutineScope {
    try {
        FirebaseFirestore.getInstance().collection("season")
            .orderBy("year", Query.Direction.DESCENDING).get().await()
            .documents.mapNotNull { doc ->
                val reinas = getReinasByIds(doc.get("reinas") as? List<String> ?: emptyList())
                Season.fromDocument(doc, reinas.toMutableList())
            }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener temporadas", e)
        emptyList()
    }
}

@OptIn(UnstableApi::class)
suspend fun getSeasonsVacia(): List<Season> = coroutineScope {
    try {
        FirebaseFirestore.getInstance().collection("season")
            .orderBy("year", Query.Direction.DESCENDING).get().await()
            .documents.mapNotNull { Season.fromDocument(it) }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener temporadas vacías", e)
        emptyList()
    }
}

@OptIn(UnstableApi::class)
suspend fun cargarPuntuacionesPorTemporadas(temporadas: List<Season>, categorias: List<Punto>): List<Season> = coroutineScope {
    temporadas.map { temporada ->
        try {
            val doc = FirebaseFirestore.getInstance().collection("ranking")
                .document(temporada.id).get().await()

            val temporadaMap = doc.data?.get(temporada.id) as? Map<String, List<String>>
            temporada.reinas.forEach { reina ->
                val puntosRaw = temporadaMap?.get(reina.id) ?: emptyList()
                reina.puntuaciones = puntosRaw.map { raw ->
                    val texto = raw.lowercase().trim()
                    val match = categorias.find { it.texto.lowercase().trim() == texto }
                    Punto(match?.texto ?: "", match?.valor ?: 0f)
                }.toMutableList()
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error cargando puntos de temporada ${temporada.id}", e)
        }
        temporada
    }
}

/** --- ACTUALIZACIONES --- */

@OptIn(UnstableApi::class)
fun actualizarSeasonConReina(seasonName: String, reinaId: String, onResult: (Result<Unit>) -> Unit) {
    FirebaseFirestore.getInstance().collection("season")
        .document(seasonName)
        .update("reinas", FieldValue.arrayUnion(reinaId))
        .addOnSuccessListener { onResult(Result.success(Unit)) }
        .addOnFailureListener { onResult(Result.failure(it)) }
}

@OptIn(UnstableApi::class)
fun deleteReinaDeSeason(reina: Reina) {
    FirebaseFirestore.getInstance().collection("season")
        .document(reina.temporada).update("reinas", FieldValue.arrayRemove(reina.id))
        .addOnSuccessListener {}
        .addOnFailureListener { Log.e("Firestore", "Error al actualizar temporada", it) }
}

@OptIn(UnstableApi::class)
fun actualizarPaletaTemporadaFB(seasonId: String, paleta: ColorPalette, onResult: (Result<Unit>) -> Unit) {
    FirebaseFirestore.getInstance().collection("season").document(seasonId)
        .update("paleta", paleta)
        .addOnSuccessListener { onResult(Result.success(Unit)) }
        .addOnFailureListener { onResult(Result.failure(it)) }
}

@OptIn(UnstableApi::class)
fun updateCapituloTemporada(season: Season, onResult: (Result<String>) -> Unit) {
    FirebaseFirestore.getInstance().collection("season")
        .document(season.id)
        .update(
            mapOf(
                "nombre" to season.nombre,
                "franquicia" to season.franquicia,
                "year" to season.year,
                "capitulos" to season.capitulos
            )
        )
        .addOnSuccessListener { onResult(Result.success("Season actualizado")) }
        .addOnFailureListener { onResult(Result.failure(it)) }
}

@OptIn(UnstableApi::class)
fun actualizarRanking(reinas: MutableList<Reina>, season: Season, onResult: (Result<String>) -> Unit) {
    val ranking = reinas.associate { it.id to it.puntuaciones.map { p -> p.texto } }

    FirebaseFirestore.getInstance().collection("ranking")
        .document("dragO'Nita")
        .update(mapOf(season.id to ranking))
        .addOnSuccessListener { onResult(Result.success("Ranking actualizado")) }
        .addOnFailureListener { onResult(Result.failure(it)) }
}

/** --- UTILIDADES EXTRA --- */

fun copiarDocumento(
    db: FirebaseFirestore,
    coleccion: String,
    idOriginal: String,
    idNuevo: String,
    onComplete: (Boolean, Exception?) -> Unit
) {
    val docOriginal = db.collection(coleccion).document(idOriginal)
    val docNuevo = db.collection(coleccion).document(idNuevo)

    docOriginal.get()
        .addOnSuccessListener { snap ->
            snap.data?.let {
                docNuevo.set(it)
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { e -> onComplete(false, e) }
            } ?: onComplete(false, Exception("Documento original sin datos"))
        }
        .addOnFailureListener { onComplete(false, it) }
}