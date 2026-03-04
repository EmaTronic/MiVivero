package com.emanuel.mivivero.ui.comunidad.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Publicacion

class PublicacionesCarruselAdapter(
    private val lista: List<Publicacion>
) : RecyclerView.Adapter<PublicacionesCarruselAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imgPlanta: ImageView = view.findViewById(R.id.imgPlanta)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreComun)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvAutor: TextView = view.findViewById(R.id.tvAutor)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_publicacion_card, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val publicacion = lista[position]

        holder.tvNombre.text =
            publicacion.nombreComun ?: "Sin nombre"

        holder.tvEstado.text =
            publicacion.estado ?: "pendiente"

        holder.tvAutor.text =
            "por ${publicacion.emailAutor}"

        Glide.with(holder.itemView.context)
            .load(publicacion.imageUrl)
            .into(holder.imgPlanta)

        holder.itemView.setOnClickListener {

            val bundle = Bundle().apply {

                putString("publicacionId", publicacion.id)
            }

            Navigation.findNavController(holder.itemView)
                .navigate(
                    R.id.detallePublicacionFragment,
                    bundle
                )
        }
    }
}