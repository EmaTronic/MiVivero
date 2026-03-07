package com.emanuel.mivivero.data.local

data class PlantaConLugar(
    val id: Long,
    val numeroPlanta: Int,
    val familia: String,
    val especie: String?,
    val lugar: String,
    val lugarId: Int?,
    val fechaIngreso: Long,
    val cantidad: Int,
    val aLaVenta: Boolean,
    val observaciones: String?,
    val fotoRuta: String?,
    val fechaFoto: Long?,
    val lugarIcono: String?
)
