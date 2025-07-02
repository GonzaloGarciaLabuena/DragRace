package com.gonchimonchi.dragrace.adapter

import PuntosAdapter
import TemporadaAdapter
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gonchimonchi.dragrace.Punto
import com.gonchimonchi.dragrace.R
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog


class RankingAdapter(
    private var reinas: MutableList<Reina>,
    private val season: Season,
    private val listaPuntos: List<Punto>
) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    inner class RankingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.img)
        val name: TextView = view.findViewById(R.id.name)
        val puntuacion: TextView = view.findViewById(R.id.puntuacion)
        val celdas: LinearLayout = view.findViewById(R.id.dynamicCellsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fila, parent, false)
        return RankingViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.celdas.removeAllViews()

        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            holder.name.text = ""
            holder.puntuacion.text = ""
            season.capitulos?.forEach { capitulo ->
                Log.i("AdapterRanking", "Capitulo $capitulo")
                inflarCeldaHeader(holder, capitulo)
            }
            return
        }

        val reina = reinas[position - 1]
        Log.i("AdapterRanking", "Reina ${reina.nombre}")
        Glide.with(holder.itemView.context)
            .load(reina.imagen_url)
            .error(R.drawable.queen_not_found)
            .into(holder.img)

        holder.name.text = reina.nombre
        holder.puntuacion.text = String.format("%.3f", reina.puntuacionMedia ?: 0f)

        season.capitulos?.forEachIndexed  { index, _ ->
            val texto = reina.puntuaciones?.getOrNull(index)?.texto ?: ""
            Log.i("AdapterRanking", "Celda $index: $texto")
            inflarCelda(holder, texto, index, position)
        }
    }

    override fun getItemCount(): Int = reinas.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_NORMAL
    }

    private fun inflarCeldaHeader(holder: RankingViewHolder, text: String){
        val celda = LayoutInflater.from(holder.itemView.context)
            .inflate(R.layout.item_celda, holder.celdas, false) as TextView

        celda.text = text
        holder.celdas.addView(celda)
    }

    @OptIn(UnstableApi::class)
    private fun inflarCelda(holder: RankingViewHolder, text: String, capIndex: Int, position: Int) {
        val inflater = LayoutInflater.from(holder.itemView.context)
        val celda = inflater.inflate(R.layout.item_punto, holder.celdas, false) as TextView
        celda.text = text
        celda.setBackgroundColor(colorParaTexto(text))
        celda.setOnClickListener {
            try {
                if (listaPuntos.isNotEmpty()) {
                    mostrarBottomSheetPuntos(listaPuntos, holder.itemView.context) { seleccionada ->
                        celda.text = seleccionada.texto
                        celda.setBackgroundColor(colorParaTexto(seleccionada.texto))
                        val reinaActual = reinas[position - 1]

                        if (reinaActual.puntuaciones == null) {
                            reinaActual.puntuaciones = mutableListOf()
                        }
                        val puntos = reinaActual.puntuaciones!!

                        while (puntos.size <= capIndex) {
                            puntos.add(null)
                        }

                        puntos[capIndex] = seleccionada

                        val valores = puntos.filterNotNull().mapNotNull { it.valor }
                        reinaActual.puntuacionMedia = if (valores.isNotEmpty()) valores.average().toFloat() else 0f

                        // Reordenar la lista de reinas
                        reinas = reinas.sortedByDescending { it.puntuacionMedia }.toMutableList()

                        notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Log.e("RankingAdapter", "Error en setOnClickListener: ", e)
                Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        celda.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Eliminar puntuación")
                .setMessage("¿Seguro que deseas borrar esta puntuación?")
                .setPositiveButton("Eliminar") { _, _ ->
                    val reinaActual = reinas[position - 1]
                    val puntos = reinaActual.puntuaciones

                    if (puntos != null && capIndex < puntos.size) {
                        puntos[capIndex] = null // o removeAt(capIndex), según tu lógica
                    }

                    // Recalcular media
                    val valores = puntos?.filterNotNull()?.mapNotNull { it.valor } ?: emptyList()
                    reinaActual.puntuacionMedia = if (valores.isNotEmpty()) valores.average().toFloat() else 0f

                    // Reordenar
                    reinas = reinas.sortedByDescending { it.puntuacionMedia }.toMutableList()

                    notifyDataSetChanged()
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }
        holder.celdas.addView(celda)
    }

    private fun mostrarBottomSheetPuntos(
        listPuntos: List<Punto>,
        context: Context,
        onSeleccion: (Punto) -> Unit
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_puntos, null)
        dialog.setContentView(view)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerPuntos)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = PuntosAdapter(listPuntos) {
            onSeleccion(it)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun colorParaTexto(text: String): Int {
        return when (text.uppercase()) {
            "WIN"   -> "#FFD700".toColorInt() // Dorado
            "TOP2"  -> "#41cf67".toColorInt()
            "HIGH"  -> "#34a853".toColorInt() // Verde Oscuro
            "SAFE"  -> "#93c47d".toColorInt() // Verde claro
            "LOW"   -> "#ff6d01".toColorInt() // Naranja
            "BTM"   -> "#e06666".toColorInt() // Rojo claro
            "ELIM"  -> "#d90000".toColorInt() // Rojo oscuro
            "WINNER"-> "#ffff00".toColorInt()
            else    -> Color.TRANSPARENT                 // Por defecto
        }
    }


    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_NORMAL = 1
    }
}

