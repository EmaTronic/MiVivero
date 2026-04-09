package com.emanuel.mivivero.ui.ventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.RankingPlanta

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.VH>() {

    private var lista: List<RankingPlanta> = emptyList()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return VH(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item = lista[position]

        holder.tvNombre.text = when (position) {
            0 -> "🥇 ${item.nombrePlanta}"
            1 -> "🥈 ${item.nombrePlanta}"
            2 -> "🥉 ${item.nombrePlanta}"
            else -> "${position + 1}. ${item.nombrePlanta}"
        }

        holder.tvCantidad.text = "${item.totalVendidas}"
    }

    fun submitList(nueva: List<RankingPlanta>) {
        lista = nueva
        notifyDataSetChanged()
    }
}