package com.emanuel.mivivero.data.model

data class FotoPlanta(
    val id: Long = 0L,
    val plantaId: Long,
    val rutaLocal: String,
    val fechaFoto: Long,
    val fechaGuardado: Long,
    val observaciones: String?
)
