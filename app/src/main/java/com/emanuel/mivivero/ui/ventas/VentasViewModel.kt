package com.emanuel.mivivero.ui.ventas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.emanuel.mivivero.data.local.AppDatabase

class VentasViewModel(application: Application)
    : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val ventas = db.ventaDao().obtenerVentas()
    val ranking = db.ventaDao().rankingPlantas()
    val totalPorAlbum = db.ventaDao().obtenerTotalPorAlbum()
}