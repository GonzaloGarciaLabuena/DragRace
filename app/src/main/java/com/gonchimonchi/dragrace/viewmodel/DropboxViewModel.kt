package com.gonchimonchi.dragrace.viewmodel


import com.gonchimonchi.dragrace.calls.DropboxHelper
import kotlinx.coroutines.Dispatchers
import java.io.File
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.withContext
import kotlin.String

class DropboxViewModel() : ViewModel() {
    private val dropboxHelper = DropboxHelper()

    suspend fun subirImagen(file: File, season: String, nombre: String): String? {
        Log.i("DRAGRACE", "Dropboxviewmodel: subirImagen")
        return withContext(Dispatchers.IO) {
            try {
                val nuevoNombre = nombre.replace("\\s+".toRegex(), "_") + "_${season}.jpg"
                Log.i("DRAGRACE", "Dropboxviewmodel: file encontrado ${file}")
                val dropboxPath = "/DragRaceImages/${season}/${nuevoNombre}"
                Log.i("DRAGRACE", "Dropboxviewmodel: dropbox path ${dropboxPath}")
                dropboxHelper.uploadFileAndGetDirectLink(file, dropboxPath)
            } catch (e: Exception) {
                Log.e("DRAGRACE", "Error al subir y crear enlace: ${e.message}", e)
                null
            }
        }
    }

    suspend fun eliminarImagen(nombre: String, season: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val nombreFormateado = nombre.replace("\\s+".toRegex(), "_") + "_${season}.jpg"
                val dropboxPath = "/DragRaceImages/$season/$nombreFormateado"
                Log.i("DropboxDelete", "Intentando eliminar: $dropboxPath")
                dropboxHelper.deleteFile(dropboxPath)
            } catch (e: Exception) {
                Log.e("DRAGRACE", "Error al eliminar imagen: ${e.message}", e)
                false
            }
        }
    }
}
