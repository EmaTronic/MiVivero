package com.emanuel.mivivero.data.model

data class Publicacion(
    val id: String = "",
    val imageUrl: String? = null,
    val observacion: String? = null,
    val emailAutor: String? = null,
    val uidAutor: String? = null,
    val estado: String? = null,
    val nombreComun: String? = null,
    val nombreCientifico: String? = null,
    val identificadaPorUid: String? = null,
    val identificadaPorEmail: String? = null,
    val prioridadEstado: Int = 0,
    val fecha: com.google.firebase.Timestamp? = null
)