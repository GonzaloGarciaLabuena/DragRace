import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gonchimonchi.dragrace.Season

class TemporadaAdapter(
    private val temporadas: List<Season>,
    private val onClick: (Season) -> Unit
) : RecyclerView.Adapter<TemporadaAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTemporada: TextView = itemView.findViewById(android.R.id.text1)

        init {
            itemView.setOnClickListener {
                onClick(temporadas[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = temporadas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textTemporada.text = temporadas[position].nombre
    }
}
