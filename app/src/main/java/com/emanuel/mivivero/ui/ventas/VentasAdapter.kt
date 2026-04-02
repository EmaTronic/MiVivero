package com.emanuel.mivivero.ui.ventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.PlantaAlbum

class VentasAdapter(
    private val onTotalChanged: (Double) -> Unit
) : RecyclerView.Adapter<VentasAdapter.VH>() {

    private var lista: List<PlantaAlbum> = emptyList()
    private val ventasMap = mutableMapOf<Long, Int>()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val etVendida: EditText = view.findViewById(R.id.etVendida)
        val tvGanancia: TextView = view.findViewById(R.id.tvGanancia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta_planta, parent, false)
        return VH(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = lista[position]

        holder.tvNombre.text = item.nombreCompleto
        holder.tvStock.text = "Stock: ${item.cantidad}"
        holder.tvPrecio.text = "$ ${item.precio}"

        holder.etVendida.setText(
            ventasMap[item.plantaId]?.toString() ?: ""
        )

        holder.etVendida.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {

                val vendida = holder.etVendida.text.toString().toIntOrNull() ?: 0

                val validada = if (vendida > item.cantidad) item.cantidad else vendida

                ventasMap[item.plantaId] = validada

                val ganancia = validada * item.precio
                holder.tvGanancia.text = "$ $ganancia"

                recalcularTotal()
            }
        }
    }

    fun submitList(nueva: List<PlantaAlbum>) {
        lista = nueva
        notifyDataSetChanged()
    }

    private fun recalcularTotal() {
        val total = lista.sumOf {
            val vendida = ventasMap[it.plantaId] ?: 0
            vendida * it.precio
        }
        onTotalChanged(total)
    }
}