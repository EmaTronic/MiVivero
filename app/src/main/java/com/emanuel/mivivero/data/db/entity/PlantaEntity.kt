package com.emanuel.mivivero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plantas")
data class PlantaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val numeroPlanta: Int,
    val familia: String,
    val especie: String?,
    val lugar: String = "",
    val lugarId: Int? = null,
    val fechaIngreso: Long,
    val cantidad: Int,
    val aLaVenta: Boolean,
    val observaciones: String?,
    val fotoRuta: String?,
    val fechaFoto: Long?
)
