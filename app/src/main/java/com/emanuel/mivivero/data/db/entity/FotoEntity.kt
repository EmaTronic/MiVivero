package com.emanuel.mivivero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fotos")
data class FotoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val plantaId: Int,
    val uri: String
)
