package com.emanuel.mivivero.data.model

import java.time.LocalDate

data class Planta(
    val id: Long = 0L,
    val numeroPlanta: Int,
    val familia: String,
    val especie: String?,
    val lugar: String,
    val fechaIngreso: Long,
    val cantidad: Int,
    val aLaVenta: Boolean,
    val observaciones: String?,
    val fotoRuta: String?,
    val fechaFoto: Long?


    )

