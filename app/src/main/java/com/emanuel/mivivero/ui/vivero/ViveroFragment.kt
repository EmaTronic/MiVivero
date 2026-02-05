package com.emanuel.mivivero.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentViveroBinding
import com.emanuel.mivivero.ui.adapter.PlantaAdapter
import com.emanuel.mivivero.ui.viewmodel.ViveroViewModel

class ViveroFragment : Fragment(R.layout.fragment_vivero) {

    private var _binding: FragmentViveroBinding? = null
    private val binding get() = _binding!!

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
