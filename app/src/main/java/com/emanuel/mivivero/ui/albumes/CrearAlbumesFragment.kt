package com.emanuel.mivivero.ui.albumes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

            if (albumId != null) {

                val bundle = Bundle().apply {
                    putLong("albumId", albumId)
                }

                findNavController().navigate(
                    R.id.action_crearAlbumesFragment_to_editarAlbumFragment,
                    bundle,
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(
                            R.id.crearAlbumesFragment,
                            true   // 🔥 elimina Crear del back stack
                        )
                        .build()
                )

                viewModel.limpiarAlbumCreado()
            }
        }


        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->

            binding.btnContinuar.isEnabled = !cargando

            if (cargando) {
                binding.btnContinuar.text = "Creando..."
                binding.btnContinuar.alpha = 0.7f
            } else {
                binding.btnContinuar.text = "Continuar"
                binding.btnContinuar.alpha = 1f
            }
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
