package com.emanuel.mivivero.data.repository

import com.emanuel.mivivero.data.model.Planta

class PlantaRepository {

    private val plantas = mutableListOf<Planta>()

    fun agregarPlanta(planta: Planta) {
        plantas.add(planta)
    }

    fun obtenerPlantas(): List<Planta> {
        return plantas
    }
}
