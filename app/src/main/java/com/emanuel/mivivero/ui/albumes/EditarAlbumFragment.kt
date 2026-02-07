package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentEditarAlbumBinding

class EditarAlbumFragment : Fragment(R.layout.fragment_editar_album) {

    private var _binding: FragmentEditarAlbumBinding? = null
    private val binding get() = _binding!!

    // VM que LEE álbum y plantas del álbum
    private val editarAlbumViewModel: EditarAlbumViewModel by viewModels()

    // VM que INSERTA plantas en album_planta
    private val albumesViewModel: AlbumesViewModel by activityViewModels()

    private var albumId: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentEditarAlbumBinding.bind(view)

        // ===== OBTENER ID =====
        albumId = arguments?.getLong("albumId") ?: -1L
        if (albumId == -1L) {
            findNavController().popBackStack()
            return
        }

        // 🔥 CLAVE: setear el álbum activo para insertar plantas
        albumesViewModel.albumActualId = albumId

        Log.d("EDITAR_ALBUM", "Editar álbum id=$albumId")

        // ===== RECYCLER =====
        binding.recyclerPlantasAlbum.layoutManager =
            LinearLayoutManager(requireContext())

        // ===== DATOS DEL ÁLBUM =====
        editarAlbumViewModel.obtenerAlbum(albumId)
            .observe(viewLifecycleOwner) { album ->
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

        // ===== PLANTAS DEL ÁLBUM =====
        editarAlbumViewModel.obtenerPlantasDelAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->
                binding.recyclerPlantasAlbum.adapter =
                    PlantasAlbumAdapter(lista) {}
            }

        // ===== BOTONES =====
        binding.btnAgregarPlantas.setOnClickListener {
            findNavController().navigate(
                R.id.action_editarAlbumFragment_to_listaPlantasFragment,
                Bundle().apply {
                    putLong("albumId", albumId)
                }
            )
        }

        binding.btnFinalizarAlbum.setOnClickListener {

            editarAlbumViewModel.finalizarAlbum(albumId)

            Toast.makeText(
                requireContext(),
                "Álbum finalizado",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(
                R.id.albumesFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.viveroFragment, false)
                    .build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
