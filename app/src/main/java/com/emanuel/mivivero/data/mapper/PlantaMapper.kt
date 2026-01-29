package com.emanuel.mivivero.data.mapper

import com.emanuel.mivivero.data.local.entity.PlantaEntity
import com.emanuel.mivivero.data.model.Planta

object PlantaMapper {

    fun toEntity(planta: Planta): PlantaEntity =
        PlantaEntity(
            id = planta.id,
            numeroPlanta = planta.numeroPlanta,
            familia = planta.familia,
            especie = planta.especie,
            lugar = planta.lugar,
            fechaIngreso = planta.fechaIngreso,
            cantidad = planta.cantidad,
            aLaVenta = planta.aLaVenta,
            observaciones = planta.observaciones,
            fotoRuta = planta.fotoRuta,
            fechaFoto = planta.fechaFoto
        )

    fun toModel(entity: PlantaEntity): Planta =
        Planta(
            id = entity.id,
            numeroPlanta = entity.numeroPlanta,
            familia = entity.familia,
            especie = entity.especie,
            lugar = entity.lugar,
            fechaIngreso = entity.fechaIngreso,
            cantidad = entity.cantidad,
            aLaVenta = entity.aLaVenta,
            observaciones = entity.observaciones,
            fotoRuta = entity.fotoRuta,
            fechaFoto = entity.fechaFoto
        )
}
