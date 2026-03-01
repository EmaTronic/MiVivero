package com.emanuel.mivivero.ui.comunidad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Comentario

class ComentariosAdapter(
    private val lista: List<Comentario>
) : RecyclerView.Adapter<ComentariosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val texto: TextView = view.findViewById(R.id.tvTextoComentario)
        val autor: TextView = view.findViewById(R.id.tvAutorComentario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comentario, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comentario = lista[position]
        holder.texto.text = comentario.texto
        holder.autor.text = comentario.emailAutor
    }
}