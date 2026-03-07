package com.emanuel.mivivero.ui.lugares

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.databinding.FragmentLugaresBinding
import com.emanuel.mivivero.ui.vivero.ViveroViewModel

class LugaresFragment : Fragment(R.layout.fragment_lugares) {

    private var _binding: FragmentLugaresBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLugaresBinding.bind(view)

        val adapter = LugaresAdapter(
            onEditar = { AgregarLugarDialog.nuevaInstancia(it).show(parentFragmentManager, "EditarLugarDialog") },
            onEliminar = { viewModel.eliminarLugar(it) }
        )

        binding.recyclerLugares.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLugares.adapter = adapter

        binding.btnAgregarLugar.setOnClickListener {
            AgregarLugarDialog.nuevaInstancia().show(parentFragmentManager, "AgregarLugarDialog")
        }

        viewModel.lugaresConConteo.observe(viewLifecycleOwner) { lugares ->
            adapter.submitList(lugares)
        }

        viewModel.mensajeLugares.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje.isNotBlank()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
