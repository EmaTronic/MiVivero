package com.emanuel.mivivero.data.model

data class Publicacion(
    val id: String = "",
    val imageUrl: String = "",
    val observacion: String = "",
    val emailAutor: String = "",
    val uidAutor: String = "",
    val estado: String = "pendiente",
    val nombreComun: String? = null,
    val nombreCientifico: String? = null,
    val identificadaPorUid: String? = null,
    val identificadaPorEmail: String? = null,
    val prioridadEstado: Int = 0,
    val fecha: com.google.firebase.Timestamp? = null
)