package com.gonchimonchi.dragrace.calls

import androidx.annotation.OptIn
import com.gonchimonchi.dragrace.viewmodel.Option
import com.google.firebase.firestore.FirebaseFirestore
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.gonchimonchi.dragrace.viewmodel.Reina
import com.gonchimonchi.dragrace.viewmodel.Season
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@OptIn(UnstableApi::class)
suspend fun getPuntosName(): List<Option> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val result = firestore.collection("puntos")
            .orderBy("valor", Query.Direction.DESCENDING)
            .get()
            .await()
        Log.d("FirestoreQuery", "Consulta exitosa, ${result.size()} documentos obtenidos")
        result.documents.map { doc ->
            Option(
                id = doc.id,
                value = doc.getString("value").orEmpty()
            )
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al cargar opciones", e)
        emptyList()
    }
}

@OptIn(UnstableApi::class)
suspend fun getSeasonData(seasonName: String): Season? {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val result = firestore.collection("season")
            .document(seasonName)
            .get()
            .await()
        if (result.exists()) {
            val season = result.toObject(Season::class.java)
            if (season != null) {
                Log.d("FirestoreQuery", "Documento obtenido: ${season.nombre}")
            }
            season
        } else {
            Log.w("FirestoreQuery", "No se encontró el documento usa15")
            null
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al cargar el documento ${seasonName}", e)
        null
    }
}

@OptIn(UnstableApi::class)
suspend fun getReinasByIds(ids: List<String>): List<Reina> = coroutineScope {
    val firestore = FirebaseFirestore.getInstance()
    try {
        val deferreds = ids.map { id ->
            async {
                val doc = firestore.collection("reina").document(id).get().await()
                val reina = doc.toObject(Reina::class.java)
                reina // esta es la última expresión, será el valor devuelto
            }
        }
        deferreds.awaitAll().filterNotNull()
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener reinas", e)
        emptyList()
    }
}