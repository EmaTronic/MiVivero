package com.emanuel.mivivero.ui.ventas

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
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

        // 🔵 ver ventas
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
        }
    }

    // 🔥 FUNCIÓN EXPORTAR
    private fun exportarCSV(context: Context, lista: List<VentaHistorial>) {

        val file = File(context.getExternalFilesDir(null), "ventas.csv")

        file.bufferedWriter().use { writer ->

            writer.write("Planta,Cantidad,Precio,Total\n")

            lista.forEach {

                val nombre = "${it.familia} ${it.especie ?: ""}"
                val total = it.cantidad * it.precioUnitario

                writer.write("$nombre,${it.cantidad},${it.precioUnitario},$total\n")
            }
        }
    }




}