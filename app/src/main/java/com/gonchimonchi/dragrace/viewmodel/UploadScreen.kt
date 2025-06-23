package com.gonchimonchi.dragrace.viewmodel

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonchimonchi.dragrace.viewmodel.DropboxViewModel
import java.io.File
import android.provider.OpenableColumns
import android.content.Context
import android.database.Cursor
import androidx.compose.runtime.livedata.observeAsState
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun UploadScreen(
    viewModel: DropboxViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onUploadDone: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var season by remember { mutableStateOf("usa15") } // Puedes cambiar dinámicamente si quieres
    val uploadResult by viewModel.uploadResult.observeAsState()

    val launcher = rememberLauncherForActivityResult(contract = GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFilePath = getFilePathFromUri(context, it)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Selecciona imagen para subir a Dropbox", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "Seleccionar imagen")
        }

        Spacer(modifier = Modifier.height(12.dp))

        selectedFilePath?.let {
            Text("Archivo seleccionado: $it")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                selectedFilePath?.let { path ->
                    viewModel.subirImagen(path, season)
                }
            },
            enabled = selectedFilePath != null
        ) {
            Text("Subir imagen")
        }

        Spacer(modifier = Modifier.height(20.dp))

        uploadResult?.let { result ->
            if (result.startsWith("http")) {
                Text("Subida correcta! Link: $result")
                // Volver después de mostrar resultado un momento
                LaunchedEffect(result) {
                    // Puedes hacer delay o ir atrás inmediatamente
                    onUploadDone()
                }
            } else {
                Text("Error o resultado: $result")
            }
        }
    }
}

fun getFilePathFromUri(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val fileName = queryFileName(context, uri) ?: "temp_file"
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            tempFile.absolutePath
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun queryFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        }
    }
    return name
}
