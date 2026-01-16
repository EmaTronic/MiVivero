package com.emanuel.mivivero.ui.vivero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.FotoEntity

class FotoAdapter : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    private val fotos = mutableListOf<FotoEntity>()

    fun submitList(nuevaLista: List<FotoEntity>) {
        fotos.clear()
        fotos.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = fotos[position]
        Glide.with(holder.itemView)
            .load(foto.rutaLocal)
            .into(holder.image)
    }

    override fun getItemCount(): Int = fotos.size

    class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgFoto)
    }
}
