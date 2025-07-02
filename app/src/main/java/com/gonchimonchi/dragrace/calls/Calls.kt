package com.gonchimonchi.dragrace.calls

import androidx.annotation.OptIn
import com.google.firebase.firestore.FirebaseFirestore
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.gonchimonchi.dragrace.Punto
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season
import com.google.firebase.firestore.FieldValue
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
suspend fun getReinasByIds(ids: List<String>): List<Reina> = coroutineScope {
    val firestore = FirebaseFirestore.getInstance()
    try {
        val deferreds = ids.map { id ->
            async {
                val doc = firestore.collection("reina")
                    .document(id)
                    .get()
                    .await()
                Log.i("Firestore", "Reina con id encontrada $doc")
                doc.toObject(Reina::class.java)?.apply { this.id = id }
            }
        }
        deferreds.awaitAll().filterNotNull()
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener reinas", e)
        emptyList()
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
        val temporadaMap = data?.get(season) as? Map<String, List<String>>
        Log.i("FirestoreQuery", "🗂️ Submapa de temporada '$season': $temporadaMap")

        // Lista de strings de puntuaciones asociada a la reina
        val puntosReina = temporadaMap?.get(idReina) as? List<String>
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

@OptIn(UnstableApi::class)
fun addReina(
    nombre: String,
    imagenUrl: String,
    seasonName: String,
    onResult: (Result<String>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    val datosReina = hashMapOf(
        "nombre" to nombre,
        "imagen_url" to imagenUrl,
        "temporada" to seasonName
    )

    db.collection("reina")
        .add(datosReina)
        .addOnSuccessListener { documentReference ->
            val reinaId = documentReference.id
            Log.d("Firestore", "Nueva reina info[ID: $reinaId, Nombre: $nombre, Season: $seasonName]")
            // Llamada a actualizar temporada
            actualizarSeasonConReina(seasonName, reinaId) { resultActualizar ->
                if (resultActualizar.isSuccess) {
                    onResult(Result.success(reinaId))
                } else {
                    onResult(Result.failure(resultActualizar.exceptionOrNull() ?: Exception("Error desconocido")))
                }
            }
        }
        .addOnFailureListener { e ->
            onResult(Result.failure(e))
        }
}

@OptIn(UnstableApi::class)
fun deleteReina(
    reina: Reina
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("reina").document(reina.id.toString())
        .delete()
        .addOnSuccessListener {
            Log.d("Firestore", "Reina ${reina.nombre} eliminada")

            // Ahora eliminarla de la temporada
            deleteReinaDeSeason(reina)
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error al eliminar reina", e)
        }
}


@OptIn(UnstableApi::class)
fun actualizarSeasonConReina(
    seasonName: String,
    reinaId: String,
    onResult: (Result<Unit>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("season")
        .document(seasonName)
        .update("reinas", FieldValue.arrayUnion(reinaId))
        .addOnSuccessListener {
            Log.d("Firestore", "Reina añadida a la temporada $seasonName")
            onResult(Result.success(Unit))
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error al actualizar la temporada", e)
            onResult(Result.failure(e))
        }
}

@OptIn(UnstableApi::class)
fun deleteReinaDeSeason(
    reina: Reina
){
    val db = FirebaseFirestore.getInstance()
    db.collection("season")
        .document(reina.temporada.toString())
        .update("reinas", FieldValue.arrayRemove(reina.id))
        .addOnSuccessListener {
            Log.d("Firestore", "Reina ${reina.nombre} eliminada de temporada ${reina.temporada}")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error al actualizar la temporada", e)
        }
}

@OptIn(UnstableApi::class)
suspend fun getSeason(season: Season): List<Season> = coroutineScope {
    val firestore = FirebaseFirestore.getInstance()

    try {
        val result = firestore.collection("season")
            .document(season.id.toString())
            .get()
            .await()

        val franquicia = result.getString("franquicia")
        val nombre = result.getString("nombre")
        val year = result.getLong("year")?.toInt()
        val capitulos = result.get("capitulos") as? List<String>
        val listaIds = result.get("reinas") as? List<String> ?: emptyList()

        // Ahora carga las reinas con sus datos completos
        val reinas = getReinasByIds(listaIds)

        val seasonObj = Season(
            id = result.id,
            franquicia = franquicia,
            nombre = nombre,
            year = year,
            capitulos = capitulos,
            reinas = reinas as MutableList<Reina>? // Aquí sí puedes poner List<Reina>
        )
        Log.i("FirestoreQuery", "📘 Temporada recuperada: ${seasonObj}")
        listOf(seasonObj)

    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener temporada", e)
        emptyList()
    }
}

@OptIn(UnstableApi::class)
suspend fun getSeasonsPoblada(): List<Season> = coroutineScope {
    val firestore = FirebaseFirestore.getInstance()
    try {
        val result = firestore.collection("season")
            .orderBy("year", Query.Direction.DESCENDING)
            .get()
            .await()

        Log.i("FirestoreQuery", "📘 Temporadas disponibles: ${result.size()}")

        result.documents.mapNotNull { doc ->
            // Recuperar lista de IDs desde el documento
            val listaIds = doc.get("reinas") as? List<String> ?: emptyList()

            // Obtener reinas completas con puntuaciones
            val reinasPobladas = getReinasByIds(listaIds)

            // Crear objeto Season manualmente
            val season = Season(
                id = doc.id,
                franquicia = doc.getString("franquicia"),
                nombre = doc.getString("nombre"),
                year = doc.getLong("year")?.toInt(),
                capitulos = doc.get("capitulos") as? List<String>,
                reinas = reinasPobladas as MutableList<Reina>?
            )
            Log.i("FirestoreQuery", "📘 Season cargada: ${season}")
            season
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener temporadas", e)
        emptyList()
    }
}


@OptIn(UnstableApi::class)
suspend fun getSeasonsVacia(): List<Season> = coroutineScope {
    val firestore = FirebaseFirestore.getInstance()
    try {
        val result = firestore.collection("season")
            .orderBy("year", Query.Direction.DESCENDING)
            .get()
            .await()

        Log.i("FirestoreQuery", "📘 Temporadas disponibles: ${result.size()}")

        result.documents.mapNotNull { doc ->
            Season(
                id = doc.id,
                franquicia = doc.getString("franquicia"),
                nombre = doc.getString("nombre"),
                year = doc.getLong("year")?.toInt(),
                capitulos = doc.get("capitulos") as? List<String>,
                reinas = null // o puedes poner `emptyList()`
            )
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error al obtener temporadas", e)
        emptyList()
    }
}

@OptIn(UnstableApi::class)
fun actualizarRanking(
    reinas: MutableList<Reina>,
    season: Season,
    onResult: (Result<String>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val rankingTemporada = mutableMapOf<String, Any>()

    for (reina in reinas) {
        val listaTexto = reina.puntuaciones
            ?.filterNotNull()
            ?.mapNotNull { it.texto?.toString() } // Convierte a texto
            ?: emptyList()

        rankingTemporada[reina.id.toString()] = listaTexto
    }

    db.collection("ranking")
        .document("dragO'Nita")
        .update(mapOf(season.id to rankingTemporada))
        .addOnSuccessListener {
            Log.d("Firestore", "Ranking actualizado")
            onResult(Result.success("Ranking actualizado"))
        }
        .addOnFailureListener { e ->
            Log.d("Firestore", "Error al actualizar ranking: ${e.message}")
            onResult(Result.failure(e))
        }
}

@OptIn(UnstableApi::class)
fun updateCapituloTemporada(
    season: Season,
    onResult: (Result<String>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("season")
        .document(season.id?: return)
        .update(
            mapOf(
                "nombre" to season.nombre,
                "franquicia" to season.franquicia,
                "year" to season.year,
                "capitulos" to season.capitulos
            )
        )
        .addOnSuccessListener {
            Log.d("Firestore", "Season actualizado")
            onResult(Result.success("Season actualizado"))
        }
        .addOnFailureListener { e ->
            Log.d("Firestore", "Error al actualizar ranking: ${e.message}")
            onResult(Result.failure(e))
        }
}
