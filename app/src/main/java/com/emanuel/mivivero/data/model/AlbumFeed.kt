package com.emanuel.mivivero.data.model


data class AlbumFeed(

    val albumId: String = "",
    val uidAutor: String = "",

    val titulo: String = "",

    val categoria: String = "",
    val ciudad: String = "",

    val portadaUrl: String = "",

    val cantidadPlantas: Int = 0,
    val comentariosCount: Int = 0,

    val fechaPublicacion: Long = 0,
    val fechaExpiracion: Long = 0
)