package com.emanuel.mivivero.data.model

data class VentaHistorial(
    val id: Long,
    val fecha: Long,
    val cantidad: Int,
    val precioUnitario: Double,
    val familia: String,
    val especie: String?,
    val albumId: Long? // 🔴 ESTE ES EL QUE FALTA
)