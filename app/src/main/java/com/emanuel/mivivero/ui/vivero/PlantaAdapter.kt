package com.emanuel.mivivero.ui.vivero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.PlantaConFoto

class PlantaAdapter(
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<PlantaAdapter.PlantaViewHolder>() {

    private val plantas = mutableListOf<PlantaConFoto>()

    fun submitList(nuevaLista: List<PlantaConFoto>) {
        plantas.clear()
        plantas.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_planta, parent, false)
        return PlantaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantaViewHolder, position: Int) {
        val item = plantas[position]
        val planta = item.planta
        val foto = item.fotoPrincipal

        // ===== TEXTO =====
        holder.txtNumeroPlanta.text = planta.numeroPlanta

        holder.txtFamiliaEspecie.text =
            planta.especie?.let { "${planta.familia} - $it" }
                ?: planta.familia

        holder.txtCantidad.text = "x${planta.cantidad}"

        holder.txtVenta.visibility =
            if (planta.aLaVenta) View.VISIBLE else View.GONE

        // ===== FOTO PRINCIPAL =====
        if (foto != null) {
            Glide.with(holder.itemView)
                .load(foto.rutaLocal)
                .centerCrop()
                .into(holder.imgPlanta)
        } else {
            holder.imgPlanta.setImageResource(R.drawable.ic_planta_placeholder)
        }

        // ===== CLICK =====
        holder.itemView.setOnClickListener {
            onItemClick(planta.id)
        }
    }

    override fun getItemCount(): Int = plantas.size

    class PlantaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPlanta: ImageView = itemView.findViewById(R.id.imgPlanta)
        val txtNumeroPlanta: TextView = itemView.findViewById(R.id.txtNumeroPlanta)
        val txtFamiliaEspecie: TextView = itemView.findViewById(R.id.txtFamiliaEspecie)
        val txtCantidad: TextView = itemView.findViewById(R.id.txtCantidad)
        val txtVenta: TextView = itemView.findViewById(R.id.txtVenta)
    }
}
