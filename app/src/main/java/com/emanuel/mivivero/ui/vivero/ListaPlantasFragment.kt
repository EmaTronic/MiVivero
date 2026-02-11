package com.emanuel.mivivero.ui.vivero

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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

    private lateinit var adapter: PlantaAdapter
    private var listaOriginal: List<Planta> = emptyList()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 1. Inicializar binding PRIMERO
        _binding = FragmentListaPlantasBinding.bind(view)

        albumId = arguments?.getLong("albumId") ?: -1L


        // ======================
        // Recycler
        // ======================

        binding.recyclerPlantas.layoutManager =
            LinearLayoutManager(requireContext())

        adapter = PlantaAdapter(
            plantas = emptyList(),
            modoAgregarAlbum = albumId != -1L,
            onAgregarPlantaAlbum = { planta ->
                mostrarDialogoCantidadPrecio(planta)
            }
        )

        binding.recyclerPlantas.adapter = adapter

        // ======================
        // Carga de datos
        // ======================

        if (albumId != -1L) {
            viewModel.cargarPlantasParaAlbum()
        } else {
            viewModel.cargarPlantas()
        }

        viewModel.plantas.observe(viewLifecycleOwner) { lista ->
            listaOriginal = lista
            adapter.actualizarLista(lista)
        }

        // ======================
        // Buscador
        // ======================

        binding.searchPlantas.setOnQueryTextListener(
            object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean = true

                override fun onQueryTextChange(texto: String?): Boolean {
                    filtrarPlantas(texto.orEmpty())
                    return true
                }
            }
        )

        // ======================
        // FAB
        // ======================

        binding.fabAgregarPlanta.setOnClickListener {
            findNavController().navigate(R.id.crearPlantaFragment)
        }
    }


    /* ======================
       FILTRADO
       ====================== */
    private fun filtrarPlantas(texto: String) {
        val filtro = texto.trim().lowercase()

        val filtradas =
            if (filtro.isEmpty()) {
                listaOriginal
            } else {
                listaOriginal.filter { planta ->
                    planta.familia.lowercase().contains(filtro) ||
                            (planta.especie?.lowercase()?.contains(filtro) == true)
                }
            }

        adapter.actualizarLista(filtradas)
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
