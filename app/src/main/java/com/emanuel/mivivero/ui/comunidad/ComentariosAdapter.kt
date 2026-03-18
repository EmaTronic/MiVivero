package com.emanuel.mivivero.ui.comunidad

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Comentario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ComentariosAdapter(
    private val lista: List<Comentario>,
    private val publicacionId: String,
    private val uidAutorPost: String
) : RecyclerView.Adapter<ComentariosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val texto: TextView = view.findViewById(R.id.tvTextoComentario)
        val autor: TextView = view.findViewById(R.id.tvAutorComentario)
        val fecha: TextView = view.findViewById(R.id.tvFechaComentario)

        val btnAceptar: Button = view.findViewById(R.id.btnAceptar)


        val btnEditar: Button = view.findViewById(R.id.btnEditarComentario)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminarComentario)
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



        if (currentUid == comentario.uidAutor) {
            holder.btnEditar.visibility = View.VISIBLE
            holder.btnEliminar.visibility = View.VISIBLE
        } else {
            holder.btnEditar.visibility = View.GONE
            holder.btnEliminar.visibility = View.GONE
        }

        holder.btnEliminar.setOnClickListener {

            FirebaseFirestore.getInstance()
                .collection("albumsFeed")
                .document(publicacionId)
                .collection("comentarios")
                .document(comentario.id)
                .delete()
        }

        holder.btnEditar.setOnClickListener {

            val context = holder.itemView.context

            val editText = EditText(context)
            editText.setText(comentario.texto)

            AlertDialog.Builder(context)
                .setTitle("Editar comentario")
                .setView(editText)

                .setPositiveButton("Guardar") { _, _ ->

                    val nuevoTexto = editText.text.toString()

                    FirebaseFirestore.getInstance()
                        .collection("albumsFeed")
                        .document(publicacionId)
                        .collection("comentarios")
                        .document(comentario.id)
                        .update("texto", nuevoTexto)
                }

                .setNegativeButton("Cancelar", null)
                .show()
        }



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

        //AUTOR
        holder.autor.text = comentario.nickAutor


        // FECHA
        val fecha = comentario.fecha?.toDate()

        if (fecha != null) {
            val formato = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            holder.fecha.text = formato.format(fecha)
        }

        // Mostrar botón ACEPTAR solo si:
        // - Es propuesta
        // - El usuario actual es el autor del post
        if (comentario.tipo == "propuesta" && uidAutorPost.isNotEmpty() && currentUid == uidAutorPost) {
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
                        "prioridadEstado" to 1,
                        "nombreComun" to comentario.nombreComunPropuesto,
                        "nombreCientifico" to comentario.nombreCientificoPropuesto,
                        "identificadaPorUid" to comentario.uidAutor,
                        "identificadaPorEmail" to comentario.emailAutor
                    )
                )

                .addOnSuccessListener {
                    val uidIdentificador = comentario.uidAutor

                    db.collection("usuarios")
                        .document(uidIdentificador)
                        .set(
                            mapOf(
                                "uid" to uidIdentificador,
                                "email" to comentario.emailAutor,
                                "reputacion" to FieldValue.increment(1)
                            ),
                            SetOptions.merge()
                        )
                }
        }
    }
}