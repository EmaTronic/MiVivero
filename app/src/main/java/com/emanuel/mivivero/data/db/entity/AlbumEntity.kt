package com.emanuel.mivivero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albumes")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val observaciones : String?,
    val estado: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)
