package com.emanuel.mivivero.data.model

data class Insight(
    val plantaId: Long,
    val nombre: String,
    val mensaje: String,
    val tipo: TipoInsight,
    val prioridad: Int
)

enum class TipoInsight {
    CRECIMIENTO,
    CAIDA,
    ALERTA,
    TOP,
    PREDICCION
}