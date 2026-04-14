package com.emanuel.mivivero.data.model

data class VentaDetalle(
    val id: Long,
    val plantaId: Long,
    val nombrePlanta: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val fecha: Long
)