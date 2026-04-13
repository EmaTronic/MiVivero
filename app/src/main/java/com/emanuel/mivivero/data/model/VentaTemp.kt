package com.emanuel.mivivero.data.model

import com.emanuel.mivivero.data.local.entity.PlantaEntity

data class VentaTemp(
    val planta: PlantaEntity,
    val cantidad: Int,
    val precio: Double
)