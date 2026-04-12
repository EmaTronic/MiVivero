package com.emanuel.mivivero.ui.ventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.TotalAlbum

class ResumenAdapter : RecyclerView.Adapter<ResumenAdapter.VH>() {

    private var lista: List<TotalAlbum> = emptyList()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTexto: TextView = view.findViewById(R.id.tvTexto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resumen, parent, false)
        return VH(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = lista[position]

        holder.tvTexto.text = "Álbum ${item.albumId} → $ ${item.totalGanado}"
    }

    fun submitList(nueva: List<TotalAlbum>) {
        lista = nueva
        notifyDataSetChanged()
    }
}