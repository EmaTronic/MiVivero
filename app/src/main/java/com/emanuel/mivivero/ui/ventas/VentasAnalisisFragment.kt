package com.emanuel.mivivero.ui.ventas

import android.R.attr.top
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emanuel.mivivero.R
import com.emanuel.mivivero.data.model.AnalisisPlanta
import com.emanuel.mivivero.data.model.RankingPlanta
import com.emanuel.mivivero.data.model.RentabilidadPlanta
import com.emanuel.mivivero.data.model.VentaTiempo
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*

class VentasAnalisisFragment :
    Fragment(R.layout.fragment_ventas_analisis) {

    private val viewModel: VentasViewModel by viewModels()

    private var topPlantas: List<RankingPlanta> = emptyList()

    private var rankingList: List<RankingPlanta> = emptyList()
    private var rentabilidadList: List<RentabilidadPlanta> = emptyList()

    private var modoActual = "SEMANA"

    private lateinit var chartTiempo: LineChart


    private lateinit var containerInsights: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("FRAGMENT_ANALISIS", "ENTRO")

        val btnSemana = view.findViewById<Button>(R.id.btnSemana)
        val btnMes = view.findViewById<Button>(R.id.btnMes)
        val btnAnio = view.findViewById<Button>(R.id.btnAnio)


        val chartRanking = view.findViewById<BarChart>(R.id.chartRanking)
        val chartRentabilidad = view.findViewById<BarChart>(R.id.chartRentabilidad)






        //ANALISIS COMPARATIVOS

        containerInsights = view.findViewById(R.id.containerInsights)


        btnSemana.isEnabled = false   // 🔥 activo por defecto

        // 🔥 CARGA INICIAL (por defecto semana)
        viewModel.ventasSemana.observe(viewLifecycleOwner) { lista ->
            if (modoActual == "SEMANA") {
                cargarGraficoTiempo(chartTiempo, lista, modoActual)
            }
        }

        viewModel.ventasMes.observe(viewLifecycleOwner) { lista ->
            if (modoActual == "MES") {
                cargarGraficoTiempo(chartTiempo, lista, modoActual)
            }
        }

        viewModel.ventasAnio.observe(viewLifecycleOwner) { lista ->
            if (modoActual == "ANIO") {
                cargarGraficoTiempo(chartTiempo, lista, modoActual)
            }
        }

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

        chartTiempo = view.findViewById<LineChart>(R.id.chartTiempo)


        btnSemana.setOnClickListener {

            modoActual = "SEMANA"

            btnSemana.isEnabled = false
            btnMes.isEnabled = true
            btnAnio.isEnabled = true

            viewModel.ventasSemana.value?.let {
                cargarGraficoTiempo(chartTiempo, it, modoActual)
            }
        }

        btnMes.setOnClickListener {

            modoActual = "MES"

            btnSemana.isEnabled = true
            btnMes.isEnabled = false
            btnAnio.isEnabled = true

            viewModel.ventasMes.value?.let {
                cargarGraficoTiempo(chartTiempo, it, modoActual)
            }
        }

        btnAnio.setOnClickListener {

            modoActual = "ANIO"

            btnSemana.isEnabled = true
            btnMes.isEnabled = true
            btnAnio.isEnabled = false

            viewModel.ventasAnio.value?.let {
                cargarGraficoTiempo(chartTiempo, it, modoActual)
            }
        }


        chart.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        viewModel.ranking.observe(viewLifecycleOwner) { lista ->

            rankingList = lista

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

            val colors = topPlantas.mapIndexed { index, _ ->
                when (index) {
                    0 -> Color.parseColor("#FFD700")
                    1 -> Color.parseColor("#C0C0C0")
                    2 -> Color.parseColor("#CD7F32")
                    else -> Color.parseColor("#81D4FA")
                }
            }

            dataSet.setDrawValues(true)
            dataSet.valueTextSize = 10f
            dataSet.valueTextColor = Color.BLACK

            dataSet.highLightAlpha = 255
            dataSet.highLightColor = Color.BLACK

            dataSet.colors = colors

            val data = BarData(dataSet)

            chart.data = data
            chartRanking.data=data

            chartRanking.axisRight.isEnabled = false
            chartRanking.axisLeft.gridColor = Color.LTGRAY
            chartRanking.xAxis.setDrawGridLines(false)

            chartRanking.xAxis.axisMinimum = -0.5f
            chartRanking.xAxis.axisMaximum = topPlantas.size - 0.5f

            chartRanking.setFitBars(true)
            chartRanking.data.barWidth = 0.9f

            chartRanking.setTouchEnabled(true)
            chartRanking.isHighlightPerTapEnabled = true
            chartRanking.isClickable = true
            chartRanking.isFocusable = true


            val xAxis = chart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.TOP
            xAxis.labelRotationAngle = -70f
            xAxis.textSize = 15f

            chartRanking.description.isEnabled = false
            chartRanking.legend.isEnabled = false
            chartRanking.extraBottomOffset = 15f
            chartRanking.extraTopOffset = 90f

            chartRanking.setFitBars(true)
            chartRanking.animateY(1000)
            chartRanking.invalidate()





            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {

                override fun onValueSelected(e: Entry?, h: Highlight?) {

                    Log.e("TEST", "CLICK DETECTADO")

                    val index = h?.x?.toInt() ?: return

                    Log.e("INDEX", "index = $index size = ${topPlantas.size}")

                    if (index >= topPlantas.size) return

                    val planta = topPlantas[index]

                    Toast.makeText(
                        requireContext(),
                        "${planta.nombrePlanta}\nVendidas: ${planta.totalVendidas}",
                        Toast.LENGTH_SHORT
                    ).show()

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

        viewModel.rentabilidad.observe(viewLifecycleOwner) { lista ->

            val entries = lista.mapIndexed { index, item ->
                val valor = kotlin.math.ln(item.totalGanado + 1).toFloat()
                BarEntry(index.toFloat(), valor)
            }

            val labels = lista.map { item ->
                val partes = item.nombrePlanta.split(" ")
                if (partes.size >= 2) {
                    "${partes[0]} ${partes[1].first()}."
                } else {
                    item.nombrePlanta
                }
            }

            val dataSet = BarDataSet(entries, "Top rentabilidad")

            val colors = entries.mapIndexed { index, _ ->
                when (index) {
                    0 -> Color.parseColor("#FFD700") // 🥇 oro
                    1 -> Color.parseColor("#C0C0C0") // 🥈 plata
                    2 -> Color.parseColor("#CD7F32") // 🥉 bronce
                    else -> Color.parseColor("#2196F3") // 🔵 azul
                }
            }

            dataSet.colors = colors

            val data = BarData(dataSet)
            chartRentabilidad.data = data


            val xAxis = chartRentabilidad.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelRotationAngle = -70f
            xAxis.textSize = 15f

            chartRentabilidad.extraBottomOffset = 15f
            chartRentabilidad.extraTopOffset = 120f

            chartRentabilidad.axisRight.isEnabled = false
            chartRentabilidad.setFitBars(true)
            chartRentabilidad.data.barWidth = 0.9f


            chartRentabilidad.description.isEnabled = false
            chartRentabilidad.invalidate()


        }

        viewModel.rentabilidad.observe(viewLifecycleOwner) { lista ->
            rentabilidadList = lista
            analizar(rankingList, rentabilidadList)
        }


    }


    private fun cargarGraficoTiempo(
        chart: LineChart,
        lista: List<VentaTiempo>,
        modo: String
    ) {

        val resultado = mutableListOf<VentaTiempo>()

        val hoy = System.currentTimeMillis()

        val formatoDia = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

        when (modo) {

            // 🔵 SEMANA → últimos 7 días
            "SEMANA" -> {

                for (i in 6 downTo 0) {

                    val fecha = hoy - i * 24 * 60 * 60 * 1000
                    val dia = formatoDia.format(java.util.Date(fecha))

                    val venta = lista.find { it.periodo == dia }

                    resultado.add(
                        VentaTiempo(
                            periodo = dia,
                            total = venta?.total ?: 0.0
                        )
                    )
                }
            }

            // 🟢 MES → últimos 30 días
            "MES" -> {

                for (i in 29 downTo 0) {

                    val fecha = hoy - i * 24 * 60 * 60 * 1000
                    val dia = formatoDia.format(java.util.Date(fecha))

                    val venta = lista.find { it.periodo == dia }

                    resultado.add(
                        VentaTiempo(
                            periodo = dia,
                            total = venta?.total ?: 0.0
                        )
                    )
                }
            }

            // 🟣 AÑO → 12 meses
            "ANIO" -> {

                for (mes in 1..12) {

                    val mesStr = String.format("%02d", mes)

                    val venta = lista.find { it.periodo == mesStr }

                    resultado.add(
                        VentaTiempo(
                            periodo = mesStr,
                            total = venta?.total ?: 0.0
                        )
                    )
                }
            }
        }

        // 🔥 ENTRIES
        val entries = resultado.mapIndexed { index, item ->
            Entry(index.toFloat(), item.total.toFloat())
        }

        val dataSet = LineDataSet(entries, "Ventas")

        dataSet.color = Color.BLUE
        dataSet.setCircleColor(Color.BLUE)
        dataSet.valueTextSize = 10f

        val data = LineData(dataSet)

        chart.data = data

        // 🔥 LABELS
        val labels = resultado.map {
            if (modo == "ANIO") {
                when (it.periodo) {
                    "01" -> "Ene"
                    "02" -> "Feb"
                    "03" -> "Mar"
                    "04" -> "Abr"
                    "05" -> "May"
                    "06" -> "Jun"
                    "07" -> "Jul"
                    "08" -> "Ago"
                    "09" -> "Sep"
                    "10" -> "Oct"
                    "11" -> "Nov"
                    "12" -> "Dic"
                    else -> it.periodo
                }
            } else {
                it.periodo
            }
        }

        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelRotationAngle = -45f
        xAxis.labelCount = if (modo == "ANIO") 12 else 6


        chart.extraBottomOffset = 15f
        chart.extraTopOffset = 60f

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false

        chart.invalidate()
    }

    private fun analizar(
        ranking: List<RankingPlanta>,
        rentabilidad: List<RentabilidadPlanta>
    ) {

        Log.e("DEBUG", "ranking size = ${ranking.size}")
        Log.e("DEBUG", "rentabilidad size = ${rentabilidad.size}")

        ranking.forEach { r ->
            Log.e("RANK", "id=${r.plantaId} nombre=${r.nombrePlanta}")
        }

        rentabilidad.forEach { r ->
            Log.e("RENT", "id=${r.plantaId} nombre=${r.nombrePlanta}")
        }
        val resultado = ranking.mapNotNull { r ->
            val match = rentabilidad.find { it.plantaId == r.plantaId }

            match?.let {
                AnalisisPlanta(
                    nombre = r.nombrePlanta,
                    vendidas = r.totalVendidas,
                    ganancia = it.totalGanado
                )
            }
        }

        if (resultado.isEmpty()) {
            return
        }

        val maxVentas = resultado.maxOf { it.vendidas }
        val maxGanancia = resultado.maxOf { it.ganancia }

        val insights = mutableListOf<String>()

        resultado.forEach {

            val ratioVentas = it.vendidas.toFloat() / maxVentas
            val ratioGanancia = it.ganancia.toFloat() / maxGanancia

            val diferencia = ratioGanancia - ratioVentas

            if (diferencia < -0.15f) {
                insights.add("⚠ ${it.nombre} vende bien pero deja poca ganancia")
            }

            if (diferencia > 0.15f) {
                insights.add("💰 ${it.nombre} muy rentable, potenciar")
            }
            if (insights.isEmpty() && resultado.isNotEmpty()) {

                val mejor = resultado.maxByOrNull { it.ganancia }

                mejor?.let {
                    insights.add("💰 ${it.nombre} es la planta más rentable")
                }
            }
        }

        containerInsights.removeAllViews()

        if (insights.isEmpty()) {

            val textView = TextView(requireContext())
            textView.text = "Sin insights relevantes"
            containerInsights.addView(textView)

            return
        }


        insights.forEach { texto ->

            val item = layoutInflater.inflate(R.layout.item_insight, containerInsights, false)

            val tvTitulo = item.findViewById<TextView>(R.id.tvTitulo)
            val tvNombre = item.findViewById<TextView>(R.id.tvNombre)
            val tvDescripcion = item.findViewById<TextView>(R.id.tvDescripcion)

            when {
                texto.contains("⚠") -> {
                    tvTitulo.text = "⚠ Revisar"
                    item.setBackgroundColor(Color.parseColor("#FFCDD2"))
                }
                texto.contains("💰") -> {
                    tvTitulo.text = "💰 Oportunidad"
                    item.setBackgroundColor(Color.parseColor("#C8E6C9"))
                }
            }

            // dividir texto
            val partes = texto.split(" ", limit = 2)

            tvNombre.text = partes.getOrNull(0) ?: ""
            tvDescripcion.text = partes.getOrNull(1) ?: texto

            containerInsights.addView(item)
        }
    }

}