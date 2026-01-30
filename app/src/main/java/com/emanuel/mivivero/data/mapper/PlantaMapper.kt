package com.emanuel.mivivero.data.mapper

import com.emanuel.mivivero.data.local.entity.PlantaEntity
import com.emanuel.mivivero.data.model.Planta

object PlantaMapper {

    fun toEntity(model: Planta): PlantaEntity {
        return PlantaEntity(
            id = model.id,
            numeroPlanta = model.numeroPlanta,   // 🔥 CLAVE
            familia = model.familia,
            especie = model.especie,
            lugar = model.lugar,
            fechaIngreso = model.fechaIngreso,
            cantidad = model.cantidad,
            aLaVenta = model.aLaVenta,
            observaciones = model.observaciones,
            fotoRuta = model.fotoRuta,
            fechaFoto = model.fechaFoto
        )
    }

    fun toModel(entity: PlantaEntity): Planta {
        return Planta(
            id = entity.id,
            numeroPlanta = entity.numeroPlanta,  // 🔥 CLAVE
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
}
