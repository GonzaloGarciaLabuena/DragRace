package com.gonchimonchi.dragrace.calls

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import java.io.FileInputStream
import java.io.InputStream
import com.gonchimonchi.dragrace.BuildConfig
import java.io.File
import android.util.Log
import java.util.Locale

class DropboxHelper() {

    private val APP_KEY = BuildConfig.APPKEY
    private val APP_SECRET = BuildConfig.APPSECRET
    private val REFRESH_TOKEN = BuildConfig.DROPBOX_REFRESH_TOKEN

    // Configuración Dropbox
    private val requestConfig = DbxRequestConfig.newBuilder("DragRaceApp/1.0").build()

    // Credenciales con refresh token (SDK renovará tokens automáticamente)
    val credential = DbxCredential(
        "",                    // accessToken vacío (la SDK lo renovará)
        -1L,                   // expiresAt en -1 para que lo ignore
        REFRESH_TOKEN,    // refreshToken real que obtuviste
        APP_KEY,          // App Key (client_id)
        APP_SECRET        // App Secret (client_secret)
    )

    // Cliente Dropbox configurado con refresh token
    private val dbxClient: DbxClientV2

    init {
        val config = DbxRequestConfig.newBuilder("DragRace").build()
        dbxClient = DbxClientV2(config, credential)
    }

    fun getClient(): DbxClientV2 {
        val config = DbxRequestConfig.newBuilder("DragRaceApp")
            .withUserLocaleFrom(Locale.getDefault())
            .build()
        return DbxClientV2(config, credential)
    }

    /**
     * Sube un archivo local a Dropbox y devuelve el enlace directo (raw) o null si falla.
     * @param localFilePath Ruta local del archivo en el dispositivo.
     * @param dropboxPath Ruta donde se guardará en Dropbox, por ejemplo "/queens/imagen.jpg"
     */
    fun uploadFileAndGetDirectLink(file: File, dropboxPath: String): String? {
        return try {
            FileInputStream(file).use { inputStream ->
                dbxClient.files()
                    .uploadBuilder(dropboxPath)
                    .uploadAndFinish(inputStream)
            }
            Log.i("DRAGRACE", "Archivo subido correctamente: $dropboxPath")

            val sharedLink = dbxClient.sharing().createSharedLinkWithSettings(dropboxPath)
            Log.i("DRAGRACE", "Enlace compartido creado: ${sharedLink.url}")

            if (sharedLink.url == null) {
                Log.e("DRAGRACE", "El URL del enlace compartido es null")
            }

            // Reemplazar para enlace directo
            val directLink = sharedLink.url?.replace("&dl=0", "&raw=1")
            Log.i("DRAGRACE", "Enlace directo generado: $directLink")

            directLink

        } catch (e: Exception) {
            Log.e("DRAGRACE", "Error al subir y crear enlace: ${e.message}", e)
            null
        }
    }

    fun deleteFile(path: String): Boolean {
        return try {
            val client = getClient()  // Asegúrate de tener esta función que retorna el cliente de Dropbox
            client.files().deleteV2(path)
            true
        } catch (e: Exception) {
            Log.e("DRAGRACE", "Error al eliminar archivo de Dropbox: ${e.message}", e)
            false
        }
    }

}
