package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.AlbumConCantidad
import com.emanuel.mivivero.databinding.FragmentAlbumesBinding

class AlbumesFragment : Fragment(R.layout.fragment_albumes) {

    private var _binding: FragmentAlbumesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlbumesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumesBinding.bind(view)

        configurarRecycler()
        observarAlbumes()
        configurarBotones()



    }

    private fun configurarRecycler() {
        binding.recyclerAlbumes.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun observarAlbumes() {
        viewModel.albumes.observe(viewLifecycleOwner) { lista ->
            binding.recyclerAlbumes.adapter =
                AlbumesAdapter(
                    lista,
                    onClick = { album ->
                        findNavController().navigate(
                            R.id.albumDetalleFragment,
                            Bundle().apply {
                                putLong("albumId", album.id)
                            }
                        )
                    },
                    onDeleteClick = { album ->
                        mostrarConfirmacionEliminar(album)
                    }
                )

        }


    }

    private fun mostrarConfirmacionEliminar(album: AlbumConCantidad) {

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar álbum")
            .setMessage("¿Eliminar '${album.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarAlbum(album.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun configurarBotones() {
        binding.fabCrearAlbum.setOnClickListener {
            findNavController().navigate(
                R.id.action_albumesFragment_to_crearAlbumesFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
