package com.emanuel.mivivero.data.model

import java.time.LocalDate

data class FotoPlanta(
    val id: Long = 0L,
    val plantaId: Long,
    val rutaLocal: String,
    val fechaFoto: LocalDate,
    val fechaGuardado: LocalDate,
    val observaciones: String?
)

