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
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val user = auth.currentUser

// 🔴 NO LOGUEADO
            if (user == null) {

                com.google.android.material.snackbar.Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    "Tenés que iniciar sesión para crear un álbum",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).setAction("Ingresar") {
                    findNavController().navigate(R.id.loginFragment)
                }.show()

                return@setOnClickListener
            }

// 🔴 NO VERIFICADO
            user.reload().addOnSuccessListener {

                if (!user.isEmailVerified) {

                    com.google.android.material.snackbar.Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Tenés que verificar tu correo antes de crear un álbum",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).setAction("Verificar") {
                        findNavController().navigate(R.id.verificarEmailFragment)
                    }.show()

                    return@addOnSuccessListener
                }

                // ✅ VALIDACIONES NORMALES
                val nombre = binding.etNombreAlbum.text.toString().trim()
                val obs = binding.etObservacionesAlbum.text.toString().trim()

                if (nombre.isEmpty()) {
                    binding.etNombreAlbum.error = "El nombre es obligatorio"
                    return@addOnSuccessListener
                }

                // ✅ RECIÉN ACÁ CREÁS
                viewModel.crearAlbum(nombre, obs)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
