package com.gonchimonchi.dragrace.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gonchimonchi.dragrace.Punto
import com.gonchimonchi.dragrace.R
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season


class RankingAdapter(
    private val reinas: Map<String, Reina>,
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

    private val keys = reinas.keys.toList()
    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.celdas.removeAllViews()

        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            // Crear encabezado dinámico
            val referencia = reinas.values.firstOrNull()
            referencia?.puntuaciones?.forEachIndexed { index, _ ->
                val celda = LayoutInflater.from(holder.itemView.context).inflate(R.layout.item_celda, holder.celdas, false) as TextView
                celda.text = "P${index + 1}"
                holder.celdas.addView(celda)
            }
            return
        }

        // Posición real en la lista
        val key = keys[position - 1]
        val reina = reinas[key] ?: return


        Glide.with(holder.itemView.context)
            .load(reina.imagen_url)  // la URL
            //.placeholder(R.drawable.ic_placeholder)  // imagen mientras carga (opcional)
            //.error(R.drawable.ic_error)              // imagen si falla (opcional)
            .into(holder.img)
        holder.name.text = reina.nombre

        holder.celdas.removeAllViews()
        reina.puntuaciones?.forEach { punto ->
            val celda = LayoutInflater.from(holder.itemView.context).inflate(R.layout.item_celda, holder.celdas, false) as TextView
            celda.text = punto.texto
            holder.celdas.addView(celda)
        }

        holder.puntuacion.text = String.format("%.3f", reina.puntuacionMedia ?: 0f)
    }

    override fun getItemCount() = reinas.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_NORMAL
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_NORMAL = 1
    }
}
