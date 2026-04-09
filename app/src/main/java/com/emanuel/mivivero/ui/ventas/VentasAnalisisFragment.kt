package com.emanuel.mivivero.ui.ventas

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.charts.BarChart

class VentasAnalisisFragment :
    Fragment(R.layout.fragment_ventas_analisis) {

    private val viewModel: VentasViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("FRAGMENT_ANALISIS", "ENTRO")

        // =========================
        // 🔴 RECYCLER ALBUMES
        // =========================
        val recyclerAlbumes = view.findViewById<RecyclerView>(R.id.recyclerVentas)

        recyclerAlbumes.layoutManager = LinearLayoutManager(requireContext())

        val adapterAlbumes = VentasAlbumAdapter { albumId ->

            Log.e("NAV", "ENVIANDO albumId = $albumId")

            // navegación (si la tenías)
        }

        recyclerAlbumes.adapter = adapterAlbumes

        // =========================
        // 🔵 RECYCLER RANKING
        // =========================
        val recyclerRanking = view.findViewById<RecyclerView>(R.id.recyclerRanking)

        recyclerRanking.layoutManager = LinearLayoutManager(requireContext())

        val rankingAdapter = RankingAdapter()

        recyclerRanking.adapter = rankingAdapter

        // =========================
        // 🔴 OBSERVE ALBUMES
        // =========================
        viewModel.resumenAlbumes.observe(viewLifecycleOwner) {

            Log.e("ALBUM_LIST", "SIZE = ${it.size}")

            it.forEach { item ->
                Log.e(
                    "ALBUM_LIST",
                    "albumId=${item.albumId} nombre=${item.nombre}"
                )
            }

            adapterAlbumes.submitList(it)
        }

        // =========================
        // 🔵 OBSERVE RANKING
        // =========================
        val chart = view.findViewById<BarChart>(R.id.chartRanking)

        viewModel.ranking.observe(viewLifecycleOwner) { lista ->

            val top = lista.take(10) // 🔥 TOP 10

            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()

            top.forEachIndexed { index, item ->
                entries.add(BarEntry(index.toFloat(), item.totalVendidas.toFloat()))
                labels.add(item.nombrePlanta)
            }

            val dataSet = BarDataSet(entries, "Top 10 plantas")
            val data = BarData(dataSet)

            chart.data = data

            val xAxis = chart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            xAxis.labelRotationAngle = -45f

            chart.description.isEnabled = false
            chart.legend.isEnabled = false

            chart.setFitBars(true)
            chart.animateY(800)

            chart.invalidate()
        }
    }
}