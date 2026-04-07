package com.emanuel.mivivero.ui.ventas

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.PlantaAlbum

class VentasAdapter(
    private val onTotalChanged: (Double) -> Unit,


) : RecyclerView.Adapter<VentasAdapter.VH>() {

    private var lista: List<PlantaAlbum> = emptyList()

    private val ventasMap = mutableMapOf<Long, Pair<Int, Double>>()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val etVendida: EditText = view.findViewById(R.id.etVendida)
        val tvGanancia: TextView = view.findViewById(R.id.tvGanancia)

        var textWatcher: TextWatcher? = null

        val imgPlanta: ImageView = view.findViewById(R.id.imgPlanta)




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

        if (!item.fotoRuta.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.fotoRuta)
                .into(holder.imgPlanta)
        } else {
            holder.imgPlanta.setImageResource(R.drawable.ic_planta_placeholder)
        }

        // 🔴 LIMPIAR watcher previo
        holder.textWatcher?.let {
            holder.etVendida.removeTextChangedListener(it)
        }

        val data = ventasMap[item.plantaId]

        val vendidaActual = data?.first ?: 0

        holder.etVendida.setText(
            if (vendidaActual == 0) "" else vendidaActual.toString()
        )

        val ganancia = vendidaActual * item.precio
        holder.tvGanancia.text = "$ $ganancia"

        // 🔴 NUEVO watcher limpio
        val watcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val vendida = s.toString().toIntOrNull() ?: 0

                val validada = if (vendida < 0) 0 else vendida

                ventasMap[item.plantaId] = Pair(validada, item.precio)

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

            val data = ventasMap[it.plantaId]

            val vendida = data?.first ?: 0
            val precio = data?.second ?: it.precio

            vendida.toDouble() * precio
        }
        onTotalChanged(total)
    }


    fun obtenerVentas(): Map<Long, Pair<Int, Double>> {
        return ventasMap
    }

    fun limpiarVentas() {
        ventasMap.clear()
        notifyDataSetChanged()
    }
}