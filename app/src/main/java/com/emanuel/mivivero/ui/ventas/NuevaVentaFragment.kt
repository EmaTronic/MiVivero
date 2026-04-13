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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevaVentaFragment : Fragment(R.layout.fragment_nueva_venta) {

    private val viewModel: VentasViewModel by viewModels()

    private lateinit var autoPlanta: AutoCompleteTextView
    private lateinit var etCantidad: EditText
    private lateinit var etPrecio: EditText
    private lateinit var btnGuardar: Button

    private var listaPlantas: List<PlantaEntity> = emptyList()
    private var plantaSeleccionada: PlantaEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoPlanta = view.findViewById(R.id.autoPlanta)
        etCantidad = view.findViewById(R.id.etCantidad)
        etPrecio = view.findViewById(R.id.etPrecio)
        btnGuardar = view.findViewById(R.id.btnGuardarVenta)

        // 🔥 cargar plantas desde DB
        lifecycleScope.launch {

            listaPlantas = withContext(Dispatchers.IO) {
                viewModel.getPlantas() // función simple en ViewModel
            }

            val nombres = listaPlantas.map {
                "${it.familia} ${it.especie ?: ""}"
            }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nombres
            )

            autoPlanta.setAdapter(adapter)
        }

        // 🔥 detectar selección real
        autoPlanta.setOnItemClickListener { _, _, position, _ ->
            plantaSeleccionada = listaPlantas[position]
        }

        // 🔥 guardar venta
        btnGuardar.setOnClickListener {

            val cantidad = etCantidad.text.toString().toIntOrNull() ?: 0
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0

            if (plantaSeleccionada == null) {
                Toast.makeText(requireContext(), "Seleccioná una planta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cantidad <= 0) {
                Toast.makeText(requireContext(), "Cantidad inválida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {

                viewModel.insertVenta(
                    plantaId = plantaSeleccionada!!.id,
                    cantidad = cantidad,
                    precio = precio
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Venta guardada", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }
    }
}