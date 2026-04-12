package com.emanuel.mivivero.ui.ventas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emanuel.mivivero.data.db.entity.VentaDetalle
import com.emanuel.mivivero.data.db.entity.VentaEntity
import com.emanuel.mivivero.data.local.AppDatabase
import kotlinx.coroutines.launch

class VentasViewModel(application: Application)
    : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val ventasDetalle = db.ventaDao().obtenerVentasDetalle()
    val ranking = db.ventaDao().rankingPlantas()
    val totalPorAlbum = db.ventaDao().obtenerTotalPorAlbum()
    val resumenAlbumes = db.ventaDao().obtenerResumenAlbumes()
    val ventas = db.ventaDao().obtenerVentas()


    val totalGeneral = db.ventaDao().totalGeneral()

    val totalesPorAlbum = db.ventaDao().totalPorAlbum()

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

    fun debugVentas() {

        viewModelScope.launch {

            val lista = db.ventaDao().debugVentas()

            android.util.Log.e("DB_REAL", "TOTAL = ${lista.size}")

            lista.forEach {
                android.util.Log.e(
                    "DB_REAL",
                    "VENTA id=${it.id} album=${it.albumId} planta=${it.plantaId}"
                )
            }
        }
    }

    fun debugAlbumes() {

        viewModelScope.launch {

            val lista = db.albumDao().debugAlbumes()

            android.util.Log.e("ALBUM_DEBUG", "TOTAL = ${lista.size}")

            lista.forEach {
                android.util.Log.e(
                    "ALBUM_DEBUG",
                    "ID=${it.id} NOMBRE=${it.nombre}"
                )
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

}