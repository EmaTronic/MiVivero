package com.emanuel.mivivero.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentViveroBinding
import com.emanuel.mivivero.ui.adapter.PlantaAdapter
import com.emanuel.mivivero.ui.viewmodel.ViveroViewModel

class ViveroFragment : Fragment(R.layout.fragment_vivero) {

    private val viewModel: ViveroViewModel by viewModels()
    private var _binding: FragmentViveroBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentViveroBinding.bind(view)

        binding.recyclerVivero.layoutManager =
            LinearLayoutManager(requireContext())


        viewModel.plantas.observe(viewLifecycleOwner) { lista ->
            binding.recyclerVivero.adapter = PlantaAdapter(lista)
        }

        viewModel.cargarPlantas()


    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
