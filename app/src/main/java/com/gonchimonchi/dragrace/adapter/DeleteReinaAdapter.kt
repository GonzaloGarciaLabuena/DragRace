package com.gonchimonchi.dragrace.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gonchimonchi.dragrace.Punto
import com.gonchimonchi.dragrace.R
import com.gonchimonchi.dragrace.Reina
import com.gonchimonchi.dragrace.Season
import com.gonchimonchi.dragrace.calls.deleteReina
import com.gonchimonchi.dragrace.viewmodel.ReinaViewModel
import com.gonchimonchi.dragrace.viewmodel.TemporadaViewModel

class DeleteReinaAdapter(
    private val reinas: List<Reina>,
    private val onDeleteClick: (Reina) -> Unit
) : RecyclerView.Adapter<DeleteReinaAdapter.DeleteReinaViewHolder>() {

    inner class DeleteReinaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.img)
        val name: TextView = view.findViewById(R.id.name)
        val btnBorrar: ImageView = view.findViewById(R.id.btnBorrar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeleteReinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fila_delete_reina, parent, false)
        return DeleteReinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeleteReinaViewHolder, position: Int) {

        val reina = reinas[position]

        Glide.with(holder.itemView.context)
            .load(reina.imagen_url)  // la URL
            //.placeholder(R.drawable.ic_placeholder)  // imagen mientras carga (opcional)
            .error(R.drawable.queen_not_found)              // imagen si falla (opcional)
            .into(holder.img)
        holder.name.text = reina.nombre

        holder.btnBorrar.setOnClickListener {
            onDeleteClick(reina)
        }
    }

    override fun getItemCount() = reinas.size
}
