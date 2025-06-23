package com.gonchimonchi.dragrace.calls

import androidx.annotation.OptIn
import com.gonchimonchi.dragrace.viewmodel.Option
import com.google.firebase.firestore.FirebaseFirestore
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.gonchimonchi.dragrace.Punto
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@OptIn(UnstableApi::class)
suspend fun getPuntosName(): List<Punto> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val result = firestore.collection("puntos")
            .orderBy("valor", Query.Direction.DESCENDING)
            .get()
            .await()
        Log.d("FirestoreQuery", "Consulta exitosa, ${result.size()} documentos obtenidos")
        result.documents.map { doc ->
            Punto(
                texto = doc.id,
                valor = (doc.get("valor") as? Number)?.toFloat() ?: 0f
            )
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al cargar opciones", e)
        emptyList()
    }
}

@OptIn(UnstableApi::class)
suspend fun getSeasonData(seasonName: String): Season {
    val firestore = FirebaseFirestore.getInstance()
    val result = firestore.collection("season")
        .document(seasonName)
        .get()
        .await()

    if (!result.exists()) {
        throw IllegalStateException("La temporada '$seasonName' no existe en Firestore.")
    }

    return result.toObject(Season::class.java)
        ?: throw IllegalStateException("No se pudo convertir el documento a Season.")
}


@OptIn(UnstableApi::class)
suspend fun getReinasByIds(ids: List<String>): Map<String, Reina> = coroutineScope {
    val firestore = FirebaseFirestore.getInstance()
    try {
        val deferreds = ids.map { id ->
            async {
                val doc = firestore.collection("reina").document(id).get().await()
                val reina = doc.toObject(Reina::class.java)
                id to reina
            }
        }
        val listaPares = deferreds.awaitAll().filter { it.second != null }
        listaPares.associate { it.first to it.second!! }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener reinas", e)
        emptyMap()
    }
}

@OptIn(UnstableApi::class)
suspend fun getPuntosReina(season: String, idReina: String): List<Punto> = coroutineScope {
    val firestore = FirebaseFirestore.getInstance()
    try {
        val result = firestore.collection("ranking")
            .document("dragO'Nita")
            .get()
            .await()

        if (!result.exists()) {
            Log.w("FirestoreQuery", "❌ Documento de temporada 'dragO'Nita' no existe")
            return@coroutineScope emptyList()
        }

        val data = result.data
        Log.i("FirestoreQuery", "📄 Documento completo: $data")

        // Submapa de la temporada específica
        val temporadaMap = data?.get(season) as? Map<*, *>
        Log.i("FirestoreQuery", "🗂️ Submapa de temporada '$season': $temporadaMap")

        // Lista de strings de puntuaciones asociada a la reina
        val puntosReina = temporadaMap?.get(idReina) as? List<*>
        Log.i("FirestoreQuery", "✅ Lista cruda para reina '$idReina': $puntosReina")

        // Obtener categorías y valores desde Firestore
        val categorias = getPuntosName() // devuelve List<Option> con (texto, valor)
        Log.i("FirestoreQuery", "📘 Categorías disponibles: $categorias")

        // Combinar cada string con su valor
        val puntos = puntosReina?.mapNotNull { textoRaw ->
            val texto = textoRaw?.toString()?.lowercase()?.trim()
            val match = categorias.find { it.texto.lowercase().trim() == texto }
            match?.let {
                Punto(
                    texto = it.texto,
                    valor = it.valor ?: 0f // Asume que it.valor es Float?
                )
            }
        } ?: emptyList()

        Log.d("FirestoreQuery", "🎯 Lista final de objetos Punto: $puntos")
        puntos
    } catch (e: Exception) {
        Log.e("Firestore", "🚨 Error al cargar puntuaciones de $idReina en $season", e)
        emptyList()
    }
}

