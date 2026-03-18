package com.emanuel.mivivero.ui.comunidad

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Comentario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AlbumComunidadFragment : Fragment(R.layout.fragment_album_comunidad) {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var recyclerCarrusel1: RecyclerView
    private lateinit var recyclerCarrusel2: RecyclerView
    private lateinit var recyclerComentarios: RecyclerView

    private lateinit var etPlanta: EditText
    private lateinit var etCantidad: EditText
    private lateinit var btnReservar: Button

    private lateinit var etComentario: EditText
    private lateinit var btnComentar: Button

    private var albumId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumId = arguments?.getString("albumId") ?: ""

        recyclerCarrusel1 = view.findViewById(R.id.recyclerCarrusel1)
        recyclerCarrusel2 = view.findViewById(R.id.recyclerCarrusel2)
        recyclerComentarios = view.findViewById(R.id.recyclerComentarios)

        etPlanta = view.findViewById(R.id.etPlantaNumero)
        etCantidad = view.findViewById(R.id.etCantidad)
        btnReservar = view.findViewById(R.id.btnReservar)

        etComentario = view.findViewById(R.id.etComentario)
        btnComentar = view.findViewById(R.id.btnComentar)

        recyclerCarrusel1.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerCarrusel2.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerComentarios.layoutManager =
            LinearLayoutManager(requireContext())

        cargarFotosAlbum()

        escucharComentarios()

        btnReservar.setOnClickListener {
            reservar()
        }


        btnComentar.setOnClickListener {

            android.util.Log.d("COMENTARIO_DEBUG","BOTON COMENTAR PRESIONADO")

            Toast.makeText(requireContext(),"CLICK",Toast.LENGTH_SHORT).show()

            comentar()
        }


    }

    // =====================================
    // CARGAR FOTOS DEL ÁLBUM
    // =====================================

    private fun cargarFotosAlbum() {

        val storage = FirebaseStorage.getInstance()
        val ref = storage.reference.child("albums/$albumId")

        ref.listAll()
            .addOnSuccessListener { result ->

                val urls = mutableListOf<String>()

                result.items.forEach { item ->

                    item.downloadUrl.addOnSuccessListener { uri ->

                        urls.add(uri.toString())

                        if (urls.size == result.items.size) {
                            mostrarCarruseles(urls)
                        }
                    }
                }
            }
    }

    // =====================================
    // MOSTRAR CARRUSELES
    // =====================================

    private fun mostrarCarruseles(urls: List<String>) {

        val carrusel1 = urls.take(10)
        val carrusel2 = urls.drop(10).take(10)

        recyclerCarrusel1.adapter = AlbumFotoAdapter(carrusel1)

        if (carrusel2.isNotEmpty()) {
            recyclerCarrusel2.visibility = View.VISIBLE
            recyclerCarrusel2.adapter = AlbumFotoAdapter(carrusel2)
        } else {
            recyclerCarrusel2.visibility = View.GONE
        }
    }
    private fun escucharComentarios() {

        db.collection("albumsFeed")
            .document(albumId)
            .collection("comentarios")
            .orderBy("fecha")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                val lista = snapshot.documents.map { doc ->
                    val comentario = doc.toObject(Comentario::class.java)!!
                    comentario.copy(id = doc.id)
                }

                recyclerComentarios.adapter =
                    ComentariosAdapter(
                        lista,
                        albumId,
                        "" // en álbum no hay identificación, dejamos vacío
                    )
            }
    }


    // =====================================
    // RESERVAR PLANTA
    // =====================================

    private fun reservar() {

        val planta = etPlanta.text.toString()
        val cantidad = etCantidad.text.toString()

        if (planta.isBlank() || cantidad.isBlank()) {

            Toast.makeText(
                requireContext(),
                "Completar planta y cantidad",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val email = FirebaseAuth.getInstance().currentUser?.email ?: "usuario"

        val reserva = hashMapOf(
            "uidUsuario" to uid,
            "usuario" to email,
            "plantaNumero" to planta.toInt(),
            "cantidad" to cantidad.toInt(),
            "fechaReserva" to FieldValue.serverTimestamp()
        )

        db.collection("albumsFeed")
            .document(albumId)
            .collection("reservas")
            .add(reserva)
            .addOnSuccessListener {

                Toast.makeText(
                    requireContext(),
                    "Reserva enviada",
                    Toast.LENGTH_SHORT
                ).show()

                etPlanta.setText("")
                etCantidad.setText("")
            }
    }

    // =====================================
    // COMENTAR ÁLBUM
    // =====================================

    private fun comentar() {

        val texto = etComentario.text.toString()

        if (texto.isBlank()) {
            Toast.makeText(requireContext(),"Escribí un comentario",Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val nick = doc.getString("nick") ?: "usuario"

                val comentario = hashMapOf(
                    "uidAutor" to uid,
                    "nickAutor" to nick,
                    "texto" to texto,
                    "fecha" to FieldValue.serverTimestamp(),
                    "tipo" to "comentario"
                )

                db.collection("albumsFeed")
                    .document(albumId)
                    .collection("comentarios")
                    .add(comentario)

                etComentario.setText("")
            }
    }
}