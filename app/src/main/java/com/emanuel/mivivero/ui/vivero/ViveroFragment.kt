package com.emanuel.mivivero.ui.vivero

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import kotlinx.coroutines.launch
import com.emanuel.mivivero.ui.vivero.PlantaAdapter
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch




class ViveroFragment : Fragment(R.layout.fragment_vivero) {

    private val viewModel: ViveroViewModel by viewModels {
        ViveroViewModelFactory(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerPlantas)
        val adapter = PlantaAdapter { planta ->
            val bundle = Bundle().apply {
                putLong("plantaId", planta.id)
            }
            findNavController().navigate(
                R.id.plantaDetalleFragment,
                bundle
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.plantas.collect { lista ->
                    adapter.submitList(lista)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.plantas.collect { lista ->
                    adapter.submitList(lista)
                }
            }
        }


    }
}