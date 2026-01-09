package com.emanuel.mivivero.data.repository

import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.data.model.FotoPlanta

class PlantaRepository {

    private val plantas = mutableListOf<Planta>()
    private val fotos = mutableListOf<FotoPlanta>()

    fun addPlanta(planta: Planta) {
        plantas.add(planta)
    }

    fun addFoto(foto: FotoPlanta) {
        fotos.add(foto)
    }

    fun getPlantas(): List<Planta> {
        return plantas
    }

    fun getFotosDePlanta(plantaId: Long): List<FotoPlanta> {
        return fotos.filter { it.plantaId == plantaId }
    }
}
