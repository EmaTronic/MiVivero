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

        android.util.Log.e("FRAGMENT_ANALISIS", "ENTRO")

        // 🔴 Recycler
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerVentas)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        // 🔴 Adapter con navegación
        val adapter = VentasAlbumAdapter { albumId ->

            android.util.Log.e("NAV", "ENVIANDO albumId = $albumId")

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

            android.util.Log.e("ALBUM_LIST", "LLEGO OBSERVE")

            if (it.isEmpty()) {
                android.util.Log.e("ALBUM_LIST", "LISTA VACIA")
            }

            it.forEach { item ->
                android.util.Log.e(
                    "ALBUM_LIST",
                    "albumId=${item.albumId} nombre=${item.nombre}"
                )
            }

            adapter.submitList(it)
        }
    }
}