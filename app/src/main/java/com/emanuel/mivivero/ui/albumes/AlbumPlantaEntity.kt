package com.emanuel.mivivero.ui.albumes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.emanuel.mivivero.data.local.entity.AlbumEntity
import com.emanuel.mivivero.data.local.entity.PlantaEntity

@Entity(
    tableName = "album_planta",
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlantaEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("albumId"), Index("plantaId")]
)
data class AlbumPlantaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val albumId: Long,
    val plantaId: Long,
    val cantidad: Int,
    val precio: Double
)
