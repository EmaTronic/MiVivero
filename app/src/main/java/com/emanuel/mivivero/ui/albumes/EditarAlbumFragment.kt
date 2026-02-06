package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentEditarAlbumBinding

class EditarAlbumFragment : Fragment(R.layout.fragment_editar_album) {

    private var _binding: FragmentEditarAlbumBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditarAlbumViewModel by viewModels()

    private var albumId: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentEditarAlbumBinding.bind(view)

        // =====================
        // OBTENER ARGUMENTO
        // =====================
        albumId = arguments?.getLong("albumId") ?: -1L

        if (albumId == -1L) {
            Log.e("EDITAR_ALBUM", "albumId inválido")
            findNavController().popBackStack()
            return
        }

        Log.d("EDITAR_ALBUM", "Editar álbum id=$albumId")

        // =====================
        // RECYCLER
        // =====================
        binding.recyclerPlantasAlbum.layoutManager =
            LinearLayoutManager(requireContext())

        // =====================
        // OBSERVERS
        // =====================

        viewModel.obtenerAlbum(albumId).observe(viewLifecycleOwner) { album ->
            if (album == null) {
                Toast.makeText(
                    requireContext(),
                    "Álbum no encontrado",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
                return@observe
            }

            binding.txtNombreAlbum.text = album.nombre
            binding.txtEstadoAlbum.text = album.estado
        }

        viewModel.obtenerPlantasDelAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->

                binding.recyclerPlantasAlbum.adapter =
                    PlantasAlbumAdapter(lista) { plantaAlbum ->

                        Toast.makeText(
                            requireContext(),
                            "Planta ${plantaAlbum.nombre}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

        // =====================
        // BOTONES
        // =====================

        binding.btnAgregarPlantas.setOnClickListener {
            findNavController().navigate(
                R.id.action_editarAlbumFragment_to_listaPlantasFragment,
                Bundle().apply {
                    putLong("albumId", albumId)
                }
            )
        }


        binding.btnFinalizarAlbum.setOnClickListener {
            viewModel.finalizarAlbum(albumId)
            Toast.makeText(
                requireContext(),
                "Álbum finalizado",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
