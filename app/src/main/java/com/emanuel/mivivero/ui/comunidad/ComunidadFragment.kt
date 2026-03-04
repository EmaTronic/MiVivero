package com.emanuel.mivivero.ui.comunidad

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Publicacion

class ComunidadFragment : Fragment(R.layout.fragment_comunidad) {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var recyclerMisPublicaciones: RecyclerView
    private lateinit var recyclerComunidad: RecyclerView

    private lateinit var btnFiltroTodas: Button
    private lateinit var btnFiltroPendientes: Button
    private lateinit var btnFiltroIdentificadas: Button
    private lateinit var etBuscarNombre: EditText
    private lateinit var btnBuscar: Button

    private var filtroEstadoActual: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerMisPublicaciones = view.findViewById(R.id.recyclerMisPublicaciones)
        recyclerComunidad = view.findViewById(R.id.recyclerComunidad)

        btnFiltroTodas = view.findViewById(R.id.btnFiltroTodas)
        btnFiltroPendientes = view.findViewById(R.id.btnFiltroPendientes)
        btnFiltroIdentificadas = view.findViewById(R.id.btnFiltroIdentificadas)
        etBuscarNombre = view.findViewById(R.id.etBuscarNombre)
        btnBuscar = view.findViewById(R.id.btnBuscar)

        recyclerMisPublicaciones.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerComunidad.layoutManager =
            LinearLayoutManager(requireContext())

        configurarEventosFiltros()

        cargarMisPublicaciones()
        cargarComunidad()
    }

    private fun configurarEventosFiltros() {

        btnFiltroTodas.setOnClickListener {
            println("CLICK TODAS")
            filtroEstadoActual = null
            cargarComunidad()
        }

        btnFiltroPendientes.setOnClickListener {
            println("CLICK PENDIENTES")
            filtroEstadoActual = "pendiente"
            cargarComunidad()
        }

        btnFiltroIdentificadas.setOnClickListener {
            println("CLICK IDENTIFICADAS")
            filtroEstadoActual = "identificada"
            cargarComunidad()
        }

        btnBuscar.setOnClickListener {
            cargarComunidad()
        }
    }

    private fun cargarMisPublicaciones() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("publicaciones")
            .whereEqualTo("uidAutor", uid)
            .get()
            .addOnSuccessListener { result ->

                val lista = result.map {
                    it.toObject(Publicacion::class.java).copy(id = it.id)
                }

                recyclerMisPublicaciones.adapter =
                    MisPublicacionesAdapter(lista)
            }
    }

    private fun cargarComunidad() {

        val textoBusqueda = etBuscarNombre.text.toString().trim()

        var query: Query = db.collection("publicaciones")

        if (filtroEstadoActual != null) {
            // 🔹 Cuando hay filtro por estado
            query = query
                .whereEqualTo("estado", filtroEstadoActual)
                .orderBy("fecha", Query.Direction.DESCENDING)
        } else {
            // 🔹 Cuando mostramos TODAS
            query = query
                .orderBy("prioridadEstado")
                .orderBy("fecha", Query.Direction.DESCENDING)
        }

        query.get()
            .addOnSuccessListener { result ->

                var lista = result.map {
                    it.toObject(Publicacion::class.java).copy(id = it.id)
                }

                // 🔹 Filtro búsqueda en memoria (no en Firestore)
                if (textoBusqueda.isNotEmpty()) {
                    lista = lista.filter {
                        it.nombreComun
                            ?.contains(textoBusqueda, ignoreCase = true) == true
                    }
                }

                println("RESULTADOS FILTRO: ${lista.size}")
                println("ESTADO FILTRO: $filtroEstadoActual")

                recyclerComunidad.adapter =
                    ComunidadAdapter(lista)
            }
            .addOnFailureListener {
                println("ERROR FILTRO: ${it.message}")
            }
    }
}