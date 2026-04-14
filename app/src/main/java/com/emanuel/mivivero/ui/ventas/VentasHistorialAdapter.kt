package com.emanuel.mivivero.ui.ventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.VentaHistorial

class VentasHistorialAdapter : RecyclerView.Adapter<VentasHistorialAdapter.VH>() {

    private var lista = listOf<VentaHistorial>()

    fun submitList(nueva: List<VentaHistorial>) {
        lista = nueva
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvDetalle: TextView = view.findViewById(R.id.tvDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_venta, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val v = lista[position]

        val nombre = "${v.familia} ${v.especie ?: ""}"
        val total = v.cantidad * v.precioUnitario

        holder.tvNombre.text = nombre
        holder.tvDetalle.text = "x${v.cantidad}  $${"%.0f".format(total)}"
    }

    override fun getItemCount() = lista.size
}