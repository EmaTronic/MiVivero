package com.emanuel.mivivero.data.model

import java.time.LocalDate

data class Planta(
    val id: Long = 0L,
    val numeroPlanta: String,
    val familia: String,
    val especie: String?,
    val lugar: String,
    val fechaIngreso: LocalDate,
    val cantidad: Int,
    val aLaVenta: Boolean,
    val observaciones: String?
)
