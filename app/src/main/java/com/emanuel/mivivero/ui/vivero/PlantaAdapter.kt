package com.emanuel.mivivero.ui.vivero

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta

class PlantaAdapter(
    plantas: List<Planta>,
    private val modoAgregarAlbum: Boolean = false,
    private val onAgregarPlantaAlbum: ((Planta) -> Unit)?
) : RecyclerView.Adapter<PlantaAdapter.PlantaViewHolder>() {

    private val plantas = plantas.toMutableList()


    inner class PlantaViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imgPlanta: ImageView = itemView.findViewById(R.id.imgPlanta)
        val txtNumero: TextView = itemView.findViewById(R.id.txtNumero)
        val txtFamiliaEspecie: TextView =
            itemView.findViewById(R.id.txtFamiliaEspecie)
        val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidad)
        val txtLugarIcono: TextView = itemView.findViewById(R.id.txtLugarIcono)
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
        holder.txtNumero.text = "N° ${planta.numeroPlanta}"

        // 🌿 Nombre (familia + especie)
        holder.txtFamiliaEspecie.text =
            "${planta.familia} ${planta.especie ?: ""}".trim()

        // 📦 Cantidad
        holder.txtCantidad.text = "Stock: ${planta.cantidad}"

        // 🟠 Stock bajo → naranja
        if (planta.cantidad <= 2) {
            holder.txtCantidad.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.orange_warning
                )
            )
        } else {
            holder.txtCantidad.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.text_primary
                )
            )
        }

        if (!planta.lugarIcono.isNullOrBlank()) {
            holder.txtLugarIcono.visibility = View.VISIBLE
            holder.txtLugarIcono.text = planta.lugarIcono
        } else {
            holder.txtLugarIcono.visibility = View.GONE
        }

        // 💰 Badge venta (solo visible si está en venta)
        if (planta.aLaVenta) {
            holder.txtVenta.visibility = View.VISIBLE
            holder.txtVenta.text = "En venta"
        } else {
            holder.txtVenta.visibility = View.GONE
        }

        // 📷 Foto
        if (!planta.fotoRuta.isNullOrEmpty()) {
            holder.imgPlanta.setImageURI(Uri.parse(planta.fotoRuta))
        } else {
            holder.imgPlanta.setImageResource(R.drawable.ic_planta_placeholder)
        }

        // 👉 Navegación / Modo álbum
        holder.itemView.setOnClickListener {

            if (modoAgregarAlbum && onAgregarPlantaAlbum != null) {
                onAgregarPlantaAlbum.invoke(planta)
            } else {
                val bundle = Bundle().apply {
                    putLong("plantaId", planta.id)
                }

                it.findNavController()
                    .navigate(R.id.plantaDetalleFragment, bundle)
            }
        }
    }

    fun actualizarLista(nuevaLista: List<Planta>) {
        plantas.clear()
        plantas.addAll(nuevaLista)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = plantas.size
}
