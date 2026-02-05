package com.emanuel.mivivero.ui.vivero


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentListaPlantasBinding
import com.emanuel.mivivero.ui.adapter.PlantaAdapter
import com.emanuel.mivivero.ui.viewmodel.ViveroViewModel

class ListaPlantasFragment : Fragment(R.layout.fragment_lista_plantas) {

    private var _binding: FragmentListaPlantasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentListaPlantasBinding.bind(view)

        binding.recyclerPlantas.layoutManager =
            LinearLayoutManager(requireContext())

        viewModel.plantas.observe(viewLifecycleOwner) { lista ->
            Log.d("PLANTAS", "Plantas = ${lista.size}")
            binding.recyclerPlantas.adapter = PlantaAdapter(lista)
        }

        binding.fabAgregarPlanta.setOnClickListener {
            findNavController().navigate(R.id.crearPlantaFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
