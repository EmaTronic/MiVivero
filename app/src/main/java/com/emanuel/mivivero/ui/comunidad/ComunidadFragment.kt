package com.emanuel.mivivero.ui.comunidad

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Publicacion
import com.emanuel.mivivero.ui.comunidad.adapter.ComunidadFeedAdapter




class ComunidadFragment : Fragment(R.layout.fragment_comunidad) {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerFeed: RecyclerView
    private lateinit var feedAdapter: ComunidadFeedAdapter

    private var listaMisPublicaciones: List<Publicacion> = emptyList()
    private var listaComunidadCompleta: List<Publicacion> = emptyList()
    private var filtroEstadoActual: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerFeed = view.findViewById(R.id.recyclerFeedComunidad)

        recyclerFeed.layoutManager =
            LinearLayoutManager(requireContext())

        // Crear adapter UNA sola vez
        feedAdapter = ComunidadFeedAdapter(
            misPublicaciones = listaMisPublicaciones,
            carruselesComunidad = emptyList(),

            onFiltroTodas = {
                filtroEstadoActual = null
                aplicarFiltros()
            },

            onFiltroPendientes = {
                filtroEstadoActual = "pendiente"
                aplicarFiltros()
            },

            onFiltroIdentificadas = {
                filtroEstadoActual = "identificada"
                aplicarFiltros()
            },

            onBuscar = { texto ->
                aplicarFiltros(texto)
            }
        )

        recyclerFeed.adapter = feedAdapter

        // Cargar datos desde Firestore
        cargarMisPublicaciones()
        cargarComunidad()
    }

    private fun cargarMisPublicaciones() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("publicaciones")
            .whereEqualTo("uidAutor", uid)
            .get()
            .addOnSuccessListener { result ->

                listaMisPublicaciones = result.map {

                    it.toObject(Publicacion::class.java).copy(id = it.id)
                }

                construirFeed()
            }
    }

    private fun cargarComunidad() {

        db.collection("publicaciones")
            .orderBy("prioridadEstado")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                listaComunidadCompleta = result.map {

                    it.toObject(Publicacion::class.java).copy(id = it.id)
                }

                aplicarFiltros()
            }
    }

    private fun aplicarFiltros(textoBusqueda: String = "") {

        var lista = listaComunidadCompleta

        filtroEstadoActual?.let { estado ->
            lista = lista.filter { it.estado == estado }
        }

        if (textoBusqueda.isNotEmpty()) {

            lista = lista.filter {
                it.nombreComun
                    ?.contains(textoBusqueda, ignoreCase = true) == true
            }
        }

        construirFeed(lista)
    }

    private fun construirFeed(lista: List<Publicacion> = listaComunidadCompleta) {

        val carruseles = lista.chunked(8)

        feedAdapter.updateData(
            listaMisPublicaciones,
            carruseles
        )
    }
}