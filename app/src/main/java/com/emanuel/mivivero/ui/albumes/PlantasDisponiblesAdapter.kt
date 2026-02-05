package com.emanuel.mivivero.ui.albumes

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.databinding.ItemPlantaBinding

class PlantasDisponiblesAdapter(
    private val onClick: (Planta) -> Unit
) : RecyclerView.Adapter<PlantasDisponiblesAdapter.VH>() {

    private val items = mutableListOf<Planta>()

    fun submit(list: List<Planta>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemPlantaBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPlantaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = items[position]

        h.b.txtNumero.text = p.numeroPlanta.toString()
        h.b.txtFamiliaEspecie.text =
            listOfNotNull(p.familia, p.especie).joinToString(" – ")
        h.b.txtCantidad.text = "Cantidad: ${p.cantidad}"
        h.b.txtVenta.text =
            if (p.aLaVenta) "Disponible para la venta" else "No disponible"

        // 🔥 CARGA DE IMAGEN CORRECTA
        Glide.with(h.itemView.context)
            .load(p.fotoRuta?.let { Uri.parse(it) })
            .placeholder(R.drawable.ic_planta_placeholder)
            .error(R.drawable.ic_planta_placeholder)
            .centerCrop()
            .into(h.b.imgPlanta)

        h.itemView.setOnClickListener {
            onClick(p)
        }
    }

    override fun getItemCount(): Int = items.size
}
