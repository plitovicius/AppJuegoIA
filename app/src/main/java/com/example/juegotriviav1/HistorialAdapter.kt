package com.example.juegotriviav1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.juegotriviav1.data.ScoreRecord

/**
 * Adaptador para mostrar la lista completa de partidas en el RecyclerView de HistorialActivity.
 */
class HistorialAdapter(private val items: List<ScoreRecord>) :
    RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPartida: TextView = view.findViewById(R.id.tvItemPartida)
        val tvDetalles: TextView = view.findViewById(R.id.tvItemDetalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val record = items[position]
        val partidaNum = items.size - position
        holder.tvPartida.text = "Partida #$partidaNum — Puntaje: ${record.score}/5"
        holder.tvDetalles.text = "⏱️ Tiempo: ${record.timeSeconds}s  |  📅 ${record.dateTime}"
    }

    override fun getItemCount(): Int = items.size
}
