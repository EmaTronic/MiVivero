package com.emanuel.mivivero.data.model

data class PlantaAlbum(
    val idPlanta: Long,
    val nombre: String,      // familia + especie
    val cantidad: Int,       // definida al agregar al álbum
    val precio: Double,      // definida al agregar al álbum
    val fotoRuta: String     // SIEMPRE explícita
)