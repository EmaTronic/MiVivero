package com.emanuel.mivivero.ui.albumes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.PlantaAlbum
import com.emanuel.mivivero.databinding.ItemPlantaAlbumBinding

class PlantasAlbumAdapter(
    private val items: List<PlantaAlbum>,
    private val onClick: (PlantaAlbum) -> Unit
) : RecyclerView.Adapter<PlantasAlbumAdapter.VH>() {

    inner class VH(val b: ItemPlantaAlbumBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPlantaAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = items[position]

        h.b.txtNombre.text = p.nombre
        h.b.txtCantidad.text = "Cantidad: ${p.cantidad}"
        h.b.txtPrecio.text = "Precio: $${p.precio}"

        Glide.with(h.itemView.context)
            .load(p.fotoRuta)
            .placeholder(R.drawable.ic_planta_placeholder)
            .error(R.drawable.ic_planta_placeholder)
            .centerCrop()
            .into(h.b.imgPlanta)

        h.b.root.setOnClickListener {
            onClick(p)
        }
    }

    override fun getItemCount(): Int = items.size
}
