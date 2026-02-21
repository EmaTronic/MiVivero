package com.emanuel.mivivero.ui.vivero

import android.content.Context
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
import androidx.core.widget.addTextChangedListener
import android.view.inputmethod.InputMethodManager
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

        binding.root.setOnClickListener {
            binding.etBuscarPlantas.clearFocus()
            ocultarTeclado()
        }

        binding.recyclerPlantas.setOnTouchListener { _, _ ->
            binding.etBuscarPlantas.clearFocus()
            ocultarTeclado()
            false
        }

        // ================= PRIMERA VEZ =================

        val prefs = requireContext()
            .getSharedPreferences("mi_vivero_prefs", Context.MODE_PRIVATE)

        val primeraVez = prefs.getBoolean("primera_vez", true)

        if (primeraVez) {

            binding.layoutBienvenida.visibility = View.VISIBLE

            binding.tvTitulo.visibility = View.GONE
            binding.cardSearch.visibility = View.GONE
            binding.btnOrdenarAZ.visibility = View.GONE
            binding.recyclerPlantas.visibility = View.GONE
            binding.btnAgregarPlanta.visibility = View.GONE



        } else {

            binding.layoutBienvenida.visibility = View.GONE
        }

        // ================= BOTÓN BIENVENIDA =================

        binding.btnAgregarDesdeBienvenida.setOnClickListener {

            prefs.edit().putBoolean("primera_vez", false).apply()

            findNavController().navigate(R.id.crearPlantaFragment)
        }

        // ================= ARGUMENTOS =================

        albumId = arguments?.getLong("albumId") ?: -1L

        if (albumId != -1L) {
            binding.btnVolverEditarAlbum.visibility = View.VISIBLE
        } else {
            binding.btnVolverEditarAlbum.visibility = View.GONE
        }

        // ================= RECYCLER =================

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

        // ================= CARGA DE DATOS =================

        if (albumId != -1L) {
            viewModel.cargarPlantasParaAlbum(albumId)
        } else {
            viewModel.cargarPlantas()
        }

        viewModel.plantas.observe(viewLifecycleOwner) { lista ->
            listaOriginal = lista
            aplicarOrdenYFiltro("")
        }

        // ================= BUSCADOR =================

        // ================= BUSCADOR =================

        binding.etBuscarPlantas.addTextChangedListener { texto ->
            aplicarOrdenYFiltro(texto?.toString().orEmpty())
        }

        // ================= BOTÓN A-Z =================

        binding.btnOrdenarAZ.setOnClickListener {
            ordenAZActivo = !ordenAZActivo
            binding.btnOrdenarAZ.text =
                if (ordenAZActivo) "ORIGINAL" else "A-Z"

            aplicarOrdenYFiltro(binding.etBuscarPlantas.text.toString())
        }



        // ================= FAB =================

        binding.btnAgregarPlanta.setOnClickListener {
            findNavController().navigate(R.id.crearPlantaFragment)
        }

        binding.btnVolverEditarAlbum.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun ocultarTeclado() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(
            requireView().windowToken,
            0
        )
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