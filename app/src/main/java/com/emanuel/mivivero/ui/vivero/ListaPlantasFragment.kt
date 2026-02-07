package com.emanuel.mivivero.ui.vivero

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.databinding.FragmentListaPlantasBinding
import com.emanuel.mivivero.ui.adapter.PlantaAdapter
import com.emanuel.mivivero.ui.albumes.AgregarPlantaAlbumDialog
import com.emanuel.mivivero.ui.viewmodel.ViveroViewModel

class ListaPlantasFragment : Fragment(R.layout.fragment_lista_plantas) {

    private var albumId: Long = -1L

    private var _binding: FragmentListaPlantasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentListaPlantasBinding.bind(view)
        albumId = arguments?.getLong("albumId") ?: -1L

        binding.recyclerPlantas.layoutManager =
            LinearLayoutManager(requireContext())

        // 🔥 CARGA SEGÚN CONTEXTO
        if (albumId != -1L) {
            viewModel.cargarPlantasParaAlbum()
        } else {
            viewModel.cargarPlantas()
        }

        viewModel.plantas.observe(viewLifecycleOwner) { lista ->
            binding.recyclerPlantas.adapter =
                PlantaAdapter(
                    plantas = lista,
                    modoAgregarAlbum = albumId != -1L,
                    onAgregarPlantaAlbum = { planta ->
                        mostrarDialogoCantidadPrecio(planta)
                    }
                )
        }

        binding.fabAgregarPlanta.setOnClickListener {
            findNavController().navigate(R.id.crearPlantaFragment)
        }
    }



    // 🔥 ESTA FUNCIÓN ES LA QUE FALTABA
    private fun mostrarDialogoCantidadPrecio(planta: Planta) {
        AgregarPlantaAlbumDialog(planta)
            .show(parentFragmentManager, "AgregarPlantaAlbum")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
