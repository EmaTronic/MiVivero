package com.emanuel.mivivero.ui.ventas

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.ui.ventas.VentasTablaAdapter

class VentasTablaFragment :
    Fragment(R.layout.fragment_ventas_tabla) {

    private val viewModel: VentasViewModel by viewModels()

    private var albumId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumId = arguments?.getLong("albumId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerVentasTabla)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = VentasTablaAdapter(

            onEditar = { venta ->
                // ya lo tenés implementado en tu código original
            },

            onEliminar = { venta ->
                viewModel.eliminarVenta(
                    venta.id,
                    venta.plantaId,
                    venta.cantidad
                )
            },

            onAgregarClick = {
                // opcional → podés no usarlo
            }
        )

        recycler.adapter = adapter

        viewModel.ventasPorAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->
                adapter.submitList(lista)
            }
    }
}