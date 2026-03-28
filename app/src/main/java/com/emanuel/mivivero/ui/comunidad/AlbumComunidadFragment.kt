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
    private lateinit var recyclerReservas: RecyclerView

    private lateinit var etPlanta: EditText
    private lateinit var etCantidad: EditText
    private lateinit var btnReservar: Button

    private lateinit var etComentario: EditText
    private lateinit var btnComentar: Button

    private val listaReservas = mutableListOf<Map<String, Any>>()
    private lateinit var adapterReservas: ReservasAdapter

    private var albumId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumId = arguments?.getString("albumId") ?: ""

        Log.d("RESERVAS_DEBUG", "albumId FRAGMENT = $albumId")

        // =========================
        // VISTAS
        // =========================
        recyclerCarrusel1 = view.findViewById(R.id.recyclerCarrusel1)
        recyclerCarrusel2 = view.findViewById(R.id.recyclerCarrusel2)
        recyclerComentarios = view.findViewById(R.id.recyclerComentarios)
        recyclerReservas = view.findViewById(R.id.recyclerReservas)

        etPlanta = view.findViewById(R.id.etPlantaNumero)
        etCantidad = view.findViewById(R.id.etCantidad)
        btnReservar = view.findViewById(R.id.btnReservar)

        etComentario = view.findViewById(R.id.etComentario)
        btnComentar = view.findViewById(R.id.btnComentar)

        // =========================
        // RECYCLERS
        // =========================

        recyclerCarrusel1.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerCarrusel2.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerComentarios.layoutManager =
            LinearLayoutManager(requireContext())
        recyclerComentarios.setHasFixedSize(false)
        recyclerComentarios.isNestedScrollingEnabled = false

        recyclerReservas.layoutManager =
            LinearLayoutManager(requireContext())
        recyclerReservas.setHasFixedSize(false)
        recyclerReservas.isNestedScrollingEnabled = false

        adapterReservas = ReservasAdapter(listaReservas, albumId)
        recyclerReservas.adapter = adapterReservas

        // =========================
        // ACCIONES
        // =========================

        btnReservar.setOnClickListener { reservar() }
        btnComentar.setOnClickListener { comentar() }

        // =========================
        // CARGAS
        // =========================

        cargarFotosAlbum()
        escucharComentarios()
        escucharReservas()
    }

    // =====================================
    // CARGAR FOTOS
    // =====================================

    private fun cargarFotosAlbum() {

        db.collection("albumsFeed")
            .document(albumId)
            .get()
            .addOnSuccessListener { doc ->

                val uidAutor = doc.getString("uidAutor") ?: return@addOnSuccessListener

                val storage = FirebaseStorage.getInstance()
                val ref = storage.reference
                    .child("albumsFeed")
                    .child(uidAutor)
                    .child(albumId)

                ref.listAll().addOnSuccessListener { result ->

                    val urls = mutableListOf<String>()

                    if (result.items.isEmpty()) {
                        Log.e("FOTOS", "NO HAY IMAGENES EN STORAGE")
                        return@addOnSuccessListener
                    }

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
    }


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

    // =====================================
    // COMENTARIOS
    // =====================================

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
                    ComentariosAdapter(lista, albumId, "")
            }
    }

    private fun comentar() {

        val texto = etComentario.text.toString()

        if (texto.isBlank()) {
            Toast.makeText(requireContext(), "Escribí un comentario", Toast.LENGTH_SHORT).show()
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

    // =====================================
    // RESERVAS
    // =====================================

    private fun escucharReservas() {

        db.collection("albumsFeed")
            .document(albumId)
            .collection("reservas")
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    Log.e("RESERVAS_DEBUG", "ERROR", e)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                Log.d("RESERVAS_DEBUG", "Docs recibidos: ${snapshot.size()}")

                val nuevaLista = mutableListOf<Map<String, Any>>()

                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue

                    val mapa = HashMap<String, Any>(data)
                    mapa["id"] = doc.id

                    nuevaLista.add(mapa)
                }

                Log.d("RESERVAS_DEBUG", "Lista final: ${nuevaLista.size}")

                adapterReservas.actualizarLista(nuevaLista)
            }
    }

    private fun reservar() {

        val planta = etPlanta.text.toString().toIntOrNull()
        val cantidad = etCantidad.text.toString().toIntOrNull()

        if (planta == null || cantidad == null) {
            Toast.makeText(requireContext(), "Valores inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val nick = doc.getString("nick") ?: "usuario"

                val reserva = hashMapOf(
                    "uidUsuario" to uid,
                    "nickUsuario" to nick,
                    "plantaNumero" to planta,
                    "cantidad" to cantidad,
                    "fechaReserva" to FieldValue.serverTimestamp()
                )

                db.collection("albumsFeed")
                    .document(albumId)
                    .collection("reservas")
                    .add(reserva)

                etPlanta.setText("")
                etCantidad.setText("")
            }
    }
}