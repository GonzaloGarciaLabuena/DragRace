import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gonchimonchi.dragrace.Punto
import com.gonchimonchi.dragrace.Season

class PuntosAdapter(
    private val listPuntos: List<Punto>,
    private val onClick: (Punto) -> Unit
) : RecyclerView.Adapter<PuntosAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textPunto: TextView = itemView.findViewById(android.R.id.text1)

        init {
            itemView.setOnClickListener {
                onClick(listPuntos[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listPuntos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textPunto.text = listPuntos[position].texto
    }
}
