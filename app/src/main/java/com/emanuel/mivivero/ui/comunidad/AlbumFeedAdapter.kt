package com.emanuel.mivivero.ui.comunidad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.emanuel.mivivero.R
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.data.model.AlbumFeed

class AlbumFeedAdapter(
    private val lista: List<AlbumFeed>
) : RecyclerView.Adapter<AlbumFeedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.imgPublicacion)
        val titulo: TextView = view.findViewById(R.id.tvObservacion)
        val autor: TextView = view.findViewById(R.id.tvAutor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comunidad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val album = lista[position]

        holder.titulo.text = album.titulo
        holder.autor.text = "Autor: ${album.uidAutor}"

        // 🔴 CLAVE: usar portadaUrl (NO imageUrl)
        Glide.with(holder.itemView.context)
            .load(album.portadaUrl)
            .into(holder.imagen)
    }

    override fun getItemCount(): Int = lista.size
}