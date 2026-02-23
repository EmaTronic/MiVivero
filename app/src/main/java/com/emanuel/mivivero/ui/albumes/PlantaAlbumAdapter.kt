package com.emanuel.mivivero.ui.albumes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.PlantaAlbum
import com.emanuel.mivivero.databinding.ItemAgregarPlantaAlbumBinding
import com.emanuel.mivivero.databinding.ItemPlantaAlbumGridBinding

class PlantasAlbumAdapter(
    private val items: List<PlantaAlbum>,
    private val onAgregarClick: () -> Unit,
    private val onItemClick: (PlantaAlbum) -> Unit,
    private val onItemLongClick: (PlantaAlbum) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_PLANTA = 1
        private const val MAX_PLANTAS = 30
    }

    override fun getItemCount(): Int {
        return if (items.size < MAX_PLANTAS) {
            items.size + 1 // incluye botón +
        } else {
            items.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items.size < MAX_PLANTAS && position == items.size) {
            TYPE_ADD
        } else {
            TYPE_PLANTA
        }
    }

    // ================= VIEW HOLDERS =================

    inner class PlantaVH(val b: ItemPlantaAlbumGridBinding)
        : RecyclerView.ViewHolder(b.root)

    inner class AddVH(val b: ItemAgregarPlantaAlbumBinding)
        : RecyclerView.ViewHolder(b.root)

    // ================= CREATE =================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_ADD) {

            val binding = ItemAgregarPlantaAlbumBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AddVH(binding)

        } else {

            val binding = ItemPlantaAlbumGridBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            PlantaVH(binding)
        }
    }

    // ================= BIND =================

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is AddVH) {

            holder.b.root.setOnClickListener {
                onAgregarClick()
            }

        } else if (holder is PlantaVH) {

            val planta = items[position]

            holder.b.txtNombre.text = planta.nombre

            Glide.with(holder.itemView.context)
                .load(planta.fotoRuta)
                .placeholder(R.drawable.ic_planta_placeholder)
                .error(R.drawable.ic_planta_placeholder)
                .centerCrop()
                .into(holder.b.imgPlanta)

            // CLICK NORMAL
            holder.b.root.setOnClickListener {
                onItemClick(planta)
            }

            // LONG CLICK → abre dialog editar/eliminar
            holder.b.root.setOnLongClickListener {
                onItemLongClick(planta)
                true
            }
        }
    }
}