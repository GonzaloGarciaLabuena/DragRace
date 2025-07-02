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
    private val onDeleteClick: (Int) -> Unit,
    private val onAddClick: () -> Unit
) : RecyclerView.Adapter<GestionarCapituloAdapter.BaseViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CAPITULO = 0
        private const val VIEW_TYPE_BOTON = 1
    }

    open inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class CapituloViewHolder(view: View) : BaseViewHolder(view) {
        val editCapitulo: EditText = view.findViewById(R.id.editCapitulo)
        val btnSave: ImageButton = view.findViewById(R.id.btnSave)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    inner class BotonViewHolder(view: View) : BaseViewHolder(view) {
        val btnAdd: ImageButton = view.findViewById(R.id.btnAddCapitulo)
    }

    override fun getItemCount() = capitulos.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == capitulos.size) VIEW_TYPE_BOTON else VIEW_TYPE_CAPITULO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == VIEW_TYPE_CAPITULO) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fila_capitulo, parent, false)
            CapituloViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_capitulo, parent, false)
            BotonViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is CapituloViewHolder) {
            val capitulo = capitulos[position]
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
        } else if (holder is BotonViewHolder) {
            holder.btnAdd.setOnClickListener {
                onAddClick()
            }
        }
    }
}
