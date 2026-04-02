package com.emanuel.mivivero.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val plantaId: Long,
    val albumId: Long,

    val cantidad: Int,
    val precioUnitario: Double,

    val fecha: Long = System.currentTimeMillis()
)