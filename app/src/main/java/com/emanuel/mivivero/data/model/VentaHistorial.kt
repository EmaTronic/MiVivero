package com.emanuel.mivivero.data.model

data class VentaHistorial(
    val albumid: Long?,
    val id: Long,
    val fecha: Long,
    val cantidad: Int,
    val precioUnitario: Double,
    val familia: String,
    val especie: String?
)