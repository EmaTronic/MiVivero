package com.emanuel.mivivero.ui.ventas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.emanuel.mivivero.data.db.entity.VentaDetalle
import com.emanuel.mivivero.data.db.entity.VentaEntity
import com.emanuel.mivivero.data.local.AppDatabase

class VentasViewModel(application: Application)
    : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val ventasDetalle = db.ventaDao().obtenerVentasDetalle()
    val ventas = db.ventaDao().obtenerVentas()
    val ranking = db.ventaDao().rankingPlantas()
    val totalPorAlbum = db.ventaDao().obtenerTotalPorAlbum()


    suspend fun obtenerVentasManual(): List<VentaDetalle> {
        return db.ventaDao().obtenerVentasDetalleDirecto()
    }
    suspend fun debugVentas(): List<VentaEntity> {
        return db.ventaDao().debugVentas()
    }
}