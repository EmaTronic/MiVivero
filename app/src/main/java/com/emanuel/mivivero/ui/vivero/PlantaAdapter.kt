package com.emanuel.mivivero.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta

class PlantaAdapter(
    private val plantas: List<Planta>
) : RecyclerView.Adapter<PlantaAdapter.PlantaViewHolder>() {

    inner class PlantaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPlanta: ImageView = itemView.findViewById(R.id.imgPlanta)
        val txtNumero: TextView = itemView.findViewById(R.id.txtNumero)
        val txtFamiliaEspecie: TextView = itemView.findViewById(R.id.txtFamiliaEspecie)
        val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidad)
        val txtVenta: TextView = itemView.findViewById(R.id.txtVenta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_planta, parent, false)
        return PlantaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantaViewHolder, position: Int) {
        val planta = plantas[position]

        holder.txtNumero.text = planta.numeroPlanta

        holder.txtFamiliaEspecie.text =
            if (planta.especie.isNullOrBlank()) {
                planta.familia
            } else {
                "${planta.familia} · ${planta.especie}"
            }

        holder.txtCantidad.text = "Cantidad: ${planta.cantidad}"

        holder.txtVenta.text =
            if (planta.aLaVenta) "A la venta" else "No disponible"

        // FOTO
        if (!planta.fotoRuta.isNullOrBlank()) {
            holder.imgPlanta.setImageURI(Uri.parse(planta.fotoRuta))
        } else {
            holder.imgPlanta.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // NAVEGACIÓN A DETALLE
        holder.itemView.setOnClickListener {
            val bundle = android.os.Bundle().apply {
                putLong("plantaId", planta.id)
            }

            it.findNavController()
                .navigate(R.id.detallePlantaFragment, bundle)
        }
    }

    override fun getItemCount(): Int = plantas.size
}
