package com.emanuel.mivivero.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey val id: Int = 1,

    val nombreReal: String,
    val nick: String,
    val nombreVivero: String,
    val pais: String,
    val provincia: String,
    val ciudad: String,
    val email: String,
    val usuarioBloqueado: Boolean = false,
    val fechaRegistro: Long
)


