package com.gonchimonchi.dragrace.calls

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import java.io.FileInputStream
import java.io.InputStream
import com.gonchimonchi.dragrace.BuildConfig
import java.io.File

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

    /**
     * Sube un archivo local a Dropbox y devuelve el enlace directo (raw) o null si falla.
     * @param localFilePath Ruta local del archivo en el dispositivo.
     * @param dropboxPath Ruta donde se guardará en Dropbox, por ejemplo "/queens/imagen.jpg"
     */
    fun uploadFileAndGetDirectLink(localFilePath: String, dropboxPath: String): String? {
        return try {
            val localFile = File(localFilePath)
            FileInputStream(localFile).use { inputStream ->
                dbxClient.files()
                    .uploadBuilder(dropboxPath)
                    .uploadAndFinish(inputStream)
            }

            // Crear enlace compartido
            val sharedLink = dbxClient.sharing()
                .createSharedLinkWithSettings(dropboxPath)

            // Convertir a enlace directo
            sharedLink.url.replace("?dl=0", "?raw=1")

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
