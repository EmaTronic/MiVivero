package com.emanuel.mivivero.data.model

data class Comentario(
    val id: String = "",
    val uidAutor: String = "",
    val emailAutor: String = "",
    val texto: String = "",
    val fecha: com.google.firebase.Timestamp? = null
)