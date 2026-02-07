package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentCrearAlbumesBinding

class CrearAlbumesFragment : Fragment(R.layout.fragment_crear_albumes) {

    private var _binding: FragmentCrearAlbumesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CrearAlbumesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCrearAlbumesBinding.bind(view)

        // =====================
        // OBSERVER ÁLBUM CREADO
        // =====================
        viewModel.albumCreadoId.observe(viewLifecycleOwner) { albumId ->
            findNavController().navigate(
                R.id.action_crearAlbumesFragment_to_editarAlbumFragment,
                Bundle().apply {
                    putLong("albumId", albumId)
                }
            )
        }

        // =====================
        // BOTÓN CONTINUAR
        // =====================
        binding.btnContinuar.setOnClickListener {
            val nombre = binding.etNombreAlbum.text.toString().trim()
            val obs = binding.etObservacionesAlbum.text.toString().trim()

            if (nombre.isEmpty()) {
                binding.etNombreAlbum.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            viewModel.crearAlbum(nombre, obs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
