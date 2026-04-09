package com.emanuel.mivivero.ui.ventas

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.ControlStock
import com.emanuel.mivivero.data.model.ResumenPlanta
import com.emanuel.mivivero.ui.ventas.VentasTablaAdapter

class VentasTablaFragment :
    Fragment(R.layout.fragment_ventas_tabla) {

    private val viewModel: VentasViewModel by viewModels()

    private var albumId: Long = -1

    private lateinit var tvResumen: TextView
    private lateinit var tvAlerta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumId = arguments?.getLong("albumId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerVentasTabla)

        recycler.layoutManager = LinearLayoutManager(requireContext())


        val tvResumen = view.findViewById<TextView>(R.id.tvResumenVentas)
        val tvAlerta = view.findViewById<TextView>(R.id.tvAlertaStock)

        val adapter = VentasTablaAdapter(

            onEditar = { venta ->

                val dialogView =
                    layoutInflater.inflate(R.layout.dialog_editar_venta, null)

                val etCantidad = dialogView.findViewById<EditText>(R.id.etCantidad)
                val etPrecio = dialogView.findViewById<EditText>(R.id.etPrecio)
                val etFecha = dialogView.findViewById<EditText>(R.id.etFecha)

                var fechaSeleccionada = venta.fecha

                val calendario = java.util.Calendar.getInstance()

                etCantidad.setText(venta.cantidad.toString())
                etPrecio.setText(venta.precioUnitario.toString())

                val formato =
                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                etFecha.setText(formato.format(java.util.Date(venta.fecha)))

                etFecha.setOnClickListener {

                    val datePicker = android.app.DatePickerDialog(
                        requireContext(),
                        { _, year, month, day ->

                            calendario.set(year, month, day)
                            fechaSeleccionada = calendario.timeInMillis

                            etFecha.setText(formato.format(calendario.time))
                        },
                        calendario.get(java.util.Calendar.YEAR),
                        calendario.get(java.util.Calendar.MONTH),
                        calendario.get(java.util.Calendar.DAY_OF_MONTH)
                    )

                    datePicker.show()
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Editar venta")
                    .setView(dialogView)
                    .setPositiveButton("Guardar") { _, _ ->

                        val nuevaCantidad =
                            etCantidad.text.toString().toIntOrNull() ?: return@setPositiveButton

                        val nuevoPrecio =
                            etPrecio.text.toString().toDoubleOrNull() ?: return@setPositiveButton

                        viewModel.editarVenta(
                            venta.id,
                            venta.plantaId,
                            venta.cantidad,
                            nuevaCantidad,
                            nuevoPrecio,
                            fechaSeleccionada
                        )
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },

            onEliminar = { venta ->
                viewModel.eliminarVenta(
                    venta.id,
                    venta.plantaId,
                    venta.cantidad
                )
            },

            onAgregarClick = {
                // opcional → podés no usarlo
            }
        )

        recycler.adapter = adapter

        viewModel.ventasPorAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->
                adapter.submitList(lista)
            }


        viewModel.resumenPorPlanta(albumId)
            .observe(viewLifecycleOwner) { lista ->

                val resumen = lista.joinToString("\n") {
                    "${it.nombrePlanta}: ${it.totalVendidas} ventas"
                }

                android.widget.Toast.makeText(
                    requireContext(),
                    "Resumen:\n$resumen",
                    android.widget.Toast.LENGTH_LONG
                ).show()

            }


        viewModel.controlStock(albumId)
            .observe(viewLifecycleOwner) { lista ->

                val alertas = lista.filter { item ->
                    item.vendidas > item.stockActual
                }

                if (alertas.isNotEmpty()) {

                    val mensaje = alertas.joinToString("\n") { item ->
                        "⚠ ${item.nombrePlanta} sin stock (vendidas ${item.vendidas})"
                    }

                    Toast.makeText(
                        requireContext(),
                        mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


        viewModel.resumenPorPlanta(albumId)
            .observe(viewLifecycleOwner) { lista: List<ResumenPlanta> ->

                val texto = lista.joinToString("\n") {
                    "${it.nombrePlanta}: ${it.totalVendidas}"
                }

                tvResumen.text = texto
            }

        viewModel.controlStock(albumId)
            .observe(viewLifecycleOwner) { lista: List<ControlStock> ->

                val alertas = lista.filter {
                    it.vendidas > it.stockActual
                }

                if (alertas.isNotEmpty()) {

                    val texto = alertas.joinToString("\n") { item ->

                        val diferencia = item.stockActual - item.vendidas

                        "⚠ ${item.nombrePlanta} sin stock. Debes: ($diferencia)"
                    }

                    tvAlerta.visibility = View.VISIBLE
                    tvAlerta.text = texto

                } else {
                    tvAlerta.visibility = View.GONE
                }
            }
    }
}
