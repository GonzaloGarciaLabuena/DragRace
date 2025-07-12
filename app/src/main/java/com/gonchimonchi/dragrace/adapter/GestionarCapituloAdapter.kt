package com.gonchimonchi.dragrace.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.media3.common.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.gonchimonchi.dragrace.R

class GestionarCapituloAdapter(
    private val capitulos: MutableList<String>,
    private val onSaveClick: (position: Int, nuevoTexto: String) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<GestionarCapituloAdapter.CapituloViewHolder>() {

    inner class CapituloViewHolder(view: View) :  RecyclerView.ViewHolder(view) {
        val editCapitulo: EditText = view.findViewById(R.id.editCapitulo)
        val btnSave: ImageButton = view.findViewById(R.id.btnSave)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val numeroCapitulo: TextView = view.findViewById(R.id.numeroCapitulo)
    }

    override fun getItemCount() = capitulos.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapituloViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fila_capitulo, parent, false)
        return CapituloViewHolder(view)
    }

    override fun onBindViewHolder(holder: CapituloViewHolder, position: Int) {
        val capitulo = capitulos[position]
        holder.numeroCapitulo.text = (position + 1).toString()

        holder.editCapitulo.setText(capitulo)

        holder.btnSave.setOnClickListener {
            val nuevoTexto = holder.editCapitulo.text.toString()
            onSaveClick(position, nuevoTexto)
            notifyItemChanged(position)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(position)
            notifyItemRemoved(position)
        }
        holder.editCapitulo.requestFocus()
    }
}
