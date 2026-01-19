package com.emanuel.mivivero.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.local.entity.PlantaEntity

class PlantaAdapter(
    private val plantas: List<PlantaEntity>
) : RecyclerView.Adapter<PlantaAdapter.PlantaViewHolder>() {

    inner class PlantaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_planta, parent, false)
        return PlantaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantaViewHolder, position: Int) {
        val planta = plantas[position]

        holder.txtNombre.text = planta.nombre
        holder.txtPrecio.text = "$${planta.precio}"

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("plantaId", planta.id.toLong())
            }

            it.findNavController()
                .navigate(R.id.plantaDetalleFragment, bundle)
        }
    }

    override fun getItemCount(): Int = plantas.size
}
