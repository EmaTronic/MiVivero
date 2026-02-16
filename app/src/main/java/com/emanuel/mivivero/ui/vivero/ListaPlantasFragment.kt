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

import com.emanuel.mivivero.ui.albumes.AgregarPlantaAlbumDialog


class ListaPlantasFragment : Fragment(R.layout.fragment_lista_plantas) {

    private var albumId: Long = -1L
    private var _binding: FragmentListaPlantasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    private var listaOriginal: List<Planta> = emptyList()
    private lateinit var adapter: PlantaAdapter
    private var ordenAZActivo = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentListaPlantasBinding.bind(view)

        albumId = arguments?.getLong("albumId") ?: -1L



        binding.recyclerPlantas.layoutManager =
            LinearLayoutManager(requireContext())

        adapter = PlantaAdapter(
            plantas = mutableListOf(),
            modoAgregarAlbum = albumId != -1L,
            onAgregarPlantaAlbum = { planta ->
                mostrarDialogoCantidadPrecio(planta)
            }
        )



        binding.recyclerPlantas.adapter = adapter


        if (albumId != -1L) {
            viewModel.cargarPlantasParaAlbum(albumId)
        } else {
            viewModel.cargarPlantas()
        }


        viewModel.plantas.observe(viewLifecycleOwner) { lista ->
            listaOriginal = lista
            aplicarOrdenYFiltro("")
        }

        // 🔍 BUSCADOR
        binding.searchPlantas.setOnQueryTextListener(
            object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = true

                override fun onQueryTextChange(texto: String?): Boolean {
                    aplicarOrdenYFiltro(texto.orEmpty())
                    return true
                }
            }
        )

        // 🔠 BOTÓN A-Z
        binding.btnOrdenarAZ.setOnClickListener {
            ordenAZActivo = !ordenAZActivo
            binding.btnOrdenarAZ.text =
                if (ordenAZActivo) "ORIGINAL" else "A-Z"

            aplicarOrdenYFiltro(binding.searchPlantas.query.toString())
        }

        binding.fabAgregarPlanta.setOnClickListener {
            findNavController().navigate(R.id.crearPlantaFragment)
        }
    }

    private fun aplicarOrdenYFiltro(texto: String) {

        val filtro = texto.trim().lowercase()

        var lista = if (filtro.isEmpty()) {
            listaOriginal
        } else {
            listaOriginal.filter {
                it.familia.lowercase().contains(filtro) ||
                        (it.especie?.lowercase()?.contains(filtro) == true)
            }
        }

        if (ordenAZActivo) {
            lista = lista.sortedWith(
                compareBy<Planta> { it.familia.lowercase() }
                    .thenBy { it.especie?.lowercase() ?: "" }
            )
        }

        adapter.actualizarLista(lista)
    }

    private fun mostrarDialogoCantidadPrecio(planta: Planta) {
        AgregarPlantaAlbumDialog(planta)
            .show(parentFragmentManager, "AgregarPlantaAlbum")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}