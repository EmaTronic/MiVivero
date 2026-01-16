package com.emanuel.mivivero.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "fotos",
    foreignKeys = [
        ForeignKey(
            entity = PlantaEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FotoEntity(
    @PrimaryKey val id: Long,
    val plantaId: Long,
    val rutaLocal: String,
    val fechaFoto: Long,
    val fechaGuardado: Long,
    val observaciones: String?,
    val esPrincipal: Boolean
)
