package com.emanuel.mivivero.ui.ventas

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R

class ResumenFragment : Fragment(R.layout.fragment_resumen) {

    private val viewModel: VentasViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotal = view.findViewById<TextView>(R.id.tvTotalGeneral)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerResumen)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ResumenAdapter()
        recycler.adapter = adapter

        // 🔵 TOTAL GENERAL
        viewModel.totalGeneral.observe(viewLifecycleOwner) { total ->
            val valor = total ?: 0.0
            tvTotal.text = "$ $valor"
        }

        // 🔵 POR ÁLBUM
        viewModel.totalesPorAlbum.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
        }
    }
}