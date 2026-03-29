package com.emanuel.mivivero.data.model

data class Notificacion(
    val id: String = "",
    val tipo: String = "",
    val mensaje: String = "",
    val nick: String = "",
    val albumTitulo: String = "",
    val albumId: String = "",
    val fecha: com.google.firebase.Timestamp? = null,
    val leido: Boolean = false
)