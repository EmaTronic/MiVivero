package com.emanuel.mivivero.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plantas")
data class PlantaEntity(
    @PrimaryKey val id: Long,
    val numeroPlanta: String,
    val familia: String,
    val especie: String?,
    val lugar: String,
    val fechaIngreso: Long,
    val cantidad: Int,
    val aLaVenta: Boolean,
    val observaciones: String?
)


