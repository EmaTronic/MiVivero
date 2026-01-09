package com.emanuel.mivivero.data.repository

import com.emanuel.mivivero.data.model.Planta
import com.emanuel.mivivero.data.model.FotoPlanta
import java.time.LocalDate

class PlantaRepository {

    private val plantas = mutableListOf<Planta>()
    private val fotos = mutableListOf<FotoPlanta>()

    init {
        plantas.add(
            Planta(
                id = 1,
                numeroPlanta = "P-001",
                familia = "Cactaceae",
                especie = "Gymnocalycium",
                lugar = "Estantería A",
                fechaIngreso = System.currentTimeMillis(),
                cantidad = 2,
                aLaVenta = true,
                observaciones = "Buen estado"
            )
        )
    }

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
