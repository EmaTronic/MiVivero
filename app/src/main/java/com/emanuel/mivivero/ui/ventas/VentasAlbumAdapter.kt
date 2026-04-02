package com.emanuel.mivivero.ui.ventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.AlbumResumen

class VentasAlbumAdapter(
    private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<VentasAlbumAdapter.VH>() {

    private var lista: List<AlbumResumen> = emptyList()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album_resumen, parent, false)
        return VH(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item = lista[position]

        holder.tvNombre.text = item.nombre
        holder.tvTotal.text = "$ ${item.totalGanado}"
        holder.tvCantidad.text = "Vendidas: ${item.totalVendidas}"

        holder.itemView.setOnClickListener {
            onClick(item.albumId)
        }
    }

    fun submitList(nueva: List<AlbumResumen>) {
        lista = nueva
        notifyDataSetChanged()
    }
}