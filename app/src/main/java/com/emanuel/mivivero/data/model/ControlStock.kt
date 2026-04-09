package com.emanuel.mivivero.data.model

data class ControlStock(
    val plantaId: Long,
    val nombrePlanta: String,
    val stockActual: Int,
    val vendidas: Int
)