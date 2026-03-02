package com.emanuel.mivivero.ui.comunidad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Comentario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ComentariosAdapter(
    private val lista: List<Comentario>,
    private val publicacionId: String,
    private val uidAutorPost: String
) : RecyclerView.Adapter<ComentariosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val texto: TextView = view.findViewById(R.id.tvTextoComentario)
        val autor: TextView = view.findViewById(R.id.tvAutorComentario)
        val btnAceptar: Button = view.findViewById(R.id.btnAceptar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comentario, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val comentario = lista[position]
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        // Mostrar texto normal o propuesta
        if (comentario.tipo == "propuesta") {
            holder.texto.text =
                "🌿 Propuesta: ${comentario.nombreComunPropuesto}" +
                        (if (!comentario.nombreCientificoPropuesto.isNullOrEmpty())
                            "\n(${comentario.nombreCientificoPropuesto})"
                        else "")
        } else {
            holder.texto.text = comentario.texto
        }

        holder.autor.text = comentario.emailAutor

        // Mostrar botón ACEPTAR solo si:
        // - Es propuesta
        // - El usuario actual es el autor del post
        if (comentario.tipo == "propuesta" && currentUid == uidAutorPost) {
            holder.btnAceptar.visibility = View.VISIBLE
        } else {
            holder.btnAceptar.visibility = View.GONE
        }

        // Acción al aceptar propuesta
        holder.btnAceptar.setOnClickListener {

            val db = FirebaseFirestore.getInstance()

            db.collection("publicaciones")
                .document(publicacionId)
                .update(
                    mapOf(
                        "estado" to "identificada",
                        "nombreComun" to comentario.nombreComunPropuesto,
                        "nombreCientifico" to comentario.nombreCientificoPropuesto,
                        "identificadaPorUid" to comentario.uidAutor,
                        "identificadaPorEmail" to comentario.emailAutor
                    )
                )
        }
    }
}