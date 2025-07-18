package com.gonchimonchi.dragrace.activity

import TemporadaAdapter
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gonchimonchi.dragrace.R
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.gonchimonchi.dragrace.viewmodel.DropboxViewModel
import com.gonchimonchi.dragrace.viewmodel.ReinaViewModel
import com.gonchimonchi.dragrace.viewmodel.TemporadaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gonchimonchi.dragrace.Season
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.FileOutputStream
import okhttp3.OkHttpClient
import okhttp3.Request

class AddReinaActivity : AppCompatActivity() {
    private lateinit var viewModelDropbox: DropboxViewModel

    private lateinit var editNombre: EditText
    private lateinit var editSeason: TextView
    private lateinit var temporadaSeleccionada: Season
    private lateinit var imgReina: ImageView
    private lateinit var fileImgDispositivo: File
    private lateinit var buttonSubirReina : Button

    private var imgSubida: Boolean? = null
    private var temporadasCargadas: List<Season> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reina)

        viewModelDropbox = ViewModelProvider(this)[DropboxViewModel::class.java]
        imgReina = findViewById(R.id.imgReina)
        imgReina.setOnClickListener{
            seleccionarImagenLauncher.launch("image/*")
        }
        editNombre = findViewById(R.id.editNombre)
        editSeason = findViewById(R.id.editSeason)
        buttonSubirReina = findViewById(R.id.buttonSubirReina)

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                verificarCampos()
                val nombre = editNombre.text.toString().trim()
                val temporada = editSeason.text.toString().trim()
                Log.i("ADD", "TextWatcher $nombre $temporada")

                if (nombre.isNotBlank() && temporada.isNotBlank()) {
                    Log.i("ADD", "TextWatcher $temporadaSeleccionada")
                    val seasonId = temporadaSeleccionada.id.toString().trim()
                    val nombreFormateado = nombre.replace(" ", "") + seasonId + "CastMug"
                    Log.i("ADD", "TextWatcher $nombreFormateado")
                    lifecycleScope.launch {
                        val url = obtenerUrlImagen(nombreFormateado)
                        if (url != null) {
                            val imgTemp = descargarImagen(url, this@AddReinaActivity)
                            if (imgTemp != null) {
                                fileImgDispositivo = imgTemp
                                imgSubida = true
                                Glide.with(this@AddReinaActivity)
                                    .load(url)
                                    .placeholder(R.drawable.queen_not_found)
                                    .error(R.drawable.queen_error)
                                    .into(imgReina)
                                verificarCampos()
                                Log.i("ADD", "Imagen autocompletada desde la Wiki.")
                            }
                        } else {
                            Log.w("ADD", "No se encontró imagen en la Wiki para $nombreFormateado")
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        editNombre.addTextChangedListener(watcher)
        editSeason.addTextChangedListener(watcher)

        val temporadaViewModel = ViewModelProvider(this)[TemporadaViewModel::class.java]
        temporadaViewModel.obtenerSeasonsVacia()
        temporadaViewModel.seasons.observe(this) { listaSeasons ->
            temporadasCargadas = listaSeasons
        }

        editSeason.setOnClickListener {
            if (temporadasCargadas.isNotEmpty()) {
                mostrarBottomSheetTemporadas(temporadasCargadas) { seleccionada ->
                    temporadaSeleccionada = seleccionada
                    editSeason.text = temporadaSeleccionada.nombre
                }
            } else {
                Toast.makeText(this, "Temporadas no cargadas", Toast.LENGTH_SHORT).show()
            }
        }

        buttonSubirReina.setOnClickListener {
            val nombre = editNombre.text.toString().trim()
            val season = editSeason.text.toString().trim()

            if(nombre.isEmpty()){
                Toast.makeText(this, "Por favor, introduce un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(season.isEmpty()){
                Toast.makeText(this, "Por favor, introduce una temporada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val seasonId = temporadaSeleccionada.id.toString().trim()
            val nombreFormateado = nombre.replace(" ", "") + seasonId + "CastMug"

            lifecycleScope.launch {
                val fileParaSubir = fileImgDispositivo
                if (fileParaSubir == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddReinaActivity, "No se pudo obtener la imagen", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val link = viewModelDropbox.subirImagen(fileParaSubir, temporadaSeleccionada.id.toString(), nombreFormateado)
                if (link != null) {
                    addNuevaReina(nombre, temporadaSeleccionada, link)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddReinaActivity, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    fun verificarCampos() {
        val todosLlenos = editNombre.text.toString().isNotBlank() &&
                editSeason.text.toString().isNotBlank() &&
                imgSubida == true
        buttonSubirReina.isEnabled = todosLlenos
    }

    // Selector de imágenes
    private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val localFile = uriToFile(uri)
            if (localFile != null) {
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.queen_not_found)
                    .error(R.drawable.queen_error)
                    .into(imgReina) // img es tu ImageView
                imgSubida = true
                fileImgDispositivo = localFile
                Toast.makeText(this, "Imágen cargada", Toast.LENGTH_SHORT).show()
                verificarCampos()
            } else {
                Toast.makeText(this, "No se pudo convertir la imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    //Sube a DropBox la img de la reina
    suspend fun addImgDropbox(nombre: String, season: Season): String? {
        Log.i("DRAGRACE", "File recogido: $fileImgDispositivo")
        val link = viewModelDropbox.subirImagen(fileImgDispositivo, season.id.toString(), nombre)
        return if (link != null) {
            Log.i("DRAGRACE", "Imagen subida con éxito: $link")
            link
        } else {
            Log.e("DRAGRACE", "Fallo al subir la imagen")
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddReinaActivity, "Error al subir imagen", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }

    //Añade la reina a Firebase con su nombre y el link de su img en DropBox. También actualiza la
    //reina en su temporada
    private fun addNuevaReina(nombre: String, season: Season, linkImg: String) {
        val viewModelReina = ViewModelProvider(this)[ReinaViewModel::class.java]
        viewModelReina.guardarReina(nombre, linkImg, season.id.toString())

        viewModelReina.addReinaStatus.observe(this) { resultado ->
            viewModelReina.addReinaStatus.removeObservers(this)
            if (resultado.isSuccess) {
                val reinaId = resultado.getOrNull()
                Toast.makeText(this, "Reina guardada con éxito: $reinaId", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val error = resultado.exceptionOrNull()
                Log.e("DRAGRACE", "Fallo al añadir reina ${error?.message}.")
                Toast.makeText(this, "Error al guardar reina: ${error?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun mostrarBottomSheetTemporadas(temporadas: List<Season>, onSeleccion: (Season) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_temporadas, null)
        dialog.setContentView(view)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerTemporadas)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = TemporadaAdapter(temporadas) {
            onSeleccion(it)
            dialog.dismiss()
        }

        dialog.show()
    }

    suspend fun obtenerUrlImagen(nombreArchivo: String): String? {
        val url = "https://rupaulsdragrace.fandom.com/api.php?" +
                "action=query&format=json&list=allimages&ailimit=1&" +
                "aiprefix=$nombreArchivo"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            val json = JSONObject(body)
            val images = json.getJSONObject("query").getJSONArray("allimages")
            if (images.length() > 0) {
                images.getJSONObject(0).getString("url")
            } else null
        }
    }

    suspend fun descargarImagen(url: String, context: Context): File? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val inputStream = response.body?.byteStream() ?: return@withContext null

            val tempFile = File(context.cacheDir, "imagen.jpg")
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("imagen", ".jpg", cacheDir)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}