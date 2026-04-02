package com.emanuel.mivivero.ui.ventas

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R

class VentasAnalisisFragment :
    Fragment(R.layout.fragment_ventas_analisis) {

    private val viewModel: VentasViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔴 Recycler
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerVentas)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        // 🔴 Adapter con navegación
        val adapter = VentasAlbumAdapter { albumId ->

            findNavController().navigate(
                R.id.ventasDetalleAlbumFragment,
                Bundle().apply {
                    putLong("albumId", albumId)
                }
            )
        }

        recycler.adapter = adapter

        // 🔴 OBSERVE (datos reales)
        viewModel.resumenAlbumes.observe(viewLifecycleOwner) {

            adapter.submitList(it)
        }
    }
}