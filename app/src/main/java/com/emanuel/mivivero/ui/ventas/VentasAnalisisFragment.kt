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
import com.emanuel.mivivero.data.model.Insight
import com.emanuel.mivivero.data.model.RankingPlanta
import com.emanuel.mivivero.data.model.RentabilidadPlanta
import com.emanuel.mivivero.data.model.TipoInsight
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
                    else -> Color.parseColor("#2E7D32")
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


            chartRanking.setFitBars(true)
            chartRanking.data.barWidth = 0.9f

            chartRanking.setTouchEnabled(true)
            chartRanking.isHighlightPerTapEnabled = true
            chartRanking.isClickable = true
            chartRanking.isFocusable = true


            val xAxis = chartRanking.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true

            xAxis.position = XAxis.XAxisPosition.TOP
            xAxis.labelRotationAngle = -70f
            xAxis.textSize = 15f

            chartRanking.description.isEnabled = false
            chartRanking.legend.isEnabled = false
            chartRanking.extraBottomOffset = 15f
            chartRanking.extraTopOffset = 90f

            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true


            chartRanking.xAxis.setDrawGridLines(true)

            chartRanking.xAxis.gridLineWidth = 1f

            chartRanking.xAxis.axisMinimum = -0.5f
            chartRanking.xAxis.axisMaximum = topPlantas.size - 0.5f

            chartRanking.animateY(1000)
            chartRanking.invalidate()

            chartRanking.data.barWidth = 0.7f

            xAxis.setLabelCount(topPlantas.size, false)


            chartRanking.setOnChartGestureListener(object : com.github.mikephil.charting.listener.OnChartGestureListener {

                override fun onChartDoubleTapped(me: android.view.MotionEvent?) {

                    val highlight = chartRanking.highlighted?.firstOrNull() ?: return
                    val index = highlight.x.toInt()

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

                override fun onChartGestureStart(me: android.view.MotionEvent?, lastPerformedGesture: com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture?) {}
                override fun onChartGestureEnd(me: android.view.MotionEvent?, lastPerformedGesture: com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture?) {}
                override fun onChartLongPressed(me: android.view.MotionEvent?) {}
                override fun onChartSingleTapped(me: android.view.MotionEvent?) {}
                override fun onChartFling(me1: android.view.MotionEvent?, me2: android.view.MotionEvent?, velocityX: Float, velocityY: Float) {}
                override fun onChartScale(me: android.view.MotionEvent?, scaleX: Float, scaleY: Float) {}
                override fun onChartTranslate(me: android.view.MotionEvent?, dX: Float, dY: Float) {}
            })
        }

        viewModel.rentabilidad.observe(viewLifecycleOwner) { lista ->

            val topRentabilidad = lista.take(10)

            val entries = topRentabilidad.mapIndexed { index, item ->
                val valor = kotlin.math.ln(item.totalGanado + 1).toFloat()
                BarEntry(index.toFloat(), valor)
            }

            val labels = topRentabilidad.map { item ->
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
            data.barWidth = 0.7f

            chartRentabilidad.data = data

            val xAxis = chartRentabilidad.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelRotationAngle = -70f
            xAxis.textSize = 15f
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true
            xAxis.setLabelCount(topRentabilidad.size, false)

            chartRentabilidad.xAxis.axisMinimum = -0.5f
            chartRentabilidad.xAxis.axisMaximum = topRentabilidad.size - 0.5f


            chartRentabilidad.extraBottomOffset = 15f
            chartRentabilidad.extraTopOffset = 90f

            chartRentabilidad.setFitBars(true)
            chartRentabilidad.invalidate()




            chartRentabilidad.setOnChartGestureListener(object : com.github.mikephil.charting.listener.OnChartGestureListener {

                override fun onChartDoubleTapped(me: android.view.MotionEvent?) {

                    val highlight = chartRentabilidad.highlighted?.firstOrNull() ?: return
                    val index = highlight.x.toInt()

                    if (index >= topRentabilidad.size) return

                    val planta = topRentabilidad[index]

                    val bundle = Bundle().apply {
                        putLong("plantaId", planta.plantaId)
                    }

                    findNavController().navigate(
                        R.id.plantaDetalleFragment,
                        bundle
                    )
                }

                override fun onChartGestureStart(me: android.view.MotionEvent?, lastPerformedGesture: com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture?) {}
                override fun onChartGestureEnd(me: android.view.MotionEvent?, lastPerformedGesture: com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture?) {}
                override fun onChartLongPressed(me: android.view.MotionEvent?) {}
                override fun onChartSingleTapped(me: android.view.MotionEvent?) {}
                override fun onChartFling(me1: android.view.MotionEvent?, me2: android.view.MotionEvent?, velocityX: Float, velocityY: Float) {}
                override fun onChartScale(me: android.view.MotionEvent?, scaleX: Float, scaleY: Float) {}
                override fun onChartTranslate(me: android.view.MotionEvent?, dX: Float, dY: Float) {}
            })


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

        viewModel.obtenerDatosSemanas { actual, anterior, semana2 ->

            val insights = mutableListOf<Insight>()

            val variaciones = viewModel.calcularVariacion(actual, anterior)
            val predicciones = viewModel.predecirSemanaSiguiente(actual, anterior)
            val scores = viewModel.calcularScore(actual, rentabilidad, variaciones)
            val alertas = viewModel.generarAlertas(actual, rentabilidad)

            // 📈 VARIACIÓN
            variaciones.forEach {
                variaciones.forEach { variacion ->

                    when {
                        variacion.variacion > 20 -> insights.add(
                            Insight(
                                plantaId = variacion.plantaId,
                                nombre = variacion.nombre,
                                mensaje = "Creciendo fuerte (${variacion.variacion.toInt()}%)",
                                tipo = TipoInsight.CRECIMIENTO,
                                prioridad = 3
                            )
                        )

                        variacion.variacion < -20 -> insights.add(
                            Insight(
                                plantaId = variacion.plantaId,
                                nombre = variacion.nombre,
                                mensaje = "En caída (${variacion.variacion.toInt()}%)",
                                tipo = TipoInsight.CAIDA,
                                prioridad = 3
                            )
                        )
                    }
                }
            }

            // 🔥 SCORE
            scores.take(3).forEach { score ->

                insights.add(
                    Insight(
                        plantaId = score.plantaId,
                        nombre = score.nombre,
                        mensaje = "Es top para vender",
                        tipo = TipoInsight.TOP,
                        prioridad = 4
                    )
                )
            }

            // 🔮 PREDICCIÓN
            predicciones.forEach { texto ->

                insights.add(
                    Insight(
                        plantaId = -1, // temporal
                        nombre = texto,
                        mensaje = "",
                        tipo = TipoInsight.PREDICCION,
                        prioridad = 2
                    )
                )
            }

            // ⚠ ALERTAS
            alertas.forEach { texto ->

                insights.add(
                    Insight(
                        plantaId = -1, // ⚠ temporal
                        nombre = texto,
                        mensaje = "",
                        tipo = TipoInsight.ALERTA,
                        prioridad = 5
                    )
                )
            }



            requireActivity().runOnUiThread {

                containerInsights.removeAllViews()

                val insightsFinal = insights
                    .distinctBy { it.plantaId to it.tipo } // 🔥 clave real
                    .sortedByDescending { it.prioridad }

                if (insightsFinal.isEmpty()) {
                    val tv = TextView(requireContext())
                    tv.text = "Sin insights relevantes"
                    containerInsights.addView(tv)
                    return@runOnUiThread
                }

                insightsFinal.take(6).forEach { insight ->

                    val item = layoutInflater.inflate(R.layout.item_insight, containerInsights, false)

                    val tvTitulo = item.findViewById<TextView>(R.id.tvTitulo)
                    val tvNombre = item.findViewById<TextView>(R.id.tvNombre)
                    val tvDescripcion = item.findViewById<TextView>(R.id.tvDescripcion)

                    tvTitulo.text = when (insight.tipo) {
                        TipoInsight.CRECIMIENTO -> "Crecimiento"
                        TipoInsight.CAIDA -> "Caída"
                        TipoInsight.TOP -> "Top"
                        TipoInsight.ALERTA -> "Alerta"
                        TipoInsight.PREDICCION -> "Predicción"
                    }

                    tvNombre.text = insight.nombre
                    tvDescripcion.text = insight.mensaje

                    containerInsights.addView(item)
                }
            }

        }
    }

}