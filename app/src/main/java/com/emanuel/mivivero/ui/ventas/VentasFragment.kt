package com.emanuel.mivivero.ui.ventas

import android.os.Bundle
import android.view.View
import android.widget.Button
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

    private val viewModel: AlbumesViewModel by viewModels()

    private lateinit var adapter: AlbumesAdapter




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        _binding = FragmentVentasBinding.bind(view)



        configurarRecycler()

        // 🔴 BOTÓN ANALISIS
        val btnAnalisis = view.findViewById<Button>(R.id.btnIrAnalisis)

        btnAnalisis.setOnClickListener {
            android.util.Log.e("NAV", "CLICK ANALISIS")
            findNavController().navigate(R.id.ventasAnalisisFragment)
        }


        viewModel.getAlbumesPublicados()
            .observe(viewLifecycleOwner) { lista ->

                android.util.Log.e("VENTAS_DEBUG", "cantidad = ${lista.size}")

                adapter.actualizarLista(lista)
            }
        observarAlbumesPublicados()
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

        viewModel.getAlbumesPublicados()
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