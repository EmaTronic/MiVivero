package com.emanuel.mivivero.ui.ventas

import android.R.attr.top
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.RankingPlanta
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.data.Entry


class VentasAnalisisFragment :
    Fragment(R.layout.fragment_ventas_analisis) {

    private val viewModel: VentasViewModel by viewModels()

    private var topPlantas: List<RankingPlanta> = emptyList()

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

        chart.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        viewModel.ranking.observe(viewLifecycleOwner) { lista ->

            topPlantas = lista.take(10)

            val entries = ArrayList<BarEntry>()

            topPlantas.forEachIndexed { index, item ->
                entries.add(BarEntry(index.toFloat(), item.totalVendidas.toFloat()))
            }

            Log.e("ENTRIES", entries.toString())

            val labels = topPlantas.map { item ->
                val partes = item.nombrePlanta.split(" ")
                if (partes.size >= 2) {
                    "${partes[0]} ${partes[1].first()}."
                } else {
                    item.nombrePlanta
                }
            }

            val dataSet = BarDataSet(entries, "Top 10 plantas")
            val data = BarData(dataSet)

            chart.data = data

            chart.xAxis.axisMinimum = -0.5f
            chart.xAxis.axisMaximum = topPlantas.size - 0.5f

            chart.setFitBars(true)
            chart.data.barWidth = 0.9f

            chart.setTouchEnabled(true)
            chart.isHighlightPerTapEnabled = true
            chart.isClickable = true
            chart.isFocusable = true


            val xAxis = chart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.TOP
            xAxis.labelRotationAngle = -70f
            xAxis.textSize = 15f

            chart.description.isEnabled = false
            chart.legend.isEnabled = false
            chart.extraBottomOffset = 15f
            chart.extraTopOffset = 120f

            chart.setFitBars(true)
            chart.animateY(800)
            chart.invalidate()



            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {

                override fun onValueSelected(e: Entry?, h: Highlight?) {

                    Log.e("TEST", "CLICK DETECTADO")

                    val index = h?.x?.toInt() ?: return

                    Log.e("INDEX", "index = $index size = ${topPlantas.size}")

                    if (index >= topPlantas.size) return

                    val planta = topPlantas[index]

                    val bundle = Bundle().apply {
                        putLong("plantaId", planta.plantaId)
                    }

                    findNavController().navigate(
                        R.id.plantaDetalleFragment,
                        bundle
                    )
                }

                override fun onNothingSelected() {}
            })
        }
    }
}