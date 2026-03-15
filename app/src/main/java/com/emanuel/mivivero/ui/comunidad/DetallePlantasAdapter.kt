package com.emanuel.mivivero.ui.comunidad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R

class DetallePlantasAdapter(
    private val onReservar: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<DetallePlantasAdapter.VH>() {

    private var items: List<Map<String, Any>> = emptyList()

    fun submitList(lista: List<Map<String, Any>>) {
        items = lista
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {

        val img: ImageView = view.findViewById(R.id.imgPlanta)
        val nombre: TextView = view.findViewById(R.id.txtNombre)
        val precio: TextView = view.findViewById(R.id.txtPrecio)
        val cantidad: TextView = view.findViewById(R.id.txtCantidad)
        val btnReservar: Button = view.findViewById(R.id.btnReservar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_planta_publicada, parent, false)

        return VH(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {

        val planta = items[position]

        holder.nombre.text = planta["nombre"].toString()
        holder.precio.text = "$${planta["precio"]}"
        holder.cantidad.text =
            "Disponible: ${planta["cantidad"]}"

        Glide.with(holder.itemView)
            .load(planta["url"])
            .into(holder.img)

        holder.btnReservar.setOnClickListener {
            onReservar(planta)
        }
    }
}