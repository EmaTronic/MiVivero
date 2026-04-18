package com.emanuel.mivivero.ui.ventas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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