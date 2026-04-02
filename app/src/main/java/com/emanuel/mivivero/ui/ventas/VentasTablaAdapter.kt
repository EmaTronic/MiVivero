package com.emanuel.mivivero.ui.ventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.VentaDetalle

class VentasTablaAdapter :
    RecyclerView.Adapter<VentasTablaAdapter.VH>() {

    private var lista: List<VentaDetalle> = emptyList()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)

            .inflate(R.layout.item_venta_tabla, parent, false)
        return VH(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item = lista[position]

        holder.tvNombre.text = "${item.nombrePlanta} - $position"
        holder.tvCantidad.text = "x${item.cantidad}"

        holder.tvFecha.text = android.text.format.DateFormat
            .format("dd/MM/yyyy", item.fecha)

        holder.tvTotal.text = "$ ${item.total}"
    }

    fun submitList(nueva: List<VentaDetalle>) {
        lista = nueva.toList() // 🔴 copia real
        notifyDataSetChanged()
    }
}