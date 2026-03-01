package com.emanuel.mivivero.ui.comunidad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Publicacion

class ComunidadAdapter(
    private val lista: List<Publicacion>
) : RecyclerView.Adapter<ComunidadAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.imgPublicacion)
        val observacion: TextView = view.findViewById(R.id.tvObservacion)
        val autor: TextView = view.findViewById(R.id.tvAutor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comunidad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val publicacion = lista[position]

        holder.observacion.text = publicacion.observacion
        holder.autor.text = "Publicado por: ${publicacion.emailAutor}"

        holder.itemView.setOnClickListener {

            val bundle = Bundle().apply {
                putString("publicacionId", publicacion.id)
            }

            val navController =
                androidx.navigation.Navigation.findNavController(holder.itemView)

            navController.navigate(
                R.id.detallePublicacionFragment,
                bundle
            )
        }



        Glide.with(holder.itemView.context)
            .load(publicacion.imageUrl)
            .into(holder.imagen)
    }

    override fun getItemCount(): Int = lista.size
}