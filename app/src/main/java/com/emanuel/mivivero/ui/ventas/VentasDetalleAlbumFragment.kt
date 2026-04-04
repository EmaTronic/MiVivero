package com.emanuel.mivivero.ui.ventas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R

class VentasDetalleAlbumFragment :
    Fragment(R.layout.fragment_ventas_detalle_album) {

    private val viewModel: VentasViewModel by viewModels()

    private var albumId: Long = -1

    private lateinit var recycler: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnGuardar: Button

    private lateinit var adapter: VentasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumId = arguments?.getLong("albumId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerVentas)
        tvTotal = view.findViewById(R.id.tvTotalGanancia)
        btnGuardar = view.findViewById(R.id.btnGuardarVentas)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        // 🔴 ADAPTER DE PLANTAS (NO ventas)
        adapter = VentasAdapter { total ->
            tvTotal.text = "Total: $ $total"
        }

        recycler.adapter = adapter

        // 🔴 TRAER PLANTAS DEL ALBUM
        viewModel.plantasPorAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->

                android.util.Log.e("PLANTAS_UI", "SIZE = ${lista.size}")

                if (lista.isEmpty()) {
                    android.util.Log.e("PLANTAS_UI", "LISTA VACIA")
                }

                lista.forEach {
                    android.util.Log.e("PLANTAS_UI", "ITEM = ${it.nombreCompleto}")
                }

                adapter.submitList(lista)
            }

        // 🔴 GUARDAR VENTAS (USA TU MAP INTERNO DEL ADAPTER)
        btnGuardar.setOnClickListener {

            val ventas = adapter.obtenerVentas()

            ventas.forEach { (plantaId, data) ->

                val cantidad = data.first
                val precio = data.second

                if (cantidad > 0) {

                    viewModel.registrarVenta(
                        plantaId = plantaId,
                        albumId = albumId,
                        cantidad = cantidad,
                        precio = precio
                    )
                }
            }

            adapter.limpiarVentas()
        }
    }
}