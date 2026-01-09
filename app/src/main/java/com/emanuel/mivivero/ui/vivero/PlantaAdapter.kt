package com.emanuel.mivivero.ui.vivero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta

class PlantaAdapter(
    private val plantas: List<Planta>
) : RecyclerView.Adapter<PlantaAdapter.PlantaViewHolder>() {

    class PlantaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNumeroPlanta: TextView = itemView.findViewById(R.id.txtNumeroPlanta)
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

        holder.txtNumeroPlanta.text = planta.numeroPlanta
        holder.txtFamiliaEspecie.text =
            if (planta.especie != null)
                "${planta.familia} - ${planta.especie}"
            else
                planta.familia

        holder.txtCantidad.text = "x${planta.cantidad}"

        holder.txtVenta.visibility =
            if (planta.aLaVenta) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = plantas.size
}


