package com.emanuel.mivivero.data.model

data class Publicacion(
    val id: String = "",
    val uidAutor: String = "",
    val emailAutor: String = "",
    val imageUrl: String = "",
    val observacion: String = "",
    val fecha: com.google.firebase.Timestamp? = null
)