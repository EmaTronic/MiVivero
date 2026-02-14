package com.emanuel.mivivero.ui.vivero

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentViveroBinding

import com.emanuel.mivivero.ui.auth.RegistroViewModel

import kotlinx.coroutines.launch

class ViveroFragment : Fragment(R.layout.fragment_vivero) {

    private var _binding: FragmentViveroBinding? = null
    private val binding get() = _binding!!

    private val registroViewModel: RegistroViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentViveroBinding.bind(view)

        binding.btnVerPlantas.setOnClickListener {
            findNavController().navigate(
                R.id.action_viveroFragment_to_listaPlantasFragment
            )
        }

        binding.btnCrearPlanta.setOnClickListener {
            findNavController().navigate(R.id.crearPlantaFragment)
        }

        binding.btnVerAlbumes.setOnClickListener {
            findNavController().navigate(
                R.id.action_viveroFragment_to_albumesFragment
            )
        }

        binding.btnCrearAlbum.setOnClickListener {
            findNavController().navigate(
                R.id.action_viveroFragment_to_crearAlbumesFragment
            )
        }


        binding.btnRegistroUsuario.setOnClickListener {
            findNavController().navigate(R.id.registroUsuarioFragment)
        }


        binding.btnUsuario.setOnClickListener {
            findNavController().navigate(R.id.registroUsuarioFragment)
        }


        viewLifecycleOwner.lifecycleScope.launch {
            val registrado = registroViewModel.usuarioRegistrado()
            binding.btnRegistroUsuario.visibility =
                if (registrado) View.GONE else View.VISIBLE
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
