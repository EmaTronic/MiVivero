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

    private val viewModel: AlbumesViewModel by viewModels()

    private var albumId: Long = -1

    private lateinit var recycler: RecyclerView
    private lateinit var tvTotal: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        albumId = arguments?.getLong("albumId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarVentas)

        recycler = view.findViewById(R.id.recyclerVentas)
        tvTotal = view.findViewById(R.id.tvTotalGanancia)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = VentasAdapter { total ->
            tvTotal.text = "Total: $ $total"
        }

        recycler.adapter = adapter

        // 🔴 OBSERVER VA AFUERA (una sola vez)
        viewModel.obtenerPlantasDelAlbumRaw(albumId)
            .observe(viewLifecycleOwner) { lista ->
                android.util.Log.e("VENTAS_PLANTAS", "size = ${lista.size}")
                adapter.submitList(lista)
            }

        // 🔴 BOTÓN SOLO GUARDA
        btnGuardar.setOnClickListener {

            android.util.Log.e("VENTAS_DEBUG", "CLICK GUARDAR")

            val ventas = adapter.obtenerVentas()

            android.util.Log.e("VENTAS_DEBUG", "MAP SIZE = ${ventas.size}")

            ventas.forEach { (plantaId, data) ->

                val cantidad = data.first
                val precio = data.second

                if (cantidad > 0) {

                    android.util.Log.e("VENTAS_DEBUG", "GUARDANDO $cantidad")

                    viewModel.registrarVenta(
                        plantaId,
                        albumId,
                        cantidad,
                        precio
                    )
                }
            }

            adapter.limpiarVentas()
            adapter.submitList(emptyList())
            // 🔴 FEEDBACK
            Toast.makeText(
                requireContext(),
                "Ventas guardadas",
                Toast.LENGTH_SHORT
            ).show()

        }
    }

}