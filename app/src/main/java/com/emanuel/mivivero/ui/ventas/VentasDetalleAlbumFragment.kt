package com.emanuel.mivivero.ui.ventas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.ui.albumes.AlbumesViewModel

class VentasDetalleAlbumFragment :
    Fragment(R.layout.fragment_ventas_detalle_album) {

    private val viewModel: VentasViewModel by viewModels()

    private var albumId: Long = -1

    private lateinit var recycler: RecyclerView
    private lateinit var tvTotal: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        albumId = arguments?.getLong("albumId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        recycler = view.findViewById(R.id.recyclerVentas)
        tvTotal = view.findViewById(R.id.tvTotalGanancia)

        viewModel.totalPorAlbum(albumId)
            .observe(viewLifecycleOwner) {

                tvTotal.text = "Total: $ $it"
            }

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = VentasTablaAdapter(

            onEditar = { venta ->
                // editar después
            },

            onEliminar = { venta ->
                // eliminar después
            }
        )

        recycler.adapter = adapter

        // 🔴 OBSERVER VA AFUERA (una sola vez)
        viewModel.ventasPorAlbum(albumId)
            .observe(viewLifecycleOwner) {

                adapter.submitList(it)
            }

    }

}