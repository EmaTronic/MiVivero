package com.emanuel.mivivero.data.model




    data class Root(
        val paises: List<Pais>
    )

    data class Pais(
        val nombre: String,
        val provincias: List<Provincia>
    )

    data class Provincia(
        val nombre: String,
        val ciudades: List<String>
    )

