package com.emanuel.mivivero.ui.albumes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.data.local.entity.AlbumEntity
import com.emanuel.mivivero.databinding.ItemAlbumBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlbumesAdapter(
    private val items: List<AlbumEntity>,
    private val onClick: (AlbumEntity) -> Unit
) : RecyclerView.Adapter<AlbumesAdapter.VH>() {

    inner class VH(val b: ItemAlbumBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val album = items[position]

        holder.b.txtNombreAlbum.text = album.nombre
        holder.b.txtFecha.text = album.fechaCreacion.toString()

        holder.itemView.setOnClickListener {
            onClick(album)
        }
    }

    override fun getItemCount(): Int = items.size
}

