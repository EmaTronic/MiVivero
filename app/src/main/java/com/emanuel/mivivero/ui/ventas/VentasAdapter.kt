package com.emanuel.mivivero.ui.ventas

import android.text.Editable
import android.text.TextWatcher
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

        var textWatcher: TextWatcher? = null
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

        // 🔴 LIMPIAR watcher previo
        holder.textWatcher?.let {
            holder.etVendida.removeTextChangedListener(it)
        }

        // 🔴 SETEAR valor actual
        val vendidaActual = ventasMap[item.plantaId] ?: 0
        holder.etVendida.setText(
            if (vendidaActual == 0) "" else vendidaActual.toString()
        )

        // 🔴 SETEAR GANANCIA SIEMPRE
        val ganancia = vendidaActual * item.precio
        holder.tvGanancia.text = "$ $ganancia"

        // 🔴 NUEVO watcher limpio
        val watcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val vendida = s.toString().toIntOrNull() ?: 0

                val validada =
                    if (vendida > item.cantidad) item.cantidad else vendida

                ventasMap[item.plantaId] = validada

                val nuevaGanancia = validada * item.precio
                holder.tvGanancia.text = "$ $nuevaGanancia"

                recalcularTotal()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        holder.etVendida.addTextChangedListener(watcher)
        holder.textWatcher = watcher
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