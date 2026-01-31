package com.emanuel.mivivero.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.FotoPlanta

class FotoAdapter(
    private val fotos: List<FotoPlanta>
) : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    inner class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgFoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        holder.img.setImageURI(Uri.parse(fotos[position].ruta))
    }

    override fun getItemCount(): Int = fotos.size
}
