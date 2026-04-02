package com.emanuel.mivivero.ui.ventas

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.db.entity.VentaDetalle
import kotlinx.coroutines.launch

class VentasAnalisisFragment :
    Fragment(R.layout.fragment_ventas_analisis) {

    private val viewModel: VentasViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔴 PRUEBA VISUAL
        view.setBackgroundColor(Color.RED)

        // 🔴 LOG PARA SABER SI ENTRA AL FRAGMENT
        Log.e("VENTAS_ANALISIS", "FRAGMENT INICIADO")

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerVentas)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = VentasTablaAdapter()
        recycler.adapter = adapter

        lifecycleScope.launch {

            val lista = viewModel.obtenerVentasManual()

            Log.e("AAAA", "TOTAL = ${lista.size}")

            adapter.submitList(lista)
        }
    }
}
