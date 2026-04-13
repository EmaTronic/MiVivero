package com.emanuel.mivivero.ui.ventas

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.local.entity.PlantaEntity
import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.data.model.VentaTemp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevaVentaFragment : Fragment(R.layout.fragment_nueva_venta) {

    private val viewModel: VentasViewModel by viewModels()

    private lateinit var autoPlanta: AutoCompleteTextView
    private lateinit var etCantidad: EditText
    private lateinit var etPrecio: EditText
    private lateinit var btnGuardar: Button

    private lateinit var tvResumen: TextView

    private lateinit var btnAgregar: Button
    private var listaPlantas: List<PlantaEntity> = emptyList()
    private var plantaSeleccionada: PlantaEntity? = null

    private val listaVenta = mutableListOf<VentaTemp>()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoPlanta = view.findViewById(R.id.autoPlanta)
        etCantidad = view.findViewById(R.id.etCantidad)
        etPrecio = view.findViewById(R.id.etPrecio)
        btnGuardar = view.findViewById(R.id.btnGuardarVenta)


        tvResumen = view.findViewById(R.id.tvResumen)
        btnAgregar = view.findViewById(R.id.btnAgregar)


        // 🔥 cargar plantas desde DB
        lifecycleScope.launch {

            listaPlantas = withContext(Dispatchers.IO) {
                viewModel.getPlantas() // función simple en ViewModel
            }

            val nombres = listaPlantas
                .map { "${it.familia} ${it.especie ?: ""}" }
                .sorted()

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nombres
            )

            autoPlanta.setAdapter(adapter)

// 🔥 CLAVE
            autoPlanta.threshold = 0

            autoPlanta.setOnClickListener {
                autoPlanta.showDropDown()
            }

            autoPlanta.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    autoPlanta.showDropDown()
                }
            }

            autoPlanta.setAdapter(adapter)
        }

        // 🔥 detectar selección real
        autoPlanta.setOnItemClickListener { _, _, position, _ ->
            plantaSeleccionada = listaPlantas[position]
        }



        btnAgregar.setOnClickListener {

            val cantidad = etCantidad.text.toString().toIntOrNull() ?: 0
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0

            val planta = plantaSeleccionada ?: return@setOnClickListener

            listaVenta.add(
                VentaTemp(planta, cantidad, precio)
            )

            Toast.makeText(requireContext(), "Agregado", Toast.LENGTH_SHORT).show()

            etCantidad.text.clear()
            etPrecio.text.clear()

            actualizarResumen()
        }


        // 🔥 guardar venta
        btnGuardar.setOnClickListener {

            if (listaVenta.isEmpty()) {
                Toast.makeText(requireContext(), "No hay ventas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {

                listaVenta.forEach {

                    viewModel.insertVenta(
                        plantaId = it.planta.id,
                        cantidad = it.cantidad,
                        precio = it.precio
                    )
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Venta guardada", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }


    }


    fun actualizarResumen() {

        val texto = listaVenta.joinToString("\n") {
            "${it.planta.familia} x${it.cantidad}"
        }

        tvResumen.text = texto
    }
}