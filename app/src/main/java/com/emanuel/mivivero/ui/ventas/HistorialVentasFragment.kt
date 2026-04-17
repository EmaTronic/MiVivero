package com.emanuel.mivivero.ui.ventas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.VentaHistorial
import java.io.File
import java.io.FileOutputStream

class HistorialVentasFragment : Fragment(R.layout.fragment_historial_ventas) {

    private val viewModel: VentasViewModel by viewModels()

    private lateinit var recycler: RecyclerView
    private lateinit var btnExportar: Button

    private val adapter = VentasHistorialAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerVentas)
        btnExportar = view.findViewById(R.id.btnExportar)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter


// 🔵 carga automática al entrar al historial
        viewModel.ventasHistorial.observe(viewLifecycleOwner) {
            Log.e("HISTORIAL", "size = ${it.size}")
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