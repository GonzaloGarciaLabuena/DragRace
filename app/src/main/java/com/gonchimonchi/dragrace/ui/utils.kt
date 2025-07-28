package com.gonchimonchi.dragrace.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.gonchimonchi.dragrace.classes.ColorPalette
import com.gonchimonchi.dragrace.classes.Season
import com.gonchimonchi.dragrace.classes.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class Utils {

    private val urlToJson = "https://rupaulsdragrace.fandom.com/api.php?" +
            "action=query&format=json&list=allimages&ailimit=1&" +
            "aiprefix="

    suspend fun generarColoresImagenUrl(
        context: Context,
        season: Season,
        onResult: (ColorPalette?) -> Unit
    ) {
        val url = obtenerUrlPromoSeason(season) ?: return onResult(null)
        Log.i("TEMPORADA" , "url $url")
        Glide.with(context)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource).generate { palette ->
                        palette?.let {
                            val colorPalette = ColorPalette(
                                dominante = it.getDominantColor(Color.GRAY).toHex(),
                                vibrante = it.getVibrantColor(Color.GRAY).toHex(),
                                suave = it.getLightMutedColor(Color.GRAY).toHex(),
                                oscuro = it.getDarkMutedColor(Color.GRAY).toHex(),
                                alternativo = it.getMutedColor(Color.GRAY).toHex()
                            )
                            onResult(colorPalette)
                        } ?: onResult(null)
                    }
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    onResult(null)
                }
            })
    }


    suspend fun obtenerUrlPromoSeason(season: Season): String? {
        val id = changeId(season.id?.trim().toString()) ?: return null
        val url = urlToJson + id
        Log.i("TEMPORADA" , "urlPromo $url")
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            val json = JSONObject(body)
            val images = json.getJSONObject("query").getJSONArray("allimages")
            if (images.length() > 0) {
                images.getJSONObject(0).getString("url")
            } else null
        }
    }

    private fun changeId(nombreTemporada: String): String? {
        val match = Regex("([A-Z]+)(\\d+)", RegexOption.IGNORE_CASE).find(nombreTemporada.trim())
        if (match == null || match.groupValues.size < 3) return null
        Log.i("TEMPORADA" , "franquicia $match")

        val franquicia = match.groupValues[1].uppercase()
        val numeroTemporada = match.groupValues[2]
        Log.i("TEMPORADA" , "franquicia $franquicia $numeroTemporada")
        return when (franquicia) {
            "S" -> "RDR$numeroTemporada"
            "AS" -> "RDRAS$numeroTemporada"
            "DRUK" -> "DRUK$numeroTemporada"
            "UKvsTW" -> "UKvsTW$numeroTemporada"
            "DRES" -> "DRES$numeroTemporada"
            "ESAS" -> "ESAS$numeroTemporada"
            "DRFR" -> "DRF$numeroTemporada"
            "DRFRAS" -> "DRFRAS$numeroTemporada"
            "DRMX" -> "DRMX$numeroTemporada"
            "DRDU" -> "DRDU$numeroTemporada"
            "GAS" -> "GAS$numeroTemporada"
            else -> null
        }
    }

    fun esColorOscuro(hex: String): Boolean {
        return try {
            val color = Color.parseColor(hex)
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)

            // Fórmula estándar de luminancia relativa
            val luminancia = (0.299 * r + 0.587 * g + 0.114 * b)
            luminancia < 186 // Umbral típico (186 o 128 según preferencia)
        } catch (e: Exception) {
            false // Por defecto considera que no es oscuro si falla el parseo
        }
    }

}