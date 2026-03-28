package com.emanuel.mivivero.ui.comunidad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R

class AlbumFotoAdapter(
    private val urls: List<String>
) : RecyclerView.Adapter<AlbumFotoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val img: ImageView = view.findViewById(R.id.imgFoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto_album, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val url = urls[position]

        if (url.isNullOrEmpty()) {
            holder.img.setImageResource(R.drawable.ic_planta_placeholder) // o lo que tengas
        } else {
            Glide.with(holder.itemView)
                .load(url)
                .into(holder.img)
        }
    }

    override fun getItemCount() = urls.size
}