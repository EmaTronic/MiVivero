package com.emanuel.mivivero.ui.comunidad

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
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

        recyclerCarrusel1.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerCarrusel2.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerComentarios.layoutManager =
            LinearLayoutManager(requireContext())

        cargarFotosAlbum()

        btnReservar.setOnClickListener {
            reservar()
        }
    }
   private fun cargarFotosAlbum() {

        val storage = FirebaseStorage.getInstance()
        val ref = storage.reference.child("albums/$albumId")

        ref.listAll()
            .addOnSuccessListener { result ->

                val urls = mutableListOf<String>()

                val tasks = result.items.map { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        urls.add(uri.toString())

                        if (urls.size == result.items.size) {
                            mostrarCarruseles(urls)
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
    private fun reservar() {

        val planta = etPlanta.text.toString()
        val cantidad = etCantidad.text.toString()

        if (planta.isBlank() || cantidad.isBlank()) {

            Toast.makeText(requireContext(), "Completar planta y cantidad", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val usuario = FirebaseAuth.getInstance().currentUser?.email ?: "usuario"

        val reserva = hashMapOf(
            "usuario" to usuario,
            "planta" to planta.toInt(),
            "cantidad" to cantidad.toInt(),
            "fecha" to FieldValue.serverTimestamp()
        )

        db.collection("albumsFeed")
            .document(albumId)
            .collection("comentarios")
            .add(reserva)
            .addOnSuccessListener {

                Toast.makeText(requireContext(), "Reserva enviada", Toast.LENGTH_SHORT).show()

                etPlanta.setText("")
                etCantidad.setText("")
            }
    }
}