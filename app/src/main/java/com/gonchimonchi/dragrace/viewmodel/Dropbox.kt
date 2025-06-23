package com.gonchimonchi.dragrace.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gonchimonchi.dragrace.calls.DropboxHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import android.util.Log

class DropboxViewModel(application: Application) : AndroidViewModel(application) {
    private val _uploadResult = MutableLiveData<String?>()
    val uploadResult: LiveData<String?> = _uploadResult
    private val dropboxHelper = DropboxHelper()

    fun subirImagen(localFilePath: String, season: String) {
        Log.i("DRAGRACE", "Dropboxviewmodel: subirImagen")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(localFilePath)
                Log.i("DRAGRACE", "Dropboxviewmodel: file encontrado ${file}")
                val dropboxPath = "/DragRaceImages/${season}/${file.name}"
                Log.i("DRAGRACE", "Dropboxviewmodel: dropbox path ${dropboxPath}")
                val link = dropboxHelper.uploadFileAndGetDirectLink(localFilePath, dropboxPath)
                Log.i("DRAGRACE", "Dropboxviewmodel: link ${link}")
                _uploadResult.postValue(link)
            } catch (e: Exception) {
                Log.e("DRAGRACE", "Dropboxviewmodel: Error al subir imagen: ${e.message}", e)
                _uploadResult.postValue(null) // o algún valor de error si usas un wrapper
            }
        }
    }
}
