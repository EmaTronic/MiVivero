package com.emanuel.mivivero.ui.adapter

import android.net.Uri
import android.os.Bundle
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
    private val plantas: List<Planta>,
    private val modoAgregarAlbum: Boolean = false,
    private val onAgregarPlantaAlbum: ((Planta) -> Unit)?

) : RecyclerView.Adapter<PlantaAdapter.PlantaViewHolder>() {

    inner class PlantaViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imgPlanta: ImageView = itemView.findViewById(R.id.imgPlanta)
        val txtNumero: TextView = itemView.findViewById(R.id.txtNumero)
        val txtFamiliaEspecie: TextView =
            itemView.findViewById(R.id.txtFamiliaEspecie)
        val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidad)
        val txtVenta: TextView = itemView.findViewById(R.id.txtVenta)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlantaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_planta, parent, false)
        return PlantaViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PlantaViewHolder,
        position: Int
    ) {
        val planta = plantas[position]

        // 🔢 Número
        holder.txtNumero.text = "Nro ${planta.numeroPlanta}"

        // 🌿 Familia + especie
        holder.txtFamiliaEspecie.text =
            "${planta.familia} ${planta.especie ?: ""}"

        // 📦 Cantidad
        holder.txtCantidad.text = "Stock: ${planta.cantidad}"

        // 💰 Venta
        holder.txtVenta.text =
            if (planta.aLaVenta) "En venta" else "No disponible"

        // 📷 Foto
        if (planta.fotoRuta != null) {
            holder.imgPlanta.setImageURI(Uri.parse(planta.fotoRuta))
        } else {
            holder.imgPlanta.setImageResource(R.drawable.ic_planta_placeholder)
        }

        // 👉 Navegar a detalle
        holder.itemView.setOnClickListener {

            if (modoAgregarAlbum && onAgregarPlantaAlbum != null) {
                // 👉 ESTÁS AGREGANDO PLANTA AL ÁLBUM
                onAgregarPlantaAlbum.invoke(planta)
            } else {
                // 👉 COMPORTAMIENTO NORMAL (NO SE TOCA)
                val bundle = Bundle().apply {
                    putLong("plantaId", planta.id)
                }

                it.findNavController()
                    .navigate(R.id.plantaDetalleFragment, bundle)
            }
        }

    }

    override fun getItemCount(): Int = plantas.size
}
