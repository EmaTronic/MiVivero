package com.emanuel.mivivero.ui.ventas

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import kotlinx.coroutines.launch

class VentasDetalleAlbumFragment :
    Fragment(R.layout.fragment_ventas_detalle_album) {

    private val viewModel: VentasViewModel by viewModels()

    private var albumId: Long = -1

    private lateinit var recycler: RecyclerView
    private lateinit var tvTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumId = arguments?.getLong("albumId") ?: -1

        android.util.Log.e("ALBUM_ID", "albumId = $albumId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        android.util.Log.e("ALBUM_UI", "albumId = $albumId")

        recycler = view.findViewById(R.id.recyclerVentas)
        tvTotal = view.findViewById(R.id.tvTotalGanancia)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        viewModel.debugVentas()

        // 🔴 ADAPTER COMPLETO
        val adapter = VentasTablaAdapter(

            onEditar = { venta ->

                val dialogView =
                    layoutInflater.inflate(R.layout.dialog_editar_venta, null)

                val etCantidad = dialogView.findViewById<EditText>(R.id.etCantidad)
                val etPrecio = dialogView.findViewById<EditText>(R.id.etPrecio)
                val etFecha = dialogView.findViewById<EditText>(R.id.etFecha)

                var fechaSeleccionada = venta.fecha

                val calendario = java.util.Calendar.getInstance()

                // 🔴 SETEAR VALORES PRIMERO
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

                            val formato = java.text.SimpleDateFormat(
                                "dd/MM/yyyy",
                                java.util.Locale.getDefault()
                            )

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
                            etCantidad.text.toString().toIntOrNull()
                                ?: return@setPositiveButton

                        val nuevoPrecio =
                            etPrecio.text.toString().toDoubleOrNull()
                                ?: return@setPositiveButton

                        val nuevaFecha = fechaSeleccionada

                        viewModel.editarVenta(
                            venta.id,
                            venta.plantaId,
                            venta.cantidad,
                            nuevaCantidad,
                            nuevoPrecio,
                            nuevaFecha
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

                // 🔴 LISTA DE PLANTAS DISPONIBLES
                lifecycleScope.launch {

                    val plantas =
                        viewModel.obtenerPlantasDisponibles(albumId)

                    if (plantas.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "No hay plantas disponibles",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    val nombres = plantas.map { it.nombre }.toTypedArray()

                    AlertDialog.Builder(requireContext())
                        .setTitle("Seleccionar planta")
                        .setItems(nombres) { _, index ->

                            val planta = plantas[index]

                            mostrarDialogoNuevaVenta(
                                planta.id,
                                planta.nombre
                            )
                        }
                        .show()
                }
            }
        )

        recycler.adapter = adapter

        // 🔴 LISTA DE VENTAS
        android.util.Log.e("VENTAS_UI", "ANTES DEL OBSERVE")

        viewModel.ventasPorAlbum(albumId)
            .observe(viewLifecycleOwner) { lista ->

                android.util.Log.e("VENTAS_UI", "ENTRO AL OBSERVE")

                if (lista == null) {
                    android.util.Log.e("VENTAS_UI", "LISTA NULL")
                    return@observe
                }

                android.util.Log.e("VENTAS_UI", "SIZE = ${lista.size}")

                lista.forEach {
                    android.util.Log.e("VENTAS_UI", "ITEM = ${it.nombrePlanta}")
                }

                adapter.submitList(lista)
            }

        // 🔴 TOTAL
        viewModel.totalPorAlbum(albumId)
            .observe(viewLifecycleOwner) {

                tvTotal.text = "Total: $ $it"
            }
    }

    // 🔴 NUEVA VENTA
    private fun mostrarDialogoNuevaVenta(
        plantaId: Long,
        nombre: String
    ) {

        val view =
            layoutInflater.inflate(R.layout.dialog_editar_venta, null)

        val etCantidad = view.findViewById<EditText>(R.id.etCantidad)
        val etPrecio = view.findViewById<EditText>(R.id.etPrecio)
        val etFecha = view.findViewById<EditText>(R.id.etFecha)

        var fechaSeleccionada = System.currentTimeMillis()

        val calendario = java.util.Calendar.getInstance()

        val formato = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        etFecha.setText(formato.format(java.util.Date(fechaSeleccionada)))

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

        // 🔴 NUEVO CONTROL REAL DEL DIALOG
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Nueva venta: $nombre")
            .setView(view)
            .setPositiveButton("Guardar", null) // ⚠️ IMPORTANTE
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        val btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        btnGuardar.setOnClickListener {

            val cantidadStr = etCantidad.text.toString()
            val precioStr = etPrecio.text.toString()

            android.util.Log.e("UI_DEBUG", "cantidadStr = $cantidadStr")
            android.util.Log.e("UI_DEBUG", "precioStr = $precioStr")

            val cantidad = cantidadStr.toIntOrNull()
            val precio = precioStr.toDoubleOrNull()

            if (cantidad == null || precio == null) {
                android.util.Log.e("UI_DEBUG", "DATOS INVALIDOS")
                Toast.makeText(requireContext(), "Datos inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            android.util.Log.e("UI_DEBUG", "LLAMANDO A registrarVenta")

            viewModel.registrarVenta(
                plantaId,
                albumId,
                cantidad,
                precio
            )

            dialog.dismiss()
        }
    }
}