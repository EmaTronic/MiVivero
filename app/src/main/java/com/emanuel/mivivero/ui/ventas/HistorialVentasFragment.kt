package com.emanuel.mivivero.ui.ventas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.VentaHistorial
import java.io.File
import android.widget.AutoCompleteTextView
import androidx.core.widget.addTextChangedListener

class HistorialVentasFragment : Fragment(R.layout.fragment_historial_ventas) {

    private val viewModel: VentasViewModel by viewModels()

    private lateinit var recycler: RecyclerView
    private lateinit var btnExportar: Button

    private val adapter = VentasHistorialAdapter()

    private lateinit var tvResumen: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerVentas)
        btnExportar = view.findViewById(R.id.btnExportar)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        tvResumen = view.findViewById(R.id.tvResumen)

        viewModel.totalFiltrado.observe(viewLifecycleOwner) { total ->
            val cant = viewModel.cantidadResultados.value ?: 0
            tvResumen.text = "Mostrando $cant - $${"%.0f".format(total)}"
        }


        view.findViewById<Button>(R.id.btnHoy).setOnClickListener {
            viewModel.setPeriodo(VentasViewModel.PeriodoFiltro.HOY)
        }

        view.findViewById<Button>(R.id.btnSemana).setOnClickListener {
            viewModel.setPeriodo(VentasViewModel.PeriodoFiltro.SIETE_DIAS)
        }

        view.findViewById<Button>(R.id.btnMes).setOnClickListener {
            viewModel.setPeriodo(VentasViewModel.PeriodoFiltro.MES)
        }

        view.findViewById<Button>(R.id.btnTodo).setOnClickListener {
            viewModel.setPeriodo(VentasViewModel.PeriodoFiltro.TODO)
        }


        view.findViewById<Button>(R.id.btnGanancia).setOnClickListener {
            viewModel.setOrden(VentasViewModel.Orden.GANANCIA)
        }

        view.findViewById<Button>(R.id.btnAZ).setOnClickListener {
            viewModel.setOrden(VentasViewModel.Orden.AZ)
        }

        val etBuscar = view.findViewById<AutoCompleteTextView>(R.id.etBuscar)

        etBuscar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setBusqueda(s.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

// 🔵 carga automática al entrar al historial
        viewModel.ventasFiltradas.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // 🔴 exporta

// 🔴 exportar
        btnExportar.setOnClickListener {

            val lista = viewModel.ventasHistorial.value ?: return@setOnClickListener

            exportarCSV(requireContext(), lista)

            Toast.makeText(requireContext(), "CSV generado", Toast.LENGTH_SHORT).show()

            val file = File(requireContext().getExternalFilesDir(null), "ventas.csv")

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/csv"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(Intent.createChooser(intent, "Compartir CSV"))
        }
    }

    // 🔥 FUNCIÓN EXPORTAR
    private fun exportarCSV(context: Context, lista: List<VentaHistorial>) {

        val file = File(context.getExternalFilesDir(null), "ventas.csv")

        file.bufferedWriter().use { writer ->

            val fecha = java.text.SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            writer.write("Generado por App Mi Vivero\n")
            writer.write("Fecha de generación: $fecha\n\n")

            writer.write("Ranking;Planta;Cantidad Total;Total $;Porcentaje\n")

            val agrupado = lista
                .groupBy { "${it.familia} ${it.especie ?: ""}" }
                .map { (nombre, ventas) ->

                    val cantidadTotal = ventas.sumOf { it.cantidad }
                    val totalDinero = ventas.sumOf { it.cantidad * it.precioUnitario }

                    Triple(nombre, cantidadTotal, totalDinero)
                }
                .sortedByDescending { it.third }

            val totalGeneral = agrupado.sumOf { it.third }

            agrupado.forEachIndexed { index, (nombre, cantidad, total) ->

                val porcentaje = if (totalGeneral > 0)
                    (total / totalGeneral) * 100 else 0.0

                writer.write(
                    "${index + 1};$nombre;$cantidad;${"%.0f".format(total)};${"%.1f".format(porcentaje)}%\n"
                )
            }

            writer.write("\nTOTAL GENERAL;;;${
                "%.0f".format(totalGeneral)
            }\n")
        }
    }
}