package com.emanuel.mivivero.ui.ventas

import android.R.attr.id
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.db.entity.VentaDetalle
import com.emanuel.mivivero.data.db.entity.VentaEntity
import com.emanuel.mivivero.data.local.AppDatabase
import com.emanuel.mivivero.data.local.entity.PlantaEntity
import com.emanuel.mivivero.data.model.Planta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.emanuel.mivivero.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class VentasViewModel(application: Application)
    : AndroidViewModel(application) {


    private var listaPlantas: List<PlantaEntity> = emptyList()
    private var plantaSeleccionada: PlantaEntity? = null
    private val db = AppDatabase.getInstance(application)

    val ventasDetalle = db.ventaDao().obtenerVentasDetalle()
    val ranking = db.ventaDao().rankingPlantas()
    val totalPorAlbum = db.ventaDao().obtenerTotalPorAlbum()
    val resumenAlbumes = db.ventaDao().obtenerResumenAlbumes()
    val ventas = db.ventaDao().obtenerVentas()


    val totalGeneral = db.ventaDao().totalGeneral()

    val totalesPorAlbum = db.ventaDao().totalPorAlbum()


    val ventasSemana = db.ventaDao().ventasSemana()
    val ventasMes = db.ventaDao().ventasMes()
    val ventasAnio = db.ventaDao().ventasAnio()

    val rentabilidad = db.ventaDao().topRentabilidad()

    val ventasHistorial = db.ventaDao().obtenerVentasCompletas()




// =========================
// 🔵 FILTROS
// =========================

    private val filtroPeriodo = MutableStateFlow(PeriodoFiltro.TODO)
    private val filtroAlbum = MutableStateFlow<Long?>(null)
    private val filtroTexto = MutableStateFlow("")
    private val orden = MutableStateFlow(Orden.FECHA)

    // 🔵 RESULTADO FINAL
    val ventasFiltradas = combine(
        ventasHistorial.asFlow(),
        filtroPeriodo,
        filtroAlbum,
        filtroTexto,
        orden
    ) { lista, periodo, albumId, texto, orden ->

        var result = lista

        val ahora = System.currentTimeMillis()

        // 🔴 PERIODO
        result = when (periodo) {
            PeriodoFiltro.HOY -> {
                val inicio = inicioDia(ahora)
                result.filter { it.fecha >= inicio }
            }

            PeriodoFiltro.SIETE_DIAS -> {
                val desde = ahora - (7 * 24 * 60 * 60 * 1000)
                result.filter { it.fecha >= desde }
            }

            PeriodoFiltro.MES -> {
                val inicio = inicioMes(ahora)
                result.filter { it.fecha >= inicio }
            }

            PeriodoFiltro.TODO -> result
        }

        // 🔴 ALBUM
        albumId?.let {
            result = result.filter { it.albumId == albumId }
        }

        // 🔴 TEXTO
        if (texto.isNotEmpty()) {
            result = result.filter {
                (it.familia + " " + (it.especie ?: ""))
                    .contains(texto, true)
            }
        }

        // 🔴 ORDEN
        result = when (orden) {
            Orden.GANANCIA -> result.sortedByDescending { it.cantidad * it.precioUnitario }
            Orden.AZ -> result.sortedBy { it.familia + (it.especie ?: "") }
            Orden.FECHA -> result.sortedByDescending { it.fecha }
        }

        result
    }.asLiveData()

    // 🔵 INDICADORES
    val totalFiltrado = ventasFiltradas.map {
        it.sumOf { v -> v.cantidad * v.precioUnitario }
    }

    val cantidadResultados = ventasFiltradas.map { it.size }



    val resumen: LiveData<Pair<Int, Double>> = MediatorLiveData<Pair<Int, Double>>().apply {

        fun actualizar() {
            val cant = cantidadResultados.value ?: 0
            val total = totalFiltrado.value ?: 0.0
            value = Pair(cant, total)
        }

        addSource(cantidadResultados) { actualizar() }
        addSource(totalFiltrado) { actualizar() }
    }



    // 🔵 SETTERS
    fun setPeriodo(p: PeriodoFiltro) { filtroPeriodo.value = p }
    fun setAlbum(id: Long?) { filtroAlbum.value = id }
    fun setBusqueda(txt: String) { filtroTexto.value = txt }
    fun setOrden(o: Orden) { orden.value = o }

    // 🔧 UTILS
    private fun inicioDia(t: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = t
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        return c.timeInMillis
    }

    private fun inicioMes(t: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = t
        c.set(Calendar.DAY_OF_MONTH, 1)
        return c.timeInMillis
    }



// =========================
    // 🟢 PLANTAS (PARA NUEVA VENTA)
    // =========================

    suspend fun getPlantas(): List<PlantaEntity> {
        return db.plantaDao().getAll()
    }


    // =========================
    // 🔴 INSERTAR VENTA
    // =========================

    fun insertVenta(
        plantaId: Long,
        cantidad: Int,
        precio: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            db.ventaDao().insert(
                VentaEntity(
                    plantaId = plantaId,
                    cantidad = cantidad,
                    precioUnitario = precio,
                    fecha = System.currentTimeMillis(),
                    albumId = null // 🔥 venta aislada
                )
            )
        }
    }



    fun totalPorAlbum(albumId: Long) =
        db.ventaDao().totalPorAlbum(albumId)


    fun ventasPorAlbum(albumId: Long) =
        db.ventaDao().obtenerVentasPorAlbum(albumId)

    fun eliminarVenta(ventaId: Long, plantaId: Long, cantidad: Int) {

        viewModelScope.launch {

            val planta = db.plantaDao().obtenerEntityPorId(plantaId) ?: return@launch

            // devolver stock
            db.plantaDao().update(
                planta.copy(cantidad = planta.cantidad + cantidad)
            )

            db.ventaDao().deleteById(ventaId)
        }
    }


    fun editarVenta(
        ventaId: Long,
        plantaId: Long,
        cantidadVieja: Int,
        nuevaCantidad: Int,
        nuevoPrecio: Double,
        nuevaFecha: Long
    ) {

        viewModelScope.launch {

            val planta = db.plantaDao().obtenerEntityPorId(plantaId) ?: return@launch

            val stockRestaurado = planta.cantidad + cantidadVieja
            val nuevoStock = stockRestaurado - nuevaCantidad

            if (nuevoStock < 0) return@launch

            db.plantaDao().update(planta.copy(cantidad = nuevoStock))

            db.ventaDao().updateVentaCompleta(
                ventaId,
                nuevaCantidad,
                nuevoPrecio,
                nuevaFecha
            )
        }
    }

    fun registrarVenta(
        plantaId: Long,
        albumId: Long,
        cantidad: Int,
        precio: Double
    ) {

        viewModelScope.launch {

            try {

                android.util.Log.e("VENTA_DEBUG", "ENTRO A COROUTINE")

                val planta = db.plantaDao()
                    .obtenerEntityPorId(plantaId)

                android.util.Log.e("VENTA_DEBUG", "planta = $planta")

                if (planta == null) {
                    android.util.Log.e("VENTA_DEBUG", "PLANTA NULL")
                    return@launch
                }

                val nuevoStock = planta.cantidad - cantidad

                android.util.Log.e("VENTA_DEBUG", "stock = ${planta.cantidad}")
                android.util.Log.e("VENTA_DEBUG", "cantidad = $cantidad")
                android.util.Log.e("VENTA_DEBUG", "nuevoStock = $nuevoStock")

               /* if (nuevoStock < 0) {
                    android.util.Log.e("VENTA_DEBUG", "SIN STOCK")
                    return@launch
                }*/

                db.plantaDao().update(
                    planta.copy(cantidad = nuevoStock)
                )

                android.util.Log.e("VENTA_DEBUG", "STOCK ACTUALIZADO")

                db.ventaDao().insert(
                    VentaEntity(
                        plantaId = plantaId,
                        albumId = albumId,
                        cantidad = cantidad,
                        precioUnitario = precio,
                        fecha = System.currentTimeMillis()
                    )
                )

                android.util.Log.e("VENTA_DEBUG", "INSERT OK")

            } catch (e: Exception) {
                android.util.Log.e("VENTA_DEBUG", "ERROR = ${e.message}")
            }
        }



    }


    private fun copiarImagenADisco(context: Context): String {

        return try {

            val input = context.resources.openRawResource(R.raw.planta_dummy)

            val file = File(
                context.filesDir,
                "planta_${java.util.UUID.randomUUID()}.jpg"
            )

            val output = FileOutputStream(file)

            input.copyTo(output)

            output.close()
            input.close()

            android.util.Log.e("IMG_OK", file.absolutePath)

            file.absolutePath

        } catch (e: Exception) {

            android.util.Log.e("IMG_ERROR", e.message ?: "error")

            ""
        }
    }

    fun insertarPlantasTest(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {

            val nombres = listOf(
                "Acer Palmatum",
                "Ficus Benjamina",
                "Lavanda Officinalis",
                "Monstera Deliciosa",
                "Pothos Aureum",
                "Rosmarinus Officinalis",
                "Mentha Spicata",
                "Ocimum Basilicum",
                "Salvia Officinalis",
                "Thymus Vulgaris",
                "Eucalyptus Globulus",
                "Pinus Radiata",
                "Quercus Robur",
                "Betula Pendula",
                "Cedrus Libani",
                "Cupressus Sempervirens",
                "Araucaria Araucana",
                "Magnolia Grandiflora",
                "Jasminum Officinale",
                "Hibiscus Rosa",
                "Bougainvillea Spectabilis",
                "Camellia Japonica",
                "Gardenia Jasminoides",
                "Nerium Oleander",
                "Plumeria Rubra",
                "Dracaena Marginata",
                "Yucca Elephantipes",
                "Sansevieria Trifasciata",
                "Zamioculcas Zamiifolia",
                "Chlorophytum Comosum",
                "Dieffenbachia Seguine",
                "Philodendron Hederaceum",
                "Calathea Orbifolia",
                "Alocasia Amazonica",
                "Colocasia Esculenta",
                "Tradescantia Zebrina",
                "Begonia Rex",
                "Impatiens Walleriana",
                "Petunia Hybrida",
                "Geranium Peltatum"
            )

            nombres.forEachIndexed { index, nombre ->

                try {

                    val partes = nombre.split(" ")

                    val ruta = copiarImagenADisco(context)

                    android.util.Log.e("PLANTA_INSERT", "OK: $nombre")

                    db.plantaDao().insert(
                        PlantaEntity(
                            numeroPlanta = index + 1,
                            familia = partes[0],
                            especie = partes.getOrNull(1),
                            lugar = "",
                            lugarId = null,
                            fechaIngreso = System.currentTimeMillis(),
                            cantidad = (5..20).random(),
                            aLaVenta = true,
                            observaciones = null,
                            fechaFoto = System.currentTimeMillis(),
                            fotoRuta = ruta
                        )
                    )


                    android.util.Log.e("PLANTA_ID", "ID=$id | $nombre")
                } catch (e: Exception) {
                    android.util.Log.e("PLANTA_ERROR", "ERROR en $nombre -> ${e.message}")
                }
            }
        }
    }


    fun insertarVentasTestMasivo() {
        viewModelScope.launch(Dispatchers.IO) {

            val random = kotlin.random.Random(System.currentTimeMillis())
            val ahora = System.currentTimeMillis()

            val unDia = 24L * 60 * 60 * 1000
            val unMes = 30L * unDia

            // 🔴 USAR IDs REALES
            val plantas = db.plantaDao().getAll().map { it.id }

            if (plantas.isEmpty()) {
                android.util.Log.e("VENTAS_TEST", "NO HAY PLANTAS")
                return@launch
            }

            for (mes in 0 until 10) {

                val baseMes = ahora - (mes * unMes)

                repeat(5) {  // 🔴 5 ventas por mes

                    val plantaId = plantas.random(random)

                    val fechaRandom = baseMes - random.nextLong(0, 25 * unDia)

                    val cantidad = random.nextInt(1, 10)
                    val precio = random.nextDouble(500.0, 5000.0)

                    db.ventaDao().insert(
                        VentaEntity(
                            plantaId = plantaId,
                            albumId = null,
                            cantidad = cantidad,
                            precioUnitario = precio,
                            fecha = fechaRandom
                        )
                    )
                }
            }

            android.util.Log.e("VENTAS_TEST", "VENTAS GENERADAS")
        }
    }
    fun resumenPorPlanta(albumId: Long) =
        db.ventaDao().resumenPorPlanta(albumId)

    fun plantasPorAlbum(albumId: Long) =
        db.ventaDao().obtenerPlantasDelAlbum(albumId)

    fun controlStock(albumId: Long) =
        db.ventaDao().controlStock(albumId)


   suspend fun obtenerPlantasDisponibles(albumId: Long) =
        db.ventaDao().plantasDisponiblesParaVenta(albumId)



    enum class PeriodoFiltro {
        HOY, SIETE_DIAS, MES, TODO
    }

    enum class Orden {
        FECHA, GANANCIA, AZ
    }

}