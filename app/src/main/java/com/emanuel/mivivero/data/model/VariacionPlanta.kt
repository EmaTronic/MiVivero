package com.emanuel.mivivero.data.model

data class VariacionPlanta(
    val plantaId: Long,
    val nombre: String,
    val actual: Int,
    val anterior: Int,
    val variacion: Double
)