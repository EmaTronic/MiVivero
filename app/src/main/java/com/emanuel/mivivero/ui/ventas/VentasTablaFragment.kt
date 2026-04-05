package com.emanuel.mivivero.ui.ventas

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.ui.ventas.VentasTablaAdapter

class VentasTablaFragment :
    Fragment(R.layout.fragment_ventas_tabla) {

    private val viewModel: VentasViewModel by viewModels()

    private var albumId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumId = arguments?.getLong("albumId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerVentasTabla)

        recycler.layoutManager = LinearLayoutManager(requireContext())

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

                val formato = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
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
    }
}