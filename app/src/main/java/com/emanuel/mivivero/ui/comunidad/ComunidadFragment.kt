package com.emanuel.mivivero.ui.comunidad

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Publicacion
import com.google.firebase.auth.FirebaseAuth

class ComunidadFragment : Fragment(R.layout.fragment_comunidad) {

    private lateinit var recycler: RecyclerView
    private val db = FirebaseFirestore.getInstance()

    private lateinit var recyclerMisPublicaciones: RecyclerView
    private lateinit var recyclerComunidad: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("ENTRO A onViewCreated COMUNIDAD")


        val uidActual = FirebaseAuth.getInstance().currentUser?.uid
        val emailActual = FirebaseAuth.getInstance().currentUser?.email

        println("USUARIO ACTUAL UID: $uidActual")
        println("USUARIO ACTUAL EMAIL: $emailActual")

        recyclerMisPublicaciones =
            view.findViewById(R.id.recyclerMisPublicaciones)

        recyclerComunidad =
            view.findViewById(R.id.recyclerComunidad)

        recyclerMisPublicaciones.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerComunidad.layoutManager =
            LinearLayoutManager(requireContext())

        cargarPublicaciones()
        cargarMisPublicaciones()


        println("VOY A LLAMAR cargarComunidad()")
        cargarComunidad()





    }

    private fun cargarPublicaciones() {
        db.collection("publicaciones")
            .orderBy("fecha")
            .get()
            .addOnSuccessListener { result ->
                // por ahora solo logueamos
                for (doc in result) {
                    println("Publicación: ${doc.data}")
                }
            }
    }

    private fun cargarMisPublicaciones() {

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        println("UID ACTUAL MIS PUBLICACIONES: ${FirebaseAuth.getInstance().currentUser?.uid}")

        println("UID ACTUAL MIS PUBLICACIONES: $uid")

        db.collection("publicaciones")
            .whereEqualTo("uidAutor", uid)

            //.orderBy("fecha")
            .get()
            .addOnSuccessListener { result ->

                println("CANTIDAD ENCONTRADA: ${result.size()}")

                val lista = result.map {
                    it.toObject(Publicacion::class.java).copy(id = it.id)
                }

                recyclerMisPublicaciones.adapter =
                    MisPublicacionesAdapter(lista)
            }
            .addOnFailureListener { e ->
                println("ERROR MIS PUBLICACIONES: ${e.message}")
            }
    }
    private fun cargarComunidad() {

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        db.collection("publicaciones")
            .orderBy("fecha")
            .get()
            .addOnSuccessListener { result ->

                val lista = result.map {
                    it.toObject(Publicacion::class.java).copy(id = it.id)
                }.filter {
                    it.uidAutor != uid
                }

                recyclerComunidad.adapter =
                    ComunidadAdapter(lista)
            }
    }


}