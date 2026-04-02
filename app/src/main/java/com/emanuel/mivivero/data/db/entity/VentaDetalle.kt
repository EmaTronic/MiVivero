package com.emanuel.mivivero.data.db.entity

data class VentaDetalle(
    val id: Long,
    val plantaId: Long,
    val nombrePlanta: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val fecha: Long
) {
    val total: Double
        get() = cantidad * precioUnitario
}