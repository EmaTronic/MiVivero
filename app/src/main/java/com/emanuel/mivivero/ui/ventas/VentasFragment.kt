package com.emanuel.mivivero.ui.ventas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentVentasBinding
import com.emanuel.mivivero.ui.albumes.AlbumesAdapter
import com.emanuel.mivivero.ui.albumes.AlbumesViewModel

class VentasFragment : Fragment(R.layout.fragment_ventas) {

    private var _binding: FragmentVentasBinding? = null
    private val binding get() = _binding!!

    private val albumesViewModel: AlbumesViewModel by viewModels()
    private val ventasViewModel: VentasViewModel by viewModels()

    private lateinit var adapter: AlbumesAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        _binding = FragmentVentasBinding.bind(view)


        val btnInsertarPlantas = view.findViewById<Button>(R.id.btnInsertarPlantas)
        val btnInsertarVentas = view.findViewById<Button>(R.id.btnInsertarVentas)


        configurarRecycler()

        // 🔴 BOTÓN ANALISIS
        val btnAnalisis = view.findViewById<Button>(R.id.btnIrAnalisis)

        btnAnalisis.setOnClickListener {
            android.util.Log.e("NAV", "CLICK ANALISIS")
            findNavController().navigate(R.id.ventasAnalisisFragment)
        }

        val btnNuevaVenta = view.findViewById<Button>(R.id.btnNuevaVenta)

        btnNuevaVenta.setOnClickListener {
            findNavController().navigate(R.id.nuevaVentaFragment)
        }

        val btnHistorial = view.findViewById<Button>(R.id.btnHistorial)

        btnHistorial.setOnClickListener {
            findNavController().navigate(R.id.historialVentasFragment)
        }

        albumesViewModel.getAlbumesPublicados()
            .observe(viewLifecycleOwner) { lista ->

                android.util.Log.e("VENTAS_DEBUG", "cantidad = ${lista.size}")

                adapter.actualizarLista(lista)
            }
        observarAlbumesPublicados()



        btnInsertarPlantas.setOnClickListener {

            android.util.Log.e("BTN_TEST", "CLICK PLANTAS")

            ventasViewModel.insertarPlantasTest(requireContext())

            Toast.makeText(requireContext(), "Plantas creadas", Toast.LENGTH_SHORT).show()
        }

        btnInsertarVentas.setOnClickListener {

            ventasViewModel.insertarVentasTestMasivo()

            Toast.makeText(requireContext(), "Ventas creadas", Toast.LENGTH_SHORT).show()
        }

    }

    // ================================
    // RECYCLER
    // ================================

    private fun configurarRecycler() {

        adapter = AlbumesAdapter(
            items = emptyList(),
            onClick = { album ->

                findNavController().navigate(
                    R.id.ventasDetalleAlbumFragment,
                    Bundle().apply {
                        putLong("albumId", album.id)
                    }
                )
            },
            onDeleteClick = {},
            onPublicarClick = {}
        )

        binding.recyclerVentas.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerVentas.adapter = adapter
    }


    // ================================
    // DATOS
    // ================================

    private fun observarAlbumesPublicados() {

        albumesViewModel.getAlbumesPublicados()
            .observe(viewLifecycleOwner) { lista ->

                android.util.Log.e("VENTAS_DEBUG", "cantidad = ${lista.size}")

                adapter.actualizarLista(lista)
            }
    }

    // ================================
    // CLEANUP
    // ================================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}