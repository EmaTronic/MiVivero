package com.emanuel.mivivero.data.model

data class AlbumConCantidad(
    val id: Long,
    val nombre: String,
    val observaciones: String?,
    val estado: String,
    val fechaCreacion: Long,
    val cantidadPlantas: Int
)
