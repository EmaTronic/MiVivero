package com.emanuel.mivivero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plantas")
data class PlantaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,           // 👈 Long (NO Int)

    val numeroPlanta: String,
    val familia: String,
    val especie: String?,
    val lugar: String,
    val fechaIngreso: Long,
    val cantidad: Int,
    val aLaVenta: Boolean,
    val observaciones: String?,
    val fotoRuta: String?,
    val fechaFoto: Long?         // 👈 EXISTE ACÁ TAMBIÉN
)
