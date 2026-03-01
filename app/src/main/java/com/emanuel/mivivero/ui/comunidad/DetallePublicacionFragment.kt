package com.emanuel.mivivero.ui.comunidad

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Comentario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetallePublicacionFragment : Fragment(R.layout.fragment_detalle_publicacion) {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var recyclerComentarios: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val publicacionId = arguments?.getString("publicacionId") ?: return

        val img = view.findViewById<ImageView>(R.id.imgDetalle)
        val obs = view.findViewById<TextView>(R.id.tvObservacionDetalle)
        val autor = view.findViewById<TextView>(R.id.tvAutorDetalle)

        //INICIALIZO
        recyclerComentarios =
            view.findViewById(R.id.recyclerComentarios)
        //ESCUCHO
        recyclerComentarios.layoutManager =
            LinearLayoutManager(requireContext())

        escucharComentarios(publicacionId)

        db.collection("publicaciones")
            .document(publicacionId)
            .get()
            .addOnSuccessListener { doc ->

                val imageUrl = doc.getString("imageUrl")
                val observacion = doc.getString("observacion")
                val emailAutor = doc.getString("emailAutor")

                obs.text = observacion
                autor.text = "Publicado por: $emailAutor"

                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(img)
            }


        val etComentario = view.findViewById<EditText>(R.id.etComentario)
        val btnEnviar = view.findViewById<Button>(R.id.btnEnviarComentario)

        btnEnviar.setOnClickListener {

            val texto = etComentario.text.toString().trim()
            if (texto.isEmpty()) return@setOnClickListener

            val user = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener

            val comentario = hashMapOf(
                "uidAutor" to user.uid,
                "emailAutor" to user.email,
                "texto" to texto,
                "fecha" to com.google.firebase.Timestamp.now()
            )

            db.collection("publicaciones")
                .document(publicacionId)
                .collection("comentarios")
                .add(comentario)

            etComentario.text.clear()
        }
    }


    private fun escucharComentarios(publicacionId: String) {

        db.collection("publicaciones")
            .document(publicacionId)
            .collection("comentarios")
            .orderBy("fecha")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                val lista = snapshot.documents.map {
                    it.toObject(Comentario::class.java)!!.copy(id = it.id)
                }

                recyclerComentarios.adapter =
                    ComentariosAdapter(lista)
            }
    }
}