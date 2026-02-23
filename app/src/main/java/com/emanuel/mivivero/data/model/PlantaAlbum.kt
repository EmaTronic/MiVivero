package com.emanuel.mivivero.data.model

data class PlantaAlbum(
    val plantaId: Long,
    val familia: String,
    val especie: String?,
    val cantidad: Int,
    val precio: Double,
    val fotoRuta: String
) {
    val nombreCompleto: String
        get() = if (!especie.isNullOrBlank())
            "$familia $especie"
        else
            familia
}

