package com.emanuel.mivivero.ui.vivero

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
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
import android.widget.ArrayAdapter
import com.emanuel.mivivero.ui.albumes.AgregarPlantaAlbumDialog
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged



class ListaPlantasFragment : Fragment(R.layout.fragment_lista_plantas) {



    private var albumId: Long = -1L
    private var _binding: FragmentListaPlantasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViveroViewModel by activityViewModels()

    private var listaOriginal: List<Planta> = emptyList()
    private lateinit var adapter: PlantaAdapter
    private var ordenAZActivo = false
    private var filtroLugarId: Int? = null

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
            aplicarOrdenYFiltro(binding.etBuscarPlantas.text.toString())
        }

        viewModel.lugares.observe(viewLifecycleOwner) { lugares ->

            binding.chipGroupLugares.removeAllViews()

            val opciones = mutableListOf("🌿 Mostrar todas")
            opciones.addAll(lugares.map { "${it.icono} ${it.nombre}" })

            opciones.forEachIndexed { index, texto ->

                val chip = com.google.android.material.chip.Chip(requireContext()).apply {

                    this.text = texto
                    isCheckable = true
                    textSize = 15f
                    setPadding(20,10,20,10)

                    setOnClickListener {

                        filtroLugarId = if (index == 0) null else lugares[index - 1].id
                        aplicarOrdenYFiltro(binding.etBuscarPlantas.text.toString())

                    }
                }

                binding.chipGroupLugares.addView(chip)
            }

            // seleccionar "mostrar todas"
            (binding.chipGroupLugares.getChildAt(0) as? com.google.android.material.chip.Chip)?.isChecked = true
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


        binding.etBuscarPlantas.doOnTextChanged { text, _, _, _ ->

            val query = text.toString()

            aplicarOrdenYFiltro(query)

            val cantidad = adapter.itemCount

            if (query.isNotEmpty()) {

                binding.tvResultadosBusqueda.visibility = View.VISIBLE
                binding.tvResultadosBusqueda.text =
                    if (cantidad == 1) "1 resultado"
                    else "$cantidad resultados"

                binding.etBuscarPlantas.hint = "Buscar en resultados"

                binding.scrollFiltros.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction { binding.scrollFiltros.visibility = View.GONE }

            } else {

                binding.tvResultadosBusqueda.visibility = View.GONE

                binding.etBuscarPlantas.hint = "Buscar plantas"

                binding.scrollFiltros.visibility = View.VISIBLE
                binding.scrollFiltros.animate().alpha(1f).setDuration(150)
            }

            binding.recyclerPlantas.scrollToPosition(0)
        }

        binding.etBuscarPlantas.setOnEditorActionListener { v, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val texto = binding.etBuscarPlantas.text.toString()

                aplicarOrdenYFiltro(texto)

                val imm = requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                imm.hideSoftInputFromWindow(v.windowToken, 0)

                binding.etBuscarPlantas.clearFocus()

                true
            } else {
                false
            }
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

            val empiezan = listaOriginal.filter {
                it.familia.lowercase().startsWith(filtro) ||
                        (it.especie?.lowercase()?.startsWith(filtro) == true)
            }

            val contienen = listaOriginal.filter {
                it.familia.lowercase().contains(filtro) ||
                        (it.especie?.lowercase()?.contains(filtro) == true)
            }

            (empiezan + contienen).distinct()
        }

        lista = filtroLugarId?.let { idLugar ->
            lista.filter { it.lugarId == idLugar }
        } ?: lista

        if (ordenAZActivo) {
            lista = lista.sortedWith(
                compareBy<Planta> { it.familia.lowercase() }
                    .thenBy { it.especie?.lowercase() ?: "" }
            )
        }

        adapter.queryActual = filtro
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